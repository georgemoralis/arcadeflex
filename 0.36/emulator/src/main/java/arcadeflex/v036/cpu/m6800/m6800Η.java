/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.m6800;

public class m6800Η {

    /*TODO*///enum {
/*TODO*///	M6800_PC=1, M6800_S, M6800_A, M6800_B, M6800_X, M6800_CC,
/*TODO*///	M6800_WAI_STATE, M6800_NMI_STATE, M6800_IRQ_STATE };
/*TODO*///
    public static final int M6800_INT_NONE = 0;
    /* No interrupt required */
    public static final int M6800_INT_IRQ = 1;
    /* Standard IRQ interrupt */
    public static final int M6800_INT_NMI = 2;
    /* NMI interrupt		  */
    public static final int M6800_WAI = 8;
    /* set when WAI is waiting for an interrupt */
    public static final int M6800_SLP = 0x10;
    /* HD63701 only */

    public static final int M6800_IRQ_LINE = 0;
    /* IRQ line number */
    public static final int M6800_TIN_LINE = 1;
    /* P20/Tin Input Capture line (eddge sense)     */
 /* Active eddge is selecrable by internal reg.  */
 /* raise eddge : CLEAR_LINE  -> ASSERT_LINE     */
 /* fall  eddge : ASSERT_LINE -> CLEAR_LINE      */
 /* it is usuali to use PULSE_LINE state         */

