package gr.codebb.arcadeflex.v036.cpu.m6809;

/**
 *
 * @author shadow
 */
public class m6809H {
    /*TODO*////*** m6809: Portable 6809 emulator ******************************************/
    /*TODO*///
    /*TODO*///#ifndef _M6809_H
    /*TODO*///#define _M6809_H
    /*TODO*///
    /*TODO*///#include "memory.h"
    /*TODO*///#include "osd_cpu.h"
    /*TODO*///
    /*TODO*///enum {
    /*TODO*///	M6809_PC=1, M6809_S, M6809_CC ,M6809_A, M6809_B, M6809_U, M6809_X, M6809_Y,
    /*TODO*///	M6809_DP, M6809_NMI_STATE, M6809_IRQ_STATE, M6809_FIRQ_STATE };
    /*TODO*///
    public static final int M6809_INT_NONE      =0;   /* No interrupt required */
    public static final int M6809_INT_IRQ	=1;	/* Standard IRQ interrupt */
    public static final int M6809_INT_FIRQ	=2;	/* Fast IRQ */
    public static final int  M6809_INT_NMI	=4;	/* NMI */	/* NS 970909 */
    public static final int M6809_IRQ_LINE	=0;	/* IRQ line number */
    public static final int M6809_FIRQ_LINE     =1;   /* FIRQ line number */
    /*TODO*///
    /*TODO*////* PUBLIC GLOBALS */
    /*TODO*///extern int  m6809_ICount;
    /*TODO*///
    /*TODO*///
    /*TODO*////* PUBLIC FUNCTIONS */
    /*TODO*///extern void m6809_reset(void *param);
    /*TODO*///extern void m6809_exit(void);
    /*TODO*///extern int m6809_execute(int cycles);  /* NS 970908 */
    /*TODO*///extern unsigned m6809_get_context(void *dst);
    /*TODO*///extern void m6809_set_context(void *src);
    /*TODO*///extern unsigned m6809_get_pc(void);
    /*TODO*///extern void m6809_set_pc(unsigned val);
    /*TODO*///extern unsigned m6809_get_sp(void);
    /*TODO*///extern void m6809_set_sp(unsigned val);
    /*TODO*///extern unsigned m6809_get_reg(int regnum);
    /*TODO*///extern void m6809_set_reg(int regnum, unsigned val);
    /*TODO*///extern void m6809_set_nmi_line(int state);
    /*TODO*///extern void m6809_set_irq_line(int irqline, int state);
    /*TODO*///extern void m6809_set_irq_callback(int (*callback)(int irqline));
    /*TODO*///extern void m6809_state_save(void *file);
    /*TODO*///extern void m6809_state_load(void *file);
    /*TODO*///extern const char *m6809_info(void *context,int regnum);
    /*TODO*///extern unsigned m6809_dasm(char *buffer, unsigned pc);
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* For now the 6309 is using the functions of the 6809						*/
    /*TODO*////****************************************************************************/
    /*TODO*///#if HAS_HD6309
    /*TODO*///#define M6309_A 				M6809_A
    /*TODO*///#define M6309_B 				M6809_B
    /*TODO*///#define M6309_PC				M6809_PC
    /*TODO*///#define M6309_S 				M6809_S
    /*TODO*///#define M6309_U 				M6809_U
    /*TODO*///#define M6309_X 				M6809_X
    /*TODO*///#define M6309_Y 				M6809_Y
    /*TODO*///#define M6309_CC				M6809_CC
    /*TODO*///#define M6309_DP				M6809_DP
    /*TODO*///#define M6309_NMI_STATE 		M6809_NMI_STATE
    /*TODO*///#define M6309_IRQ_STATE 		M6809_IRQ_STATE
    /*TODO*///#define M6309_FIRQ_STATE		M6809_FIRQ_STATE
    /*TODO*///
    public static final int HD6309_INT_NONE					=M6809_INT_NONE;
    public static final int HD6309_INT_IRQ					=M6809_INT_IRQ;
    public static final int HD6309_INT_FIRQ					=M6809_INT_FIRQ;
    public static final int HD6309_INT_NMI					=M6809_INT_NMI;
    public static final int M6309_IRQ_LINE					=M6809_IRQ_LINE;
    public static final int M6309_FIRQ_LINE 				=M6809_FIRQ_LINE;
    /*TODO*///
    /*TODO*///#define hd6309_ICount					 m6809_ICount
    /*TODO*///extern void hd6309_reset(void *param);
    /*TODO*///extern void hd6309_exit(void);
    /*TODO*///extern int hd6309_execute(int cycles);	/* NS 970908 */
    /*TODO*///extern unsigned hd6309_get_context(void *dst);
    /*TODO*///extern void hd6309_set_context(void *src);
    /*TODO*///extern unsigned hd6309_get_pc(void);
    /*TODO*///extern void hd6309_set_pc(unsigned val);
    /*TODO*///extern unsigned hd6309_get_sp(void);
    /*TODO*///extern void hd6309_set_sp(unsigned val);
    /*TODO*///extern unsigned hd6309_get_reg(int regnum);
    /*TODO*///extern void hd6309_set_reg(int regnum, unsigned val);
    /*TODO*///extern void hd6309_set_nmi_line(int state);
    /*TODO*///extern void hd6309_set_irq_line(int irqline, int state);
    /*TODO*///extern void hd6309_set_irq_callback(int (*callback)(int irqline));
    /*TODO*///extern void hd6309_state_save(void *file);
    /*TODO*///extern void hd6309_state_load(void *file);
    /*TODO*///extern const char *hd6309_info(void *context,int regnum);
    /*TODO*///extern unsigned hd6309_dasm(char *buffer, unsigned pc);
    /*TODO*///#endif
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* Read a byte from given memory location									*/
    /*TODO*////****************************************************************************/
    /*TODO*////* ASG 971005 -- changed to cpu_readmem16/cpu_writemem16 */
    /*TODO*///#define M6809_RDMEM(Addr) ((unsigned)cpu_readmem16(Addr))
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* Write a byte to given memory location                                    */
    /*TODO*////****************************************************************************/
    /*TODO*///#define M6809_WRMEM(Addr,Value) (cpu_writemem16(Addr,Value))
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* Z80_RDOP() is identical to Z80_RDMEM() except it is used for reading     */
    /*TODO*////* opcodes. In case of system with memory mapped I/O, this function can be  */
    /*TODO*////* used to greatly speed up emulation                                       */
    /*TODO*////****************************************************************************/
    /*TODO*///#define M6809_RDOP(Addr) ((unsigned)cpu_readop(Addr))
    /*TODO*///
    /*TODO*////****************************************************************************/
    /*TODO*////* Z80_RDOP_ARG() is identical to Z80_RDOP() except it is used for reading  */
    /*TODO*////* opcode arguments. This difference can be used to support systems that    */
    /*TODO*////* use different encoding mechanisms for opcodes and opcode arguments       */
    /*TODO*////****************************************************************************/
    /*TODO*///#define M6809_RDOP_ARG(Addr) ((unsigned)cpu_readop_arg(Addr))
    /*TODO*///
    /*TODO*///#ifndef FALSE
    /*TODO*///#    define FALSE 0
    /*TODO*///#endif
    /*TODO*///#ifndef TRUE
    /*TODO*///#    define TRUE (!FALSE)
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///#ifdef MAME_DEBUG
    /*TODO*///extern unsigned Dasm6809 (char *buffer, unsigned pc);
    /*TODO*///#endif
    /*TODO*///
    /*TODO*///#endif /* _M6809_H */
    /*TODO*///   
}
