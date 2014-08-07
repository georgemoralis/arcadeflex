
package cpu.m6502;


/**
 *
 * @author george
 */
public class m6502H {
    public static final int SUBTYPE_6502	=0;
    public static final int SUBTYPE_65C02	=1;
    public static final int SUBTYPE_6510	=2;
    public static final int SUBTYPE_2A03	=3;
    public static final int SUBTYPE_65SC02	=4;

     public static final int M6502_INT_NONE	=0;
     public static final int M6502_INT_IRQ	=1;
     public static final int M6502_INT_NMI	=2;
     /* use cpu_set_irq_line(cpu, M6502_SET_OVERFLOW, level)
       to change level of the so input line
       positiv edge sets overflow flag */
     public static final int M6502_SET_OVERFLOW =3;

     public static final int M6502_NMI_VEC	=0xfffa;
     public static final int M6502_RST_VEC	=0xfffc; 
     public static final int M6502_IRQ_VEC	=0xfffe;

}