 /*TODO*////* PUBLIC GLOBALS */
/*TODO*///extern int m6800_ICount;
/*TODO*///
/*TODO*////* PUBLIC FUNCTIONS */
/*TODO*///void m6800_reset(void *param);
/*TODO*///void m6800_exit(void);
/*TODO*///int	m6800_execute(int cycles);
/*TODO*///unsigned m6800_get_context(void *dst);
/*TODO*///void m6800_set_context(void *src);
/*TODO*///unsigned m6800_get_pc(void);
/*TODO*///void m6800_set_pc(unsigned val);
/*TODO*///unsigned m6800_get_sp(void);
/*TODO*///void m6800_set_sp(unsigned val);
/*TODO*///unsigned m6800_get_reg(int regnum);
/*TODO*///void m6800_set_reg(int regnum, unsigned val);
/*TODO*///void m6800_set_nmi_line(int state);
/*TODO*///void m6800_set_irq_line(int irqline, int state);
/*TODO*///void m6800_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void m6800_state_save(void *file);
/*TODO*///void m6800_state_load(void *file);
/*TODO*///const char *m6800_info(void *context, int regnum);
/*TODO*///unsigned m6800_dasm(char *buffer, unsigned pc);
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * For now make the 6801 using the m6800 variables and functions
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_M6801)
/*TODO*///#define M6801_A 					M6800_A
/*TODO*///#define M6801_B 					M6800_B
/*TODO*///#define M6801_PC					M6800_PC
/*TODO*///#define M6801_S 					M6800_S
/*TODO*///#define M6801_X 					M6800_X
/*TODO*///#define M6801_CC					M6800_CC
/*TODO*///#define M6801_WAI_STATE 			M6800_WAI_STATE
/*TODO*///#define M6801_NMI_STATE 			M6800_NMI_STATE
/*TODO*///#define M6801_IRQ_STATE 			M6800_IRQ_STATE
/*TODO*///
/*TODO*///#define M6801_INT_NONE              M6800_INT_NONE
/*TODO*///#define M6801_INT_IRQ				M6800_INT_IRQ
/*TODO*///#define M6801_INT_NMI				M6800_INT_NMI
/*TODO*///#define M6801_WAI					M6800_WAI
/*TODO*///#define M6801_IRQ_LINE				M6800_IRQ_LINE
/*TODO*///
/*TODO*///#define m6801_ICount				m6800_ICount
/*TODO*///void m6801_reset(void *param);
/*TODO*///void m6801_exit(void);
/*TODO*///int	m6801_execute(int cycles);
/*TODO*///unsigned m6801_get_context(void *dst);
/*TODO*///void m6801_set_context(void *src);
/*TODO*///unsigned m6801_get_pc(void);
/*TODO*///void m6801_set_pc(unsigned val);
/*TODO*///unsigned m6801_get_sp(void);
/*TODO*///void m6801_set_sp(unsigned val);
/*TODO*///unsigned m6801_get_reg(int regnum);
/*TODO*///void m6801_set_reg(int regnum, unsigned val);
/*TODO*///void m6801_set_nmi_line(int state);
/*TODO*///void m6801_set_irq_line(int irqline, int state);
/*TODO*///void m6801_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void m6801_state_save(void *file);
/*TODO*///void m6801_state_load(void *file);
/*TODO*///const char *m6801_info(void *context, int regnum);
/*TODO*///unsigned m6801_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * For now make the 6802 using the m6800 variables and functions
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_M6802)
/*TODO*///#define M6802_A 					M6800_A
/*TODO*///#define M6802_B 					M6800_B
/*TODO*///#define M6802_PC					M6800_PC
/*TODO*///#define M6802_S 					M6800_S
/*TODO*///#define M6802_X 					M6800_X
/*TODO*///#define M6802_CC					M6800_CC
/*TODO*///#define M6802_WAI_STATE 			M6800_WAI_STATE
/*TODO*///#define M6802_NMI_STATE 			M6800_NMI_STATE
/*TODO*///#define M6802_IRQ_STATE 			M6800_IRQ_STATE
/*TODO*///
/*TODO*///#define M6802_INT_NONE              M6800_INT_NONE
/*TODO*///#define M6802_INT_IRQ				M6800_INT_IRQ
/*TODO*///#define M6802_INT_NMI				M6800_INT_NMI
/*TODO*///#define M6802_WAI					M6800_WAI
/*TODO*///#define M6802_IRQ_LINE				M6800_IRQ_LINE
/*TODO*///
/*TODO*///#define m6802_ICount				m6800_ICount
/*TODO*///void m6802_reset(void *param);
/*TODO*///void m6802_exit(void);
/*TODO*///int	m6802_execute(int cycles);
/*TODO*///unsigned m6802_get_context(void *dst);
/*TODO*///void m6802_set_context(void *src);
/*TODO*///unsigned m6802_get_pc(void);
/*TODO*///void m6802_set_pc(unsigned val);
/*TODO*///unsigned m6802_get_sp(void);
/*TODO*///void m6802_set_sp(unsigned val);
/*TODO*///unsigned m6802_get_reg(int regnum);
/*TODO*///void m6802_set_reg(int regnum, unsigned val);
/*TODO*///void m6802_set_nmi_line(int state);
/*TODO*///void m6802_set_irq_line(int irqline, int state);
/*TODO*///void m6802_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void m6802_state_save(void *file);
/*TODO*///void m6802_state_load(void *file);
/*TODO*///const char *m6802_info(void *context, int regnum);
/*TODO*///unsigned m6802_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * For now make the 6803 using the m6800 variables and functions
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_M6803)
/*TODO*///#define M6803_A 					M6800_A
/*TODO*///#define M6803_B 					M6800_B
/*TODO*///#define M6803_PC					M6800_PC
/*TODO*///#define M6803_S 					M6800_S
/*TODO*///#define M6803_X 					M6800_X
/*TODO*///#define M6803_CC					M6800_CC
/*TODO*///#define M6803_WAI_STATE 			M6800_WAI_STATE
/*TODO*///#define M6803_NMI_STATE 			M6800_NMI_STATE
/*TODO*///#define M6803_IRQ_STATE 			M6800_IRQ_STATE
/*TODO*///
/*TODO*///#define M6803_INT_NONE              M6800_INT_NONE
/*TODO*///#define M6803_INT_IRQ				M6800_INT_IRQ
/*TODO*///#define M6803_INT_NMI				M6800_INT_NMI
/*TODO*///#define M6803_WAI					M6800_WAI
/*TODO*///#define M6803_IRQ_LINE				M6800_IRQ_LINE
/*TODO*///#define M6803_TIN_LINE				M6800_TIN_LINE
/*TODO*///
/*TODO*///#define m6803_ICount				m6800_ICount
/*TODO*///void m6803_reset(void *param);
/*TODO*///void m6803_exit(void);
/*TODO*///int	m6803_execute(int cycles);
/*TODO*///unsigned m6803_get_context(void *dst);
/*TODO*///void m6803_set_context(void *src);
/*TODO*///unsigned m6803_get_pc(void);
/*TODO*///void m6803_set_pc(unsigned val);
/*TODO*///unsigned m6803_get_sp(void);
/*TODO*///void m6803_set_sp(unsigned val);
/*TODO*///unsigned m6803_get_reg(int regnum);
/*TODO*///void m6803_set_reg(int regnum, unsigned val);
/*TODO*///void m6803_set_nmi_line(int state);
/*TODO*///void m6803_set_irq_line(int irqline, int state);
/*TODO*///void m6803_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void m6803_state_save(void *file);
/*TODO*///void m6803_state_load(void *file);
/*TODO*///const char *m6803_info(void *context, int regnum);
/*TODO*///unsigned m6803_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*///#if (HAS_M6803||HAS_HD63701)
/*TODO*////* By default, on a port write port bits which are not set as output in the DDR */
/*TODO*////* are set to the value returned by a read from the same port. If you need to */
/*TODO*////* know the DDR for e.g. port 1, do m6803_internal_registers_r(M6801_DDR1) */
/*TODO*///
/*TODO*///#define M6803_DDR1	0x00
/*TODO*///#define M6803_DDR2	0x01
    public static final int M6803_PORT1 = 0x100;
    public static final int M6803_PORT2 = 0x101;
    /*TODO*///READ_HANDLER( m6803_internal_registers_r );
