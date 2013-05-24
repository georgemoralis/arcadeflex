/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
/**
 *
 * @author george
 */
public class mario {
    
        //dummy memory for testing
    	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x6000, 0x6fff, MRA_RAM ),
		new MemoryReadAddress( 0x7400, 0x77ff, MRA_RAM ),	/* video RAM */
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0xf000, 0xffff, MRA_ROM ),
	//	new MemoryReadAddress( 0x7c00, 0x7c00, input_port_0_r ),	/* IN0 */
	//	new MemoryReadAddress( 0x7c80, 0x7c80, input_port_1_r ),	/* IN1 */
	//	new MemoryReadAddress( 0x7f80, 0x7f80, input_port_2_r ),	/* DSW */
		new MemoryReadAddress( -1 )	/* end of table */
	};
	/*dummy memory for testing */
	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x6000, 0x68ff, MWA_RAM ),
		new MemoryWriteAddress( 0x6a80, 0x6fff, MWA_RAM ),
		//.new MemoryWriteAddress( 0x6900, 0x6a7f, MWA_RAM, spriteram,spriteram_size ),
		//new MemoryWriteAddress( 0x7400, 0x77ff, videoram_w, videoram,videoram_size ),
		//new MemoryWriteAddress( 0x7e80, 0x7e80, mario_gfxbank_w ),
		//new MemoryWriteAddress( 0x7e84, 0x7e84, interrupt_enable_w ),
		//new MemoryWriteAddress( 0x7e83, 0x7e83, MWA_RAM, mario_sprite_palette ),
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( 0xf000, 0xffff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};
    //dummy functions for testing
    static InputPortPtr input_ports_mario = new InputPortPtr(){ public void handler() { }};
    /*dummy machine drivers*/
    static MachineDriver machine_driver_mario = new MachineDriver
    (
        new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz (?) */
				readmem, writemem, null, null,
				null, 1
			),
                new MachineCPU(
			CPU_I8039 | CPU_AUDIO_CPU,
                        730000,         /* 730 khz */
			null,null,null,null,
			null,1
		)
		},
		60,0  
    );
    /*dummy machine drivers */
    static MachineDriver machine_driver_masao = new MachineDriver
    (
                  new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz (?) */
				readmem, writemem, null, null,
				null, 1
			),
                        new MachineCPU(
			CPU_Z80 | CPU_AUDIO_CPU,
                        730000,         /* 730 khz */
			null,null,null,null,
			null,1
                        )
		},
		60,0  
     );
    
    
    static RomLoadPtr rom_mario = new RomLoadPtr(){ public void handler(){
        ROM_REGION( 0x10000, REGION_CPU1 );	/* 64k for code */
	ROM_LOAD( "mario.7f",     0x0000, 0x2000, 0xc0c6e014 );
	ROM_LOAD( "mario.7e",     0x2000, 0x2000, 0x116b3856 );
	ROM_LOAD( "mario.7d",     0x4000, 0x2000, 0xdcceb6c1 );
	ROM_LOAD( "mario.7c",     0xf000, 0x1000, 0x4a63d96b );

	ROM_REGION( 0x1000, REGION_CPU2 );	/* sound */
	ROM_LOAD( "tma1c-a.6k",   0x0000, 0x1000, 0x06b9ff85 );

	ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "mario.3f",     0x0000, 0x1000, 0x28b0c42c );
	ROM_LOAD( "mario.3j",     0x1000, 0x1000, 0x0c8cc04d );

	ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "mario.7m",     0x0000, 0x1000, 0x22b7372e );
	ROM_LOAD( "mario.7n",     0x1000, 0x1000, 0x4f3a1f47 );
	ROM_LOAD( "mario.7p",     0x2000, 0x1000, 0x56be6ccd );
	ROM_LOAD( "mario.7s",     0x3000, 0x1000, 0x56f1d613 );
	ROM_LOAD( "mario.7t",     0x4000, 0x1000, 0x641f0008 );
	ROM_LOAD( "mario.7u",     0x5000, 0x1000, 0x7baf5309 );

	ROM_REGION( 0x0200, REGION_PROMS );
	ROM_LOAD( "mario.4p",     0x0000, 0x0200, 0xafc9bd41 );
        ROM_END();
    
    
    
    }};
    static RomLoadPtr rom_mariojp = new RomLoadPtr(){ public void handler()
    {
        ROM_REGION( 0x10000, REGION_CPU1 ); /* 64k for code */
	ROM_LOAD( "tma1c-a1.7f",  0x0000, 0x2000, 0xb64b6330 );
	ROM_LOAD( "tma1c-a2.7e",  0x2000, 0x2000, 0x290c4977 );
	ROM_LOAD( "tma1c-a1.7d",  0x4000, 0x2000, 0xf8575f31 );
	ROM_LOAD( "tma1c-a2.7c",  0xf000, 0x1000, 0xa3c11e9e );

	ROM_REGION( 0x1000, REGION_CPU2 );	/* sound */
	ROM_LOAD( "tma1c-a.6k",   0x0000, 0x1000, 0x06b9ff85 );

	ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "tma1v-a.3f",   0x0000, 0x1000, 0xadf49ee0 );
	ROM_LOAD( "tma1v-a.3j",   0x1000, 0x1000, 0xa5318f2d );

	ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "tma1v-a.7m",   0x0000, 0x1000, 0x186762f8 );
	ROM_LOAD( "tma1v-a.7n",   0x1000, 0x1000, 0xe0e08bba );
	ROM_LOAD( "tma1v-a.7p",   0x2000, 0x1000, 0x7b27c8c1 );
	ROM_LOAD( "tma1v-a.7s",   0x3000, 0x1000, 0x912ba80a );
	ROM_LOAD( "tma1v-a.7t",   0x4000, 0x1000, 0x5cbb92a5 );
	ROM_LOAD( "tma1v-a.7u",   0x5000, 0x1000, 0x13afb9ed );

	ROM_REGION( 0x0200, REGION_PROMS );
	ROM_LOAD( "mario.4p",     0x0000, 0x0200, 0xafc9bd41 );
        ROM_END();
    }};
    static RomLoadPtr rom_masao = new RomLoadPtr(){ public void handler()
    {
    	ROM_REGION( 0x10000, REGION_CPU1 ); /* 64k for code */
	ROM_LOAD( "masao-4.rom",  0x0000, 0x2000, 0x07a75745 );
	ROM_LOAD( "masao-3.rom",  0x2000, 0x2000, 0x55c629b6 );
	ROM_LOAD( "masao-2.rom",  0x4000, 0x2000, 0x42e85240 );
	ROM_LOAD( "masao-1.rom",  0xf000, 0x1000, 0xb2817af9 );

	ROM_REGION( 0x10000, REGION_CPU2 ); /* 64k for sound */
	ROM_LOAD( "masao-5.rom",  0x0000, 0x1000, 0xbd437198 );

	ROM_REGION( 0x2000, REGION_GFX1 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "masao-6.rom",  0x0000, 0x1000, 0x1c9e0be2 );
	ROM_LOAD( "masao-7.rom",  0x1000, 0x1000, 0x747c1349 );

	ROM_REGION( 0x6000, REGION_GFX2 | REGIONFLAG_DISPOSE );
	ROM_LOAD( "tma1v-a.7m",   0x0000, 0x1000, 0x186762f8 );
	ROM_LOAD( "masao-9.rom",  0x1000, 0x1000, 0x50be3918 );
	ROM_LOAD( "mario.7p",     0x2000, 0x1000, 0x56be6ccd );
	ROM_LOAD( "tma1v-a.7s",   0x3000, 0x1000, 0x912ba80a );
	ROM_LOAD( "tma1v-a.7t",   0x4000, 0x1000, 0x5cbb92a5 );
	ROM_LOAD( "tma1v-a.7u",   0x5000, 0x1000, 0x13afb9ed );

	ROM_REGION( 0x0200, REGION_PROMS );
	ROM_LOAD( "mario.4p",     0x0000, 0x0200, 0xafc9bd41 );
        ROM_END();
    }};
    
    
    
    public static GameDriver driver_mario   = new GameDriver("1983","mario"  ,"mario.java", rom_mario  , null        , machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo of America", "Mario Bros. (US)" );
    public static GameDriver driver_mariojp = new GameDriver("1983","mariojp","mario.java", rom_mariojp, driver_mario, machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo"           , "Mario Bros. (Japan)");
    public static GameDriver driver_masao   = new GameDriver("1983","masao"  ,"mario.java", rom_masao  , driver_mario, machine_driver_masao, input_ports_mario, null, ROT180, "bootleg"            , "Masao" );        

}
