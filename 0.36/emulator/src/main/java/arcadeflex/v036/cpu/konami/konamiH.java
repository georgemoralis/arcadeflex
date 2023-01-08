/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.konami;

public class konamiH {
    public static final int KONAMI_INT_NONE  =0;   /* No interrupt required */
    public static final int KONAMI_INT_IRQ	 =1;	/* Standard IRQ interrupt */
    public static final int KONAMI_INT_FIRQ  =2;	/* Fast IRQ */
    public static final int KONAMI_INT_NMI   =4;	/* NMI */	/* NS 970909 */
    public static final int KONAMI_IRQ_LINE	 =0;	/* IRQ line number */
    public static final int KONAMI_FIRQ_LINE =1;   /* FIRQ line number */    
    
/****************************************************************************/
/* Read a byte from given memory location									*/
/****************************************************************************/
//#define KONAMI_RDMEM(Addr) ((unsigned)cpu_readmem16(Addr))

/****************************************************************************/
/* Write a byte to given memory location                                    */
/****************************************************************************/
//#define KONAMI_WRMEM(Addr,Value) (cpu_writemem16(Addr,Value))

/****************************************************************************/
/* Z80_RDOP() is identical to Z80_RDMEM() except it is used for reading     */
/* opcodes. In case of system with memory mapped I/O, this function can be  */
/* used to greatly speed up emulation                                       */
/****************************************************************************/
//#define KONAMI_RDOP(Addr) ((unsigned)cpu_readop(Addr))

/****************************************************************************/
/* Z80_RDOP_ARG() is identical to Z80_RDOP() except it is used for reading  */
/* opcode arguments. This difference can be used to support systems that    */
/* use different encoding mechanisms for opcodes and opcode arguments       */
/****************************************************************************/
//#define KONAMI_RDOP_ARG(Addr) ((unsigned)cpu_readop_arg(Addr))
}
