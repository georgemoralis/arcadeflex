
package mame;

public class cpuintrfH {
    /* The old system is obsolete and no longer supported by the core */
    public static final int NEW_INTERRUPT_SYSTEM = 1;
    public static final int MAX_IRQ_LINES        = 8;       /* maximum number of IRQ lines per CPU */

    public static final int CLEAR_LINE		 = 0;		/* clear (a fired, held or pulsed) line */
    public static final int ASSERT_LINE          = 1;      /* assert an interrupt immediately */
    public static final int HOLD_LINE            = 2;       /* hold interrupt line until enable is true */
    public static final int PULSE_LINE		 = 3;		/* pulse interrupt line for one instruction */

    public static final int  MAX_REGS		 =64;		/* maximum number of register of any CPU */

    /* Values passed to the cpu_info function of a core to retrieve information */
    public static final int CPU_INFO_REG         = 0;
    public static final int CPU_INFO_FLAGS        =MAX_REGS;
    public static final int CPU_INFO_NAME         =MAX_REGS+1;
    public static final int CPU_INFO_FAMILY       =MAX_REGS+2;
    public static final int CPU_INFO_VERSION      =MAX_REGS+3;
    public static final int CPU_INFO_FILE         =MAX_REGS+4;
    public static final int CPU_INFO_CREDITS      =MAX_REGS+5; 
    public static final int CPU_INFO_REG_LAYOUT   =MAX_REGS+6;
    public static final int CPU_INFO_WIN_LAYOUT   =MAX_REGS+7;


    public static final int  CPU_IS_LE		= 0;	/* emulated CPU is little endian */
    public static final int  CPU_IS_BE		= 1;	/* emulated CPU is big endian */

    /*
     * This value is passed to cpu_get_reg to retrieve the previous
     * program counter value, ie. before a CPU emulation started
     * to fetch opcodes and arguments for the current instrution.
     */
    public static final int  REG_PREVIOUSPC	= -1;

    /*
     * This value is passed to cpu_get_reg/cpu_set_reg, instead of one of
     * the names from the enum a CPU core defines for it's registers,
     * to get or set the contents of the memory pointed to by a stack pointer.
     * You can specify the n'th element on the stack by (REG_SP_CONTENTS-n),
     * ie. lower negative values. The actual element size (UINT16 or UINT32)
     * depends on the CPU core.
     * This is also used to replace the cpu_geturnpc() function.
     */
    public static final int REG_SP_CONTENTS = -2;

    /* ASG 971222 -- added this generic structure */
    public static abstract interface burnPtr { public abstract void handler(int cycles); }
    public static abstract class cpu_interface
    {
        public int cpu_num;
 /*TODO*///        void (*reset)(void *param);
 /*TODO*///        void (*exit)(void);
  /*TODO*///       int (*execute)(int cycles);
                   public burnPtr burn;
  /*TODO*///       void (*burn)(int cycles);
  /*TODO*///       unsigned (*get_context)(void *reg);
  /*TODO*///       void (*set_context)(void *reg);
  /*TODO*///       unsigned (*get_pc)(void);
  /*TODO*///       void (*set_pc)(unsigned val);
  /*TODO*///       unsigned (*get_sp)(void);
  /*TODO*///       void (*set_sp)(unsigned val);
 /*TODO*///        unsigned (*get_reg)(int regnum);
  /*TODO*///       void (*set_reg)(int regnum, unsigned val);
 /*TODO*///        void (*set_nmi_line)(int linestate);
 /*TODO*///        void (*set_irq_line)(int irqline, int linestate);
  /*TODO*///       void (*set_irq_callback)(int(*callback)(int irqline));
  /*TODO*///       void (*internal_interrupt)(int type);
 /*TODO*///        void (*cpu_state_save)(void *file);
  /*TODO*///       void (*cpu_state_load)(void *file);
        public abstract String cpu_info(Object context, int regnum);
  /*TODO*///       unsigned (*cpu_dasm)(char *buffer,unsigned pc);
        public int num_irqs;
        public int default_vector;
        public int[] icount;
        public double overclock;
        public int no_int, irq_int, nmi_int;
  /*TODO*///       int (*memory_read)(int offset);
  /*TODO*///       void (*memory_write)(int offset, int data);
  /*TODO*///       void (*set_op_base)(int pc);
        public int address_shift;
        public int address_bits, endianess, align_unit, max_inst_len;
        public int abits1, abits2, abitsmin;
    }
    /* Returns previous pc (start of opcode causing read/write) */
    /* int cpu_getpreviouspc(void); */
 /*TODO*///    #define cpu_getpreviouspc() cpu_get_reg(REG_PREVIOUSPC)

    /* Returns the return address from the top of the stack (Z80 only) */
    /* int cpu_getreturnpc(void); */
    /* This can now be handled with a generic function */
  /*TODO*///   #define cpu_geturnpc() cpu_get_reg(REG_SP_CONTENTS)


    /* daisy-chain link */
  /*TODO*///   typedef struct {
  /*TODO*///       void (*reset)(int);             /* reset callback     */
  /*TODO*///       int  (*interrupt_entry)(int);   /* entry callback     */
  /*TODO*///       void (*interrupt_reti)(int);    /* reti callback      */
  /*TODO*///       int irq_param;                  /* callback paramater */
  /*TODO*///   }	Z80_DaisyChain;

    public static final int Z80_MAXDAISY	= 4;		/* maximum of daisy chan device */

    public static final int Z80_INT_REQ     = 0x01;    /* interrupt request mask       */
    public static final int Z80_INT_IEO     = 0x02;    /* interrupt disable mask(IEO)  */

/*TODO*///   #define Z80_VECTOR(device,state) (((device)<<8)|(state))    
}