/*TODO*///WRITE_HANDLER( m6803_internal_registers_w );
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * For now make the 6808 using the m6800 variables and functions
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_M6808)
/*TODO*///#define M6808_A 					M6800_A
/*TODO*///#define M6808_B 					M6800_B
/*TODO*///#define M6808_PC					M6800_PC
/*TODO*///#define M6808_S 					M6800_S
/*TODO*///#define M6808_X 					M6800_X
/*TODO*///#define M6808_CC					M6800_CC
/*TODO*///#define M6808_WAI_STATE 			M6800_WAI_STATE
/*TODO*///#define M6808_NMI_STATE 			M6800_NMI_STATE
/*TODO*///#define M6808_IRQ_STATE 			M6800_IRQ_STATE
/*TODO*///
/*TODO*///#define M6808_INT_NONE              M6800_INT_NONE
/*TODO*///#define M6808_INT_IRQ               M6800_INT_IRQ
/*TODO*///#define M6808_INT_NMI               M6800_INT_NMI
/*TODO*///#define M6808_WAI                   M6800_WAI
/*TODO*///#define M6808_IRQ_LINE              M6800_IRQ_LINE
/*TODO*///
/*TODO*///#define m6808_ICount                m6800_ICount
/*TODO*///void m6808_reset(void *param);
/*TODO*///void m6808_exit(void);
/*TODO*///int	m6808_execute(int cycles);
/*TODO*///unsigned m6808_get_context(void *dst);
/*TODO*///void m6808_set_context(void *src);
/*TODO*///unsigned m6808_get_pc(void);
/*TODO*///void m6808_set_pc(unsigned val);
/*TODO*///unsigned m6808_get_sp(void);
/*TODO*///void m6808_set_sp(unsigned val);
/*TODO*///unsigned m6808_get_reg(int regnum);
/*TODO*///void m6808_set_reg(int regnum, unsigned val);
/*TODO*///void m6808_set_nmi_line(int state);
/*TODO*///void m6808_set_irq_line(int irqline, int state);
/*TODO*///void m6808_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void m6808_state_save(void *file);
/*TODO*///void m6808_state_load(void *file);
/*TODO*///const char *m6808_info(void *context, int regnum);
/*TODO*///unsigned m6808_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * For now make the HD63701 using the m6800 variables and functions
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_HD63701)
/*TODO*///#define HD63701_A					 M6800_A
/*TODO*///#define HD63701_B					 M6800_B
/*TODO*///#define HD63701_PC					 M6800_PC
/*TODO*///#define HD63701_S					 M6800_S
/*TODO*///#define HD63701_X					 M6800_X
/*TODO*///#define HD63701_CC					 M6800_CC
/*TODO*///#define HD63701_WAI_STATE			 M6800_WAI_STATE
/*TODO*///#define HD63701_NMI_STATE			 M6800_NMI_STATE
/*TODO*///#define HD63701_IRQ_STATE			 M6800_IRQ_STATE
/*TODO*///
/*TODO*///#define HD63701_INT_NONE             M6800_INT_NONE
/*TODO*///#define HD63701_INT_IRQ 			 M6800_INT_IRQ
/*TODO*///#define HD63701_INT_NMI 			 M6800_INT_NMI
/*TODO*///#define HD63701_WAI 				 M6800_WAI
/*TODO*///#define HD63701_SLP 				 M6800_SLP
/*TODO*///#define HD63701_IRQ_LINE			 M6800_IRQ_LINE
/*TODO*///#define HD63701_TIN_LINE			 M6800_TIN_LINE
/*TODO*///
/*TODO*///#define hd63701_ICount				 m6800_ICount
/*TODO*///void hd63701_reset(void *param);
/*TODO*///void hd63701_exit(void);
/*TODO*///int	hd63701_execute(int cycles);
/*TODO*///unsigned hd63701_get_context(void *dst);
/*TODO*///void hd63701_set_context(void *src);
/*TODO*///unsigned hd63701_get_pc(void);
/*TODO*///void hd63701_set_pc(unsigned val);
/*TODO*///unsigned hd63701_get_sp(void);
/*TODO*///void hd63701_set_sp(unsigned val);
/*TODO*///unsigned hd63701_get_reg(int regnum);
/*TODO*///void hd63701_set_reg(int regnum, unsigned val);
/*TODO*///void hd63701_set_nmi_line(int state);
/*TODO*///void hd63701_set_irq_line(int irqline, int state);
/*TODO*///void hd63701_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void hd63701_state_save(void *file);
/*TODO*///void hd63701_state_load(void *file);
/*TODO*///const char *hd63701_info(void *context, int regnum);
/*TODO*///unsigned hd63701_dasm(char *buffer, unsigned pc);
/*TODO*///
/*TODO*///void hd63701_trap_pc(void);
/*TODO*///
/*TODO*///#define HD63701_DDR1 M6803_DDR1
/*TODO*///#define HD63701_DDR2 M6803_DDR2

    public static final int HD63701_PORT1 = M6803_PORT1;
    public static final int HD63701_PORT2 = M6803_PORT2;

    /*TODO*///READ_HANDLER( hd63701_internal_registers_r );
