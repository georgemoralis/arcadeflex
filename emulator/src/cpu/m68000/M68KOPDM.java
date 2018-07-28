package cpu.m68000;

import static cpu.m68000.m68kcpu.*;
import static arcadeflex.libc_old.*;
import static cpu.m68000.m68kcpuH.*;
import static cpu.m68000.m68kopsH.*;

public class M68KOPDM {

    /*TODO*///void m68000_dbt(void)
/*TODO*///{
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///

    public static opcode m68000_dbf = new opcode() {
        public void handler() {
            long d_reg = get_DY();
            long res = MASK_OUT_ABOVE_16(d_reg - 1);
            set_DY(MASK_OUT_BELOW_16(d_reg) | res);
            if (res != 0xffffL) {
                m68ki_branch_word(m68ki_read_16(get_CPU_PC()));
                USE_CLKS(10);
                /*if (m68klog != null) {
                 fprintf(m68klog, "dbf(1) :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
                 }*/
                return;
            }
            set_CPU_PC((get_CPU_PC() + 2) & 0xFFFFFFFFL);
            USE_CLKS(14);
            /*if (m68klog != null) {
             fprintf(m68klog, "dbf(2) :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
             }*/
        }
    };
    /*TODO*///void m68000_dbhi(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_HI)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbls(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_LS)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbcc(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_CC)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbcs(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_CS)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbne(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_NE)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbeq(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_EQ)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbvc(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_VC)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbvs(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_VS)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbpl(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_PL)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbmi(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_MI)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbge(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_GE)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dblt(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_LT)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dbgt(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_GT)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_dble(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NOT_LE)
/*TODO*///	{
/*TODO*///		uint *d_reg = &DY;
/*TODO*///		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);
/*TODO*///
/*TODO*///		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
/*TODO*///		if (res != 0xffff)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///			USE_CLKS(10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 2;
/*TODO*///		USE_CLKS(14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(DY);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_ai_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_AI));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_pi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_PI_16));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_pd_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_PD_16));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_di_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_DI));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_ix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_IX));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_aw_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_AW));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_al_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_AL));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(ea));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_16(EA_PCIX));
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divs_i_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	int src = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		int quotient = MAKE_INT_32(*d_dst) / src;
/*TODO*///		int remainder = MAKE_INT_32(*d_dst) % src;
/*TODO*///
/*TODO*///		if (quotient == MAKE_INT_16(quotient))
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(158+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(158+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(DY);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_ai_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AI);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_pi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PI_16);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_pd_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PD_16);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_di_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_DI);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_ix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_IX);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_aw_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AW);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_al_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AL);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PCIX);
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_divu_i_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///
/*TODO*///	if (src != 0)
/*TODO*///	{
/*TODO*///		uint quotient = *d_dst / src;
/*TODO*///		uint remainder = *d_dst % src;
/*TODO*///
/*TODO*///		if (quotient < 0x10000)
/*TODO*///		{
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_N = GET_MSB_16(quotient);
/*TODO*///			CPU_V = 0;
/*TODO*///			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
/*TODO*///			USE_CLKS(140+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_V = 1;
/*TODO*///		USE_CLKS(140+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_d_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = DY;
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_ai_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_AI);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+8);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+8);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_pi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_PI_32);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+8);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+8);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_pd_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_PD_32);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+10);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+10);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_di_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_DI);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+12);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+12);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_ix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_IX);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+14);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+14);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+14);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_aw_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_AW);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+12);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+12);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_al_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_AL);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+16);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+16);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+16);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_pcdi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint divisor = m68ki_read_32(ea);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+12);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+12);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_pcix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_32(EA_PCIX);
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+14);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+14);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+14);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_divl_i_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint divisor = m68ki_read_imm_32();
/*TODO*///		uint dividend_hi = CPU_D[word2 & 7];
/*TODO*///		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint quotient = 0;
/*TODO*///		uint remainder = 0;
/*TODO*///		uint dividend_neg = 0;
/*TODO*///		uint divisor_neg = 0;
/*TODO*///		int i;
/*TODO*///
/*TODO*///		if (divisor != 0)
/*TODO*///		{
/*TODO*///			/* quad / long : long quotient, long remainder */
/*TODO*///			if (BIT_A(word2))
/*TODO*///			{
/*TODO*///				/* if dividing the upper long does not clear it, we're overflowing. */
/*TODO*///				if (dividend_hi / divisor)
/*TODO*///				{
/*TODO*///					CPU_V = 1;
/*TODO*///					USE_CLKS(78+8);
/*TODO*///					return;
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (GET_MSB_32(dividend_hi))
/*TODO*///					{
/*TODO*///						dividend_neg = 1;
/*TODO*///						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
/*TODO*///						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
/*TODO*///					}
/*TODO*///					if (GET_MSB_32(divisor))
/*TODO*///					{
/*TODO*///						divisor_neg = 1;
/*TODO*///						divisor = MASK_OUT_ABOVE_32(-divisor);
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///				for (i = 31; i >= 0; i--)
/*TODO*///				{
/*TODO*///					quotient <<= 1;
/*TODO*///					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
/*TODO*///					if (remainder >= divisor)
/*TODO*///					{
/*TODO*///						remainder -= divisor;
/*TODO*///						quotient++;
/*TODO*///					}
/*TODO*///				}
/*TODO*///
/*TODO*///				if (BIT_B(word2))	   /* signed */
/*TODO*///				{
/*TODO*///					if (dividend_neg)
/*TODO*///					{
/*TODO*///						remainder = MASK_OUT_ABOVE_32(-remainder);
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///					}
/*TODO*///					if (divisor_neg)
/*TODO*///						quotient = MASK_OUT_ABOVE_32(-quotient);
/*TODO*///				}
/*TODO*///				CPU_D[word2 & 7] = remainder;
/*TODO*///				CPU_D[(word2 >> 12) & 7] = quotient;
/*TODO*///
/*TODO*///				CPU_N = GET_MSB_32(quotient);
/*TODO*///				CPU_NOT_Z = quotient;
/*TODO*///				CPU_V = 0;
/*TODO*///				USE_CLKS(78+8);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* long / long: long quotient, maybe long remainder */
/*TODO*///			if (BIT_B(word2))	   /* signed */
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
/*TODO*///				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
/*TODO*///			}
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(quotient);
/*TODO*///			CPU_NOT_Z = quotient;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(78+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY ^= MASK_OUT_ABOVE_8(DX));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY ^= MASK_OUT_ABOVE_16(DX));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_d_32(void)
/*TODO*///{
/*TODO*///	uint res = DY ^= DX;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eor_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = DX ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY ^= m68ki_read_imm_8());
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_ai_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pi_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pi7_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pd_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pd7_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_di_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_ix_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_aw_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_al_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = tmp ^ m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY ^= m68ki_read_imm_16());
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_ai_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pi_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pd_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_di_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_ix_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_aw_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_al_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = tmp ^ m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_d_32(void)
/*TODO*///{
/*TODO*///	uint res = DY ^= m68ki_read_imm_32();
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_ai_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pi_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_pd_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_di_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_ix_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_aw_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_al_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = tmp ^ m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_to_ccr(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_get_ccr() ^ m68ki_read_imm_16());
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_eori_to_sr(void)
/*TODO*///{
/*TODO*///	uint eor_val = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(m68ki_get_sr() ^ eor_val);
/*TODO*///		USE_CLKS(20);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_exg_dd(void)
/*TODO*///{
/*TODO*///	uint *reg_a = &DX;
/*TODO*///	uint *reg_b = &DY;
/*TODO*///	uint tmp = *reg_a;
/*TODO*///
/*TODO*///	*reg_a = *reg_b;
/*TODO*///	*reg_b = tmp;
/*TODO*///
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_exg_aa(void)
/*TODO*///{
/*TODO*///	uint *reg_a = &AX;
/*TODO*///	uint *reg_b = &AY;
/*TODO*///	uint tmp = *reg_a;
/*TODO*///
/*TODO*///	*reg_a = *reg_b;
/*TODO*///	*reg_b = tmp;
/*TODO*///
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_exg_da(void)
/*TODO*///{
/*TODO*///	uint *reg_a = &DX;
/*TODO*///	uint *reg_b = &AY;
/*TODO*///	uint tmp = *reg_a;
/*TODO*///
/*TODO*///	*reg_a = *reg_b;
/*TODO*///	*reg_b = tmp;
/*TODO*///
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ext_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | MASK_OUT_ABOVE_8(*d_dst) | (GET_MSB_8(*d_dst) ? 0xff00 : 0);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(*d_dst);
/*TODO*///	CPU_NOT_Z = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ext_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_ABOVE_16(*d_dst) | (GET_MSB_16(*d_dst) ? 0xffff0000 : 0);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(*d_dst);
/*TODO*///	CPU_NOT_Z = *d_dst;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_extb(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint *d_dst = &DY;
/*TODO*///
/*TODO*///		*d_dst = MASK_OUT_ABOVE_8(*d_dst) | (GET_MSB_8(*d_dst) ? 0xffffff00 : 0);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(*d_dst);
/*TODO*///		CPU_NOT_Z = *d_dst;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_illegal(void)
/*TODO*///{
/*TODO*///	m68ki_exception(EXCEPTION_ILLEGAL_INSTRUCTION);
/*TODO*///	M68K_DO_LOG((M68K_LOG, "%s at %08x: illegal instruction %04x (%s)\n",
/*TODO*///				 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PPC), CPU_IR,
/*TODO*///				 m68k_disassemble_quick(ADDRESS_68K(CPU_PPC))));
/*TODO*///#ifdef M68K_LOG
/*TODO*///	/* I use this to get the proper offset when disassembling the offending instruction */
/*TODO*///	m68k_pc_offset = 2;
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_ai(void)
/*TODO*///{
/*TODO*///	m68ki_branch_long(EA_AI);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_di(void)
/*TODO*///{
/*TODO*///	m68ki_branch_long(EA_DI);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_ix(void)
/*TODO*///{
/*TODO*///	m68ki_branch_long(EA_IX);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_aw(void)
/*TODO*///{
/*TODO*///	m68ki_branch_long(EA_AW);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_al(void)
/*TODO*///{
/*TODO*///	m68ki_branch_long(EA_AL);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jmp_pcix(void)
/*TODO*///{
/*TODO*///	m68ki_branch_long(EA_PCIX);
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(0+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+22);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_jsr_pcix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PCIX;
/*TODO*///
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_long(ea);
/*TODO*///	USE_CLKS(0+22);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_ai(void)
/*TODO*///{
/*TODO*///	AX = EA_AI;
/*TODO*///	USE_CLKS(0+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_di(void)
/*TODO*///{
/*TODO*///	AX = EA_DI;
/*TODO*///	USE_CLKS(0+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_ix(void)
/*TODO*///{
/*TODO*///	AX = EA_IX;
/*TODO*///	USE_CLKS(0+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_aw(void)
/*TODO*///{
/*TODO*///	AX = EA_AW;
/*TODO*///	USE_CLKS(0+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_al(void)
/*TODO*///{
/*TODO*///	AX = EA_AL;
/*TODO*///	USE_CLKS(0+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	AX = ea;
/*TODO*///	USE_CLKS(0+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lea_pcix(void)
/*TODO*///{
/*TODO*///	AX = EA_PCIX;
/*TODO*///	USE_CLKS(0+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_link_16_a7(void)
/*TODO*///{
/*TODO*///	CPU_A[7] -= 4;
/*TODO*///	m68ki_write_32(CPU_A[7], CPU_A[7]);
/*TODO*///	CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + MAKE_INT_16(m68ki_read_imm_16()));
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_link_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AY;
/*TODO*///
/*TODO*///	m68ki_push_32(*a_dst);
/*TODO*///	*a_dst = CPU_A[7];
/*TODO*///	CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + MAKE_INT_16(m68ki_read_imm_16()));
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_link_32_a7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		CPU_A[7] -= 4;
/*TODO*///		m68ki_write_32(CPU_A[7], CPU_A[7]);
/*TODO*///		CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + m68ki_read_imm_32());
/*TODO*///		USE_CLKS(16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_link_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint *a_dst = &AY;
/*TODO*///
/*TODO*///		m68ki_push_32(*a_dst);
/*TODO*///		*a_dst = CPU_A[7];
/*TODO*///		CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + m68ki_read_imm_32());
/*TODO*///		USE_CLKS(16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = shift > 8 ? 0 : (src >> (shift - 1)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = *d_dst;
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift <= 8)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///			CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///			CPU_N = 0;
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffffff00;
/*TODO*///		CPU_X = CPU_C = 0;
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift <= 16)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///			CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///			CPU_N = 0;
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffff0000;
/*TODO*///		CPU_X = CPU_C = 0;
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = *d_dst;
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 32)
/*TODO*///		{
/*TODO*///			*d_dst = res;
/*TODO*///			CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///			CPU_N = 0;
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst = 0;
/*TODO*///		CPU_X = CPU_C = (shift == 32 ? GET_MSB_32(src) : 0);
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsr_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = 0;
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = shift > 8 ? 0 : (src >> (8 - shift)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = (src >> (16 - shift)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src << shift);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = (src >> (32 - shift)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift <= 8)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///			CPU_X = CPU_C = (src >> (8 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_8(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffffff00;
/*TODO*///		CPU_X = CPU_C = 0;
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift <= 16)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///			CPU_X = CPU_C = (src >> (16 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_16(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffff0000;
/*TODO*///		CPU_X = CPU_C = 0;
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src << shift);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 32)
/*TODO*///		{
/*TODO*///			*d_dst = res;
/*TODO*///			CPU_X = CPU_C = (src >> (32 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_32(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst = 0;
/*TODO*///		CPU_X = CPU_C = (shift == 32 ? src & 1 : 0);
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = 0;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_lsl_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi7_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = (CPU_A[7] += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = AX++;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
    public static opcode m68000_move_pd7_al_8 = new opcode() {
        public void handler() {
            long res = m68ki_read_8(EA_AL());
            long ea_dst = m68k_cpu.ar[7] -= 2;

            m68ki_write_8(ea_dst, res);

            m68k_cpu.n_flag = GET_MSB_8(res);
            m68k_cpu.not_z_flag = res;
            m68k_cpu.v_flag = m68k_cpu.c_flag = 0;
            USE_CLKS(8 + 12);
            if (m68klog != null) {
                fprintf(m68klog, "m68000_move_pd7_al_8 :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
            }
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd7_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = CPU_A[7] -= 2;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = --AX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///
/*TODO*///	m68ki_write_8(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AI);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI_8);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD_8);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_di_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_DI);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_IX);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AW);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_al_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_AL);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_8(EA_PCIX);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_i_8(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_8();
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_8(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint ea_dst = (AX += 2) - 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint ea_dst = AX -= 2;
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	m68ki_write_16(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_a_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PCIX);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_i_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_16();
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_16(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_dd_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ai_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint ea_dst = AX;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pi_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint ea_dst = (AX += 4) - 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_pd_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint ea_dst = AX -= 4;
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_di_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_ix_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(m68ki_get_ea_ix_dst(), res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(18+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_aw_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(16+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_a_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(AY);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_32(ea);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PCIX);
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_al_i_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_imm_32();
/*TODO*///	uint ea_dst = m68ki_read_imm_32();
/*TODO*///
/*TODO*///	m68ki_write_32(ea_dst, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_d_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(DY);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_a_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(AY);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_ai_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_AI));
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pi_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_PI_16));
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pd_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_PD_16));
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_di_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_DI));
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_ix_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_IX));
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_aw_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_AW));
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_al_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_AL));
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(ea));
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pcix_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_16(EA_PCIX));
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_i_16(void)
/*TODO*///{
/*TODO*///	AX = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_d_32(void)
/*TODO*///{
/*TODO*///	AX = MASK_OUT_ABOVE_32(DY);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_a_32(void)
/*TODO*///{
/*TODO*///	AX = MASK_OUT_ABOVE_32(AY);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_ai_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_AI);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pi_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_PI_32);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pd_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_PD_32);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_di_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_DI);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_ix_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_IX);
/*TODO*///	USE_CLKS(4+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_aw_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_AW);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_al_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_AL);
/*TODO*///	USE_CLKS(4+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	AX = m68ki_read_32(ea);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_pcix_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_32(EA_PCIX);
/*TODO*///	USE_CLKS(4+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movea_i_32(void)
/*TODO*///{
/*TODO*///	AX = m68ki_read_imm_32();
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_d(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		DY = MASK_OUT_BELOW_16(DY) | m68ki_get_ccr();
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_ai(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_AI, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_pi(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_PI_16, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_pd(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_PD_16, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_di(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_DI, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_ix(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_IX, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_aw(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_AW, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_move_fr_ccr_al(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_write_16(EA_AL, m68ki_get_ccr());
/*TODO*///		USE_CLKS(8+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_d(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(DY);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_ai(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_AI));
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_pi(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_PI_16));
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_pd(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_PD_16));
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_di(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_DI));
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_ix(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_IX));
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_aw(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_AW));
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_al(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_AL));
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	m68ki_set_ccr(m68ki_read_16(ea));
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_pcix(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_16(EA_PCIX));
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_ccr_i(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_read_imm_16());
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_d(void)
/*TODO*///{
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		DY = MASK_OUT_BELOW_16(DY) | m68ki_get_sr();
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_sr_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///
/*TODO*///	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
/*TODO*///	{
/*TODO*///		m68ki_write_16(ea, m68ki_get_sr());
/*TODO*///		USE_CLKS(8+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_d(void)
/*TODO*///{
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_set_sr(DY);
/*TODO*///		USE_CLKS(12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_ai(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_AI);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_pi(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_PI_16);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_pd(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_PD_16);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_di(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_DI);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_ix(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_IX);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_aw(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_AW);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_al(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_AL);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint new_sr = m68ki_read_16(ea);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_pcix(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_16(EA_PCIX);
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_sr_i(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		USE_CLKS(12+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_fr_usp(void)
/*TODO*///{
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		AY = CPU_USP;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_move_to_usp(void)
/*TODO*///{
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_USP = AY;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_movec_cr(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		if (CPU_S)
/*TODO*///		{
/*TODO*///			uint next_word = m68ki_read_imm_16();
/*TODO*///
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			USE_CLKS(12);
/*TODO*///			switch (next_word & 0xfff)
/*TODO*///			{
/*TODO*///			case 0x000:			   /* SFC */
/*TODO*///				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_SFC;
/*TODO*///				return;
/*TODO*///			case 0x001:			   /* DFC */
/*TODO*///				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_DFC;
/*TODO*///				return;
/*TODO*///			case 0x002:			   /* CACR */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_CACR;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				return;
/*TODO*///			case 0x800:			   /* USP */
/*TODO*///				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_USP;
/*TODO*///				return;
/*TODO*///			case 0x801:			   /* VBR */
/*TODO*///				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_VBR;
/*TODO*///				return;
/*TODO*///			case 0x802:			   /* CAAR */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_CAAR;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				break;
/*TODO*///			case 0x803:			   /* MSP */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_MSP;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			case 0x804:			   /* ISP */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_ISP;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			default:
/*TODO*///#ifdef M68K_LOG
/*TODO*///				m68k_pc_offset = 4;
/*TODO*///#endif
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_movec_rc(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		if (CPU_S)
/*TODO*///		{
/*TODO*///			uint next_word = m68ki_read_imm_16();
/*TODO*///
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			USE_CLKS(10);
/*TODO*///			switch (next_word & 0xfff)
/*TODO*///			{
/*TODO*///			case 0x000:			   /* SFC */
/*TODO*///				CPU_SFC = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
/*TODO*///				return;
/*TODO*///			case 0x001:			   /* DFC */
/*TODO*///				CPU_DFC = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
/*TODO*///				return;
/*TODO*///			case 0x002:			   /* CACR */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					CPU_CACR = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			case 0x800:			   /* USP */
/*TODO*///				CPU_USP = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7];
/*TODO*///				return;
/*TODO*///			case 0x801:			   /* VBR */
/*TODO*///				CPU_VBR = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7];
/*TODO*///				return;
/*TODO*///			case 0x802:			   /* CAAR */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					CPU_CAAR = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			case 0x803:			   /* MSP */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					CPU_MSP = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			case 0x804:			   /* ISP */
/*TODO*///				if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///				{
/*TODO*///					CPU_ISP = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
/*TODO*///					return;
/*TODO*///				}
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			default:
/*TODO*///#ifdef M68K_LOG
/*TODO*///				m68k_pc_offset = 4;
/*TODO*///#endif
/*TODO*///				m68000_illegal();
/*TODO*///				return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_pd_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = AY;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			ea -= 2;
/*TODO*///			m68ki_write_16(ea, *(m68k_movem_pd_table[i]));
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	AY = ea;
/*TODO*///	USE_CLKS((count << 2) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_pd_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = AY;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			ea -= 4;
/*TODO*///			m68ki_write_32(ea, *(m68k_movem_pd_table[i]));
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	AY = ea;
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_pi_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = AY;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	AY = ea;
/*TODO*///	USE_CLKS((count << 2) + 12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_pi_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = AY;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	AY = ea;
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_ai_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_16(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_di_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_16(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_ix_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_16(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_aw_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_16(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_al_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_16(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_ai_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_32(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_di_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_32(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_ix_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_32(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_aw_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_32(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_re_al_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			m68ki_write_32(ea, *(m68k_movem_pi_table[i]));
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_ai_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_di_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_ix_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_aw_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_al_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_pcix_16(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PCIX;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
/*TODO*///			ea += 2;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	USE_CLKS((count << 2) + 8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_ai_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_di_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_ix_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_aw_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_al_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movem_er_pcix_32(void)
/*TODO*///{
/*TODO*///	uint i = 0;
/*TODO*///	uint register_list = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PCIX;
/*TODO*///	uint count = 0;
/*TODO*///
/*TODO*///	for (; i < 16; i++)
/*TODO*///		if (register_list & (1 << i))
/*TODO*///		{
/*TODO*///			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
/*TODO*///			ea += 4;
/*TODO*///			count++;
/*TODO*///		}
/*TODO*///	/* ASG: changed from (count << 4) to (count << 3) */
/*TODO*///	USE_CLKS((count << 3) + 8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movep_re_16(void)
/*TODO*///{
/*TODO*///	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
/*TODO*///	uint src = DX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea, MASK_OUT_ABOVE_8(src >> 8));
/*TODO*///	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src));
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movep_re_32(void)
/*TODO*///{
/*TODO*///	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
/*TODO*///	uint src = DX;
/*TODO*///
/*TODO*///	m68ki_write_8(ea, MASK_OUT_ABOVE_8(src >> 24));
/*TODO*///	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src >> 16));
/*TODO*///	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src >> 8));
/*TODO*///	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src));
/*TODO*///	USE_CLKS(24);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movep_er_16(void)
/*TODO*///{
/*TODO*///	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
/*TODO*///	uint *d_dst = &DX;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | ((m68ki_read_8(ea) << 8) + m68ki_read_8(ea + 2));
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_movep_er_32(void)
/*TODO*///{
/*TODO*///	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
/*TODO*///
/*TODO*///	DX = (m68ki_read_8(ea) << 24) + (m68ki_read_8(ea + 2) << 16)
/*TODO*///		+ (m68ki_read_8(ea + 4) << 8) + m68ki_read_8(ea + 6);
/*TODO*///	USE_CLKS(24);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_ai_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+18);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pi_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI_8;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pi7_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI7_8;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pd_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD_8;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pd7_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD7_8;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_di_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_DI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_ix_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_IX;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+24);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_aw_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AW;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_al_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AL;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+24);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_ai_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+18);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI_16;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pd_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD_16;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_di_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_DI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_ix_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_IX;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+24);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_aw_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AW;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+20);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_al_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AL;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+24);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		if (BIT_F(next_word))		   /* Memory to address register */
/*TODO*///		{
/*TODO*///			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to data register */
/*TODO*///		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_ai_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+8);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI_32;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+8);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_pd_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD_32;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+10);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_di_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_DI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+12);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_ix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_IX;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+14);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_aw_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AW;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+12);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_moves_al_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint next_word = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AL;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(0+16);
/*TODO*///		if (BIT_B(next_word))		   /* Register to memory */
/*TODO*///		{
/*TODO*///			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		/* Memory to register */
/*TODO*///		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_moveq(void)
/*TODO*///{
/*TODO*///	uint res = DX = MAKE_INT_8(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(DY) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_ai_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_AI)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_pi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_PI_16)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_pd_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_PD_16)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_di_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_DI)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+8);
/*TODO*///}
/*TODO*///
/*TODO*///
    public static opcode m68000_muls_ix_16 = new opcode() {
        public void handler() {
            long d_dst = get_DX();
            long res = MAKE_INT_16(m68ki_read_16(EA_IX())) * MAKE_INT_16(MASK_OUT_ABOVE_16(d_dst));

            set_DX(res);

            m68k_cpu.not_z_flag = res;
            m68k_cpu.n_flag = GET_MSB_32(res);
            m68k_cpu.v_flag = m68k_cpu.c_flag = 0;
            USE_CLKS(54 + 10);
            if (m68klog != null) {
                fprintf(m68klog, "m68000_muls_ix_16 :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
            }
        }
    };
    /*TODO*///
/*TODO*///void m68000_muls_aw_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_AW)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_al_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_AL)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(ea)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_16(EA_PCIX)) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_muls_i_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MAKE_INT_16(m68ki_read_imm_16()) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_ai_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_AI) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_pi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_PI_16) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_pd_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_PD_16) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_di_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_DI) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_ix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_IX) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_aw_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_AW) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_al_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_AL) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = m68ki_read_16(ea) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_16(EA_PCIX) * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_mulu_i_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint res = m68ki_read_imm_16() * MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(54+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_d_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = DY;
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_ai_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_AI);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_pi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_PI_32);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_pd_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_PD_32);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_di_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_DI);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_ix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_IX);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+14);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_aw_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_AW);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_al_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_AL);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+16);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_pcdi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint src = m68ki_read_32(ea);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_pcix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_32(EA_PCIX);
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+14);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_mull_i_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68ki_read_imm_32();
/*TODO*///		uint dst = CPU_D[(word2 >> 12) & 7];
/*TODO*///		uint neg = GET_MSB_32(src ^ dst);
/*TODO*///		uint r1;
/*TODO*///		uint r2;
/*TODO*///		uint r3;
/*TODO*///		uint lo;
/*TODO*///		uint hi;
/*TODO*///
/*TODO*///		if (BIT_B(word2))			   /* signed */
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				src = MASK_OUT_ABOVE_32(-src);
/*TODO*///			if (GET_MSB_32(dst))
/*TODO*///				dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///		}
/*TODO*///
/*TODO*///		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
/*TODO*///		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
/*TODO*///		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);
/*TODO*///
/*TODO*///		r1 = lo;
/*TODO*///		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
/*TODO*///		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);
/*TODO*///
/*TODO*///		if (BIT_B(word2) && neg)
/*TODO*///		{
/*TODO*///			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
/*TODO*///			lo = MASK_OUT_ABOVE_32(-lo);
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_D[(word2 >> 12) & 7] = lo;
/*TODO*///		if (BIT_A(word2))
/*TODO*///		{
/*TODO*///			CPU_D[word2 & 7] = hi;
/*TODO*///			CPU_N = GET_MSB_32(hi);
/*TODO*///			CPU_NOT_Z = hi | lo;
/*TODO*///			CPU_V = 0;
/*TODO*///			USE_CLKS(43+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(lo);
/*TODO*///		CPU_NOT_Z = lo;
/*TODO*///		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
/*TODO*///		USE_CLKS(43+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
}
