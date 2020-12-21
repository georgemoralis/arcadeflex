
package gr.codebb.arcadeflex.v036.cpu.m6800;


public class m6800H {

public static final int M6800_INT_NONE  =0;           /* No interrupt required */
public static final int M6800_INT_IRQ	=1;			/* Standard IRQ interrupt */
public static final int M6800_INT_NMI	=2;			/* NMI interrupt		  */
public static final int M6800_WAI		=8;			/* set when WAI is waiting for an interrupt */
public static final int M6800_SLP		=0x10;		/* HD63701 only */


public static final int M6800_IRQ_LINE	=0;			/* IRQ line number */
public static final int M6800_TIN_LINE	=1;			/* P20/Tin Input Capture line (eddge sense)     */
									/* Active eddge is selecrable by internal reg.  */
									/* raise eddge : CLEAR_LINE  -> ASSERT_LINE     */
									/* fall  eddge : ASSERT_LINE -> CLEAR_LINE      */
									/* it is usuali to use PULSE_LINE state         */    
}
