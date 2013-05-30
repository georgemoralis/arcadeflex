/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu;

import mame.cpuintrfH.cpu_interface;

/**
 *
 * @author george
 */
public class Dummy_cpu extends cpu_interface {
    
    public Dummy_cpu()
    {
        
    }
    @Override
    public String cpu_info(Object context, int regnum) {
        if( context==null && regnum!=0 )
		return "";
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
