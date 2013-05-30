/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cpu.i8039;

/**
 *
 * @author george
 */
public class i8039H {
     public static final int I8039_IGNORE_INT   = 0;   /* Ignore interrupt                     */
     public static final int I8039_EXT_INT	= 1;	/* Execute a normal extern interrupt	*/
     public static final int I8039_TIMER_INT 	= 2;	/* Execute a Timer interrupt			*/
     public static final int I8039_COUNT_INT 	= 4;	/* Execute a Counter interrupt			*/   
}
