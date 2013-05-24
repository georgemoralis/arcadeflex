/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package drivers;

import static mame.driverH.*;

/**
 *
 * @author george
 */
public class mario {
    
    //dummy functions for testing
    static InputPortPtr input_ports_mario = new InputPortPtr(){ public void handler() { }};
    static MachineDriver machine_driver_mario = new MachineDriver();
    static MachineDriver machine_driver_masao = new MachineDriver();
    
    
    static RomLoadPtr rom_mario = new RomLoadPtr(){ public void handler(){}};
    static RomLoadPtr rom_mariojp = new RomLoadPtr(){ public void handler(){}};
    static RomLoadPtr rom_masao = new RomLoadPtr(){ public void handler(){}};
    
    
    
    public static GameDriver driver_mario   = new GameDriver("1983","mario"  ,"mario.java", rom_mario  , null        , machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo of America", "Mario Bros. (US)" );
    public static GameDriver driver_mariojp = new GameDriver("1983","mariojp","mario.java", rom_mariojp, driver_mario, machine_driver_mario, input_ports_mario, null, ROT180, "Nintendo"           , "Mario Bros. (Japan)");
    public static GameDriver driver_masao   = new GameDriver("1983","masao"  ,"mario.java", rom_masao  , driver_mario, machine_driver_masao, input_ports_mario, null, ROT180, "bootleg"            , "Masao" );        

}