/*TODO*///WRITE_HANDLER( hd63701_internal_registers_w );
/*TODO*///
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************
/*TODO*/// * For now make the NSC8105 using the m6800 variables and functions
/*TODO*/// ****************************************************************************/
/*TODO*///#if (HAS_NSC8105)
/*TODO*///#define NSC8105_A					 M6800_A
/*TODO*///#define NSC8105_B					 M6800_B
/*TODO*///#define NSC8105_PC					 M6800_PC
/*TODO*///#define NSC8105_S					 M6800_S
/*TODO*///#define NSC8105_X					 M6800_X
/*TODO*///#define NSC8105_CC					 M6800_CC
/*TODO*///#define NSC8105_WAI_STATE			 M6800_WAI_STATE
/*TODO*///#define NSC8105_NMI_STATE			 M6800_NMI_STATE
/*TODO*///#define NSC8105_IRQ_STATE			 M6800_IRQ_STATE
/*TODO*///
/*TODO*///#define NSC8105_INT_NONE             M6800_INT_NONE
/*TODO*///#define NSC8105_INT_IRQ 			 M6800_INT_IRQ
/*TODO*///#define NSC8105_INT_NMI 			 M6800_INT_NMI
/*TODO*///#define NSC8105_WAI 				 M6800_WAI
/*TODO*///#define NSC8105_IRQ_LINE			 M6800_IRQ_LINE
/*TODO*///#define NSC8105_TIN_LINE			 M6800_TIN_LINE
/*TODO*///
/*TODO*///#define nsc8105_ICount				 m6800_ICount
/*TODO*///void nsc8105_reset(void *param);
/*TODO*///void nsc8105_exit(void);
/*TODO*///int	nsc8105_execute(int cycles);
/*TODO*///unsigned nsc8105_get_context(void *dst);
/*TODO*///void nsc8105_set_context(void *src);
/*TODO*///unsigned nsc8105_get_pc(void);
/*TODO*///void nsc8105_set_pc(unsigned val);
/*TODO*///unsigned nsc8105_get_sp(void);
/*TODO*///void nsc8105_set_sp(unsigned val);
/*TODO*///unsigned nsc8105_get_reg(int regnum);
/*TODO*///void nsc8105_set_reg(int regnum, unsigned val);
/*TODO*///void nsc8105_set_nmi_line(int state);
/*TODO*///void nsc8105_set_irq_line(int irqline, int state);
/*TODO*///void nsc8105_set_irq_callback(int (*callback)(int irqline));
/*TODO*///void nsc8105_state_save(void *file);
/*TODO*///void nsc8105_state_load(void *file);
/*TODO*///const char *nsc8105_info(void *context, int regnum);
/*TODO*///unsigned nsc8105_dasm(char *buffer, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Read a byte from given memory location									*/
/*TODO*////****************************************************************************/
/*TODO*////* ASG 971005 -- changed to cpu_readmem16/cpu_writemem16 */
/*TODO*///#define M6800_RDMEM(Addr) ((unsigned)cpu_readmem16(Addr))
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* Write a byte to given memory location                                    */
/*TODO*////****************************************************************************/
/*TODO*///#define M6800_WRMEM(Addr,Value) (cpu_writemem16(Addr,Value))
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* M6800_RDOP() is identical to M6800_RDMEM() except it is used for reading */
/*TODO*////* opcodes. In case of system with memory mapped I/O, this function can be  */
/*TODO*////* used to greatly speed up emulation                                       */
/*TODO*////****************************************************************************/
/*TODO*///#define M6800_RDOP(Addr) ((unsigned)cpu_readop(Addr))
/*TODO*///
/*TODO*////****************************************************************************/
/*TODO*////* M6800_RDOP_ARG() is identical to M6800_RDOP() but it's used for reading  */
/*TODO*////* opcode arguments. This difference can be used to support systems that    */
/*TODO*////* use different encoding mechanisms for opcodes and opcode arguments       */
/*TODO*////****************************************************************************/
/*TODO*///#define M6800_RDOP_ARG(Addr) ((unsigned)cpu_readop_arg(Addr))
/*TODO*///
/*TODO*///#ifndef FALSE
/*TODO*///#    define FALSE 0
/*TODO*///#endif
/*TODO*///#ifndef TRUE
/*TODO*///#    define TRUE (!FALSE)
/*TODO*///#endif
/*TODO*///
/*TODO*///#ifdef	MAME_DEBUG
/*TODO*///unsigned Dasm680x(int subtype, char *buf, unsigned pc);
/*TODO*///#endif
/*TODO*///
/*TODO*///#endif /* _M6800_H */
/*TODO*///    
}
