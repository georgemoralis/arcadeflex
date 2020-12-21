package gr.codebb.arcadeflex.v036.cpu.tms32010;

/**
 *
 * @author shadow
 */
public class tms32010H {
public static final int TMS320C10_ACTIVE_INT  =0;		/* Activate INT external interrupt		 */
public static final int TMS320C10_ACTIVE_BIO  =1;		/* Activate BIO for use with BIOZ inst	 */
public static final int TMS320C10_IGNORE_BIO  =-1;	/* Inhibit BIO polled external interrupt */
public static final int	TMS320C10_PENDING	  =0x80000000;
public static final int TMS320C10_NOT_PENDING =0;
public static final int TMS320C10_INT_NONE	 = -1;

public static final int  TMS320C10_ADDR_MASK =0x0fff;	/* TMS320C10 can only address 0x0fff */
										/* however other TMS320C1x devices	 */
										/* can address up to 0xffff (incase  */
										/* their support is ever added).	 */    
}
