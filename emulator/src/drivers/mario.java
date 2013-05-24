/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package drivers;

import static mame.driverH.*;
import static mame.memoryH.*;
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
    
    
    static RomLoadPtr rom_mario = new RomLoadPtr(){ public void handler(){}};
    static RomLoadPtr rom_mariojp = new RomLoadPtr(){ public void handler(){}};
    static RomLoadPtr rom_masao = new RomLoadPtr(){ public void handler(){}};
    
    
    
    public static GameDriver driver_mario   = new GameDriver("1983","mario"  ,"mario.java", rom_mario  , null        , machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo of America", "Mario Bros. (US)" );
    public static GameDriver driver_mariojp = new GameDriver("1983","mariojp","mario.java", rom_mariojp, driver_mario, machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo"           , "Mario Bros. (Japan)");
    public static GameDriver driver_masao   = new GameDriver("1983","masao"  ,"mario.java", rom_masao  , driver_mario, machine_driver_masao, input_ports_mario, null, ROT180, "bootleg"            , "Masao" );        

}
