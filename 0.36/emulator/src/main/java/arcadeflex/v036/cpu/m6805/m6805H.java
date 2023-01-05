/*
 * ported to v0.36
 */
package arcadeflex.v036.cpu.m6805;

//mame imports
import static arcadeflex.v036.mame.memory.*;
import static arcadeflex.v036.mame.memoryH.*;

public class m6805H {

    public static final int M6805_PC = 1;
    public static final int M6805_S = 2;
    public static final int M6805_CC = 3;
    public static final int M6805_A = 4;
    public static final int M6805_X = 5;
    public static final int M6805_IRQ_STATE = 6;

    public static final int M6805_INT_NONE = 0;/* No interrupt required */
    public static final int M6805_INT_IRQ = 1;

    /*TODO*////****************************************************************************
/*TODO*/// * 68705 section
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_M68705)
/*TODO*///#define M68705_A					M6805_A
/*TODO*///#define M68705_PC					M6805_PC
/*TODO*///#define M68705_S					M6805_S
/*TODO*///#define M68705_X					M6805_X
/*TODO*///#define M68705_CC					M6805_CC
/*TODO*///#define M68705_IRQ_STATE			M6805_IRQ_STATE
/*TODO*///
    public static final int M68705_INT_NONE = M6805_INT_NONE;
    public static final int M68705_INT_IRQ = M6805_INT_IRQ;
    /*TODO*///
/*TODO*///#define m68705_ICount				m6805_ICount
/*TODO*///extern void m68705_reset(void *param);
/*TODO*///extern void m68705_exit(void);
/*TODO*///extern int	m68705_execute(int cycles);
/*TODO*///extern unsigned m68705_get_context(void *dst);
/*TODO*///extern void m68705_set_context(void *src);
/*TODO*///extern unsigned m68705_get_pc(void);
/*TODO*///extern void m68705_set_pc(unsigned val);
/*TODO*///extern unsigned m68705_get_sp(void);
/*TODO*///extern void m68705_set_sp(unsigned val);
/*TODO*///extern unsigned m68705_get_reg(int regnum);
/*TODO*///extern void m68705_set_reg(int regnum, unsigned val);
/*TODO*///extern void m68705_set_nmi_line(int state);
/*TODO*///extern void m68705_set_irq_line(int irqline, int state);
/*TODO*///extern void m68705_set_irq_callback(int (*callback)(int irqline));
/*TODO*///extern void m68705_state_save(void *file);
/*TODO*///extern void m68705_state_load(void *file);
/*TODO*///extern const char *m68705_info(void *context, int regnum);
/*TODO*///extern unsigned m68705_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * HD63705 section
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_HD63705)
/*TODO*///#define HD63705_A					M6805_A
/*TODO*///#define HD63705_PC					M6805_PC
/*TODO*///#define HD63705_S					M6805_S
/*TODO*///#define HD63705_X					M6805_X
/*TODO*///#define HD63705_CC					M6805_CC
    public static final int HD63705_NMI_STATE = M6805_IRQ_STATE;
    public static final int HD63705_IRQ1_STATE = M6805_IRQ_STATE + 1;
    public static final int HD63705_IRQ2_STATE = M6805_IRQ_STATE + 2;
    public static final int HD63705_ADCONV_STATE = M6805_IRQ_STATE + 3;

    public static final int HD63705_INT_NONE = M6805_INT_NONE;
    public static final int HD63705_INT_IRQ = M6805_INT_IRQ;
    public static final int HD63705_INT_NMI = 0x08;

    public static final int HD63705_INT_MASK = 0x1ff;

    public static final int HD63705_INT_IRQ1 = 0x00;
    public static final int HD63705_INT_IRQ2 = 0x01;

    public static final int HD63705_INT_TIMER1 = 0x02;
    public static final int HD63705_INT_TIMER2 = 0x03;
    public static final int HD63705_INT_TIMER3 = 0x04;
    public static final int HD63705_INT_PCI = 0x05;
    public static final int HD63705_INT_SCI = 0x06;
    public static final int HD63705_INT_ADCONV = 0x07;

    /*TODO*///
/*TODO*///#define hd63705_ICount				m6805_ICount
/*TODO*///extern void hd63705_reset(void *param);
/*TODO*///extern void hd63705_exit(void);
/*TODO*///extern int	hd63705_execute(int cycles);
/*TODO*///extern unsigned hd63705_get_context(void *dst);
/*TODO*///extern void hd63705_set_context(void *src);
/*TODO*///extern unsigned hd63705_get_pc(void);
/*TODO*///extern void hd63705_set_pc(unsigned val);
/*TODO*///extern unsigned hd63705_get_sp(void);
/*TODO*///extern void hd63705_set_sp(unsigned val);
/*TODO*///extern unsigned hd63705_get_reg(int regnum);
/*TODO*///extern void hd63705_set_reg(int regnum, unsigned val);
/*TODO*///extern void hd63705_set_nmi_line(int state);
/*TODO*///extern void hd63705_set_irq_line(int irqline, int state);
/*TODO*///extern void hd63705_set_irq_callback(int (*callback)(int irqline));
/*TODO*///extern void hd63705_state_save(void *file);
/*TODO*///extern void hd63705_state_load(void *file);
/*TODO*///extern const char *hd63705_info(void *context, int regnum);
/*TODO*///extern unsigned hd63705_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
    /**
     * *************************************************************************
     */
    /* Read a byte from given memory location                                   */
    /**
     * *************************************************************************
     */
    /* ASG 971005 -- changed to cpu_readmem16/cpu_writemem16 */
    public static char M6805_RDMEM(int Addr) {
        return (char) ((cpu_readmem16(Addr)) & 0xFF);
    }

    /**
     * *************************************************************************
     */
    /* Write a byte to given memory location                                    */
    /**
     * *************************************************************************
     */
    public static void M6805_WRMEM(int Addr, int Value) {
        cpu_writemem16(Addr & 0xFFFF, Value & 0xFF);
    }

    /**
     * *************************************************************************
     */
    /* M6805_RDOP() is identical to M6805_RDMEM() except it is used for reading */
 /* opcodes. In case of system with memory mapped I/O, this function can be  */
 /* used to greatly speed up emulation                                       */
    /**
     * *************************************************************************
     */
    public static char M6805_RDOP(int Addr) {
        return (char) ((cpu_readop(Addr)) & 0xFF);
    }

    /**
     * *************************************************************************
     */
    /* M6805_RDOP_ARG() is identical to M6805_RDOP() but it's used for reading  */
 /* opcode arguments. This difference can be used to support systems that    */
 /* use different encoding mechanisms for opcodes and opcode arguments       */
    /**
     * *************************************************************************
     */
    public static char M6805_RDOP_ARG(int Addr) {
        return (char) ((cpu_readop_arg(Addr)) & 0xFF);
    }

}
