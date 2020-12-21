package gr.codebb.arcadeflex.v036.cpu.m68000;

public class M68KOPAC
{
/*TODO*///void m68000_1010(void)
/*TODO*///{
/*TODO*///	m68ki_exception(EXCEPTION_1010);
/*TODO*///	M68K_DO_LOG_EMU((M68K_LOG, "%s at %08x: called 1010 instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_1111(void)
/*TODO*///{
/*TODO*///	m68ki_exception(EXCEPTION_1111);
/*TODO*///	M68K_DO_LOG_EMU((M68K_LOG, "%s at %08x: called 1111 instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_abcd_rr(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res += 6;
/*TODO*///	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res -= 0xa0;
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | MASK_OUT_ABOVE_8(res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res); /* officially undefined */
/*TODO*///
/*TODO*///	if (MASK_OUT_ABOVE_8(res) != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_abcd_mm_ax7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--AY);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res += 6;
/*TODO*///	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res -= 0xa0;
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res); /* officially undefined */
/*TODO*///
/*TODO*///	if (MASK_OUT_ABOVE_8(res) != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_abcd_mm_ay7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res += 6;
/*TODO*///	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res -= 0xa0;
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res); /* officially undefined */
/*TODO*///
/*TODO*///	if (MASK_OUT_ABOVE_8(res) != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_abcd_mm_axy7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res += 6;
/*TODO*///	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res -= 0xa0;
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res); /* officially undefined */
/*TODO*///
/*TODO*///	if (MASK_OUT_ABOVE_8(res) != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_abcd_mm(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--AY);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res += 6;
/*TODO*///	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res -= 0xa0;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res); /* officially undefined */
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	if (MASK_OUT_ABOVE_8(res) != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_ai_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_AI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pi_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PI_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pi7_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pd_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PD_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pd7_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_di_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_DI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_ix_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_IX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_aw_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_AW);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_al_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_AL);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pcix_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PCIX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_i_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_a_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_ai_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PI_16);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pd_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PD_16);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_di_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_DI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_ix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_IX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_aw_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AW);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_al_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AL);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PCIX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_i_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_a_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_ai_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_AI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pi_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_PI_32);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pd_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_PD_32);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_di_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_DI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_ix_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_IX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_aw_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_AW);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_al_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_AL);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_32(ea);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_pcix_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_PCIX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_er_i_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_add_re_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_d_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(DY));
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_a_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(AY));
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_ai_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_AI)));
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pi_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_PI_16)));
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pd_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_PD_16)));
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_di_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_DI)));
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_ix_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_IX)));
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_aw_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_AW)));
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_al_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_AL)));
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(ea)));
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(EA_PCIX)));
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_i_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_imm_16()));
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_d_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + DY);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_a_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + AY);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_ai_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_AI));
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pi_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_PI_32));
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pd_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_PD_32));
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_di_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_DI));
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_ix_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_IX));
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_aw_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_AW));
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_al_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_AL));
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(ea));
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_pcix_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(EA_PCIX));
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_adda_i_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_imm_32());
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_ai_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pi_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pi7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pd_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pd7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_di_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_ix_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_aw_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_al_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_di_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_al_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_di_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addi_al_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_ai_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pi_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pi7_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pd_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pd7_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_di_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_ix_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_aw_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_al_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_a_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AY;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + (((CPU_IR >> 9) - 1) & 7) + 1);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_di_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_al_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_a_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AY;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst + (((CPU_IR >> 9) - 1) & 7) + 1);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_di_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addq_al_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_rr_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_rr_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_rr_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_mm_8_ax7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--AY);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_mm_8_ay7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_mm_8_axy7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_mm_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--AY);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_8(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_mm_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(AY -= 2);
/*TODO*///	uint ea = (AX -= 2);
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_16(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_addx_mm_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(AY -= 4);
/*TODO*///	uint ea = (AX -= 4);
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(src + dst + (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = VFLAG_ADD_32(src, dst, res);
/*TODO*///	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
/*TODO*///	USE_CLKS(30);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (DY | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_AI) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_PI_8) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_PI7_8) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_PD_8) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_PD7_8) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_di_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_DI) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_IX) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_AW) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_al_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_AL) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(ea) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(EA_PCIX) | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_i_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_imm_8() | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (DY | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_AI) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_PI_16) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_PD_16) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_di_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_DI) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_IX) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_AW) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_al_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_AL) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(ea) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(EA_PCIX) | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_i_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_imm_16() | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_d_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= DY;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_AI);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_PI_32);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_PD_32);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_di_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_DI);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_IX);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_AW);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_al_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_AL);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = DX &= m68ki_read_32(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_32(EA_PCIX);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_er_i_32(void)
/*TODO*///{
/*TODO*///	uint res = DX &= m68ki_read_imm_32();
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_and_re_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));
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
/*TODO*///void m68000_and_re_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));
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
/*TODO*///void m68000_and_re_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_and_re_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_and_re_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_and_re_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_and_re_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_and_re_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_and_re_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = DX & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY &= (m68ki_read_imm_8() | 0xffffff00));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_andi_ai_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_pi_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_pi7_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_pd_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_pd7_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_di_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_ix_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_aw_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_al_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = tmp & m68ki_read_8(ea);
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
/*TODO*///void m68000_andi_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY &= (m68ki_read_imm_16() | 0xffff0000));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_andi_ai_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_pi_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_pd_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_di_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_ix_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_aw_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_al_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = tmp & m68ki_read_16(ea);
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
/*TODO*///void m68000_andi_d_32(void)
/*TODO*///{
/*TODO*///	uint res = DY &= (m68ki_read_imm_32());
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_andi_ai_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_pi_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_pd_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_di_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_ix_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_aw_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_al_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = tmp & m68ki_read_32(ea);
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
/*TODO*///void m68000_andi_to_ccr(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_get_ccr() & m68ki_read_imm_16());
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_andi_to_sr(void)
/*TODO*///{
/*TODO*///	uint and_val = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(m68ki_get_sr() & and_val);
/*TODO*///		USE_CLKS(20);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	if (GET_MSB_8(src))
/*TODO*///		res |= m68k_shift_8_table[shift];
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_X = CPU_C = shift > 7 ? CPU_N : (src >> (shift - 1)) & 1;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= m68k_shift_16_table[shift];
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	if (GET_MSB_32(src))
/*TODO*///		res |= m68k_shift_32_table[shift];
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 8)
/*TODO*///		{
/*TODO*///			if (GET_MSB_8(src))
/*TODO*///				res |= m68k_shift_8_table[shift];
/*TODO*///
/*TODO*///			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///			CPU_C = CPU_X = (src >> (shift - 1)) & 1;
/*TODO*///			CPU_N = GET_MSB_8(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (GET_MSB_8(src))
/*TODO*///		{
/*TODO*///			*d_dst |= 0xff;
/*TODO*///			CPU_C = CPU_X = 1;
/*TODO*///			CPU_N = 1;
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffffff00;
/*TODO*///		CPU_C = CPU_X = 0;
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
/*TODO*///void m68000_asr_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 16)
/*TODO*///		{
/*TODO*///			if (GET_MSB_16(src))
/*TODO*///				res |= m68k_shift_16_table[shift];
/*TODO*///
/*TODO*///			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///			CPU_C = CPU_X = (src >> (shift - 1)) & 1;
/*TODO*///			CPU_N = GET_MSB_16(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (GET_MSB_16(src))
/*TODO*///		{
/*TODO*///			*d_dst |= 0xffff;
/*TODO*///			CPU_C = CPU_X = 1;
/*TODO*///			CPU_N = 1;
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffff0000;
/*TODO*///		CPU_C = CPU_X = 0;
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
/*TODO*///void m68000_asr_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = src >> shift;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 32)
/*TODO*///		{
/*TODO*///			if (GET_MSB_32(src))
/*TODO*///				res |= m68k_shift_32_table[shift];
/*TODO*///
/*TODO*///			*d_dst = res;
/*TODO*///
/*TODO*///			CPU_C = CPU_X = (src >> (shift - 1)) & 1;
/*TODO*///			CPU_N = GET_MSB_32(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (GET_MSB_32(src))
/*TODO*///		{
/*TODO*///			*d_dst = 0xffffffff;
/*TODO*///			CPU_C = CPU_X = 1;
/*TODO*///			CPU_N = 1;
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst = 0;
/*TODO*///		CPU_C = CPU_X = 0;
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
/*TODO*///void m68000_asr_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asr_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = src >> 1;
/*TODO*///
/*TODO*///	if (GET_MSB_16(src))
/*TODO*///		res |= 0x8000;
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///	CPU_C = CPU_X = src & 1;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_X = CPU_C = (src >> (8 - shift)) & 1;
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	src &= m68k_shift_8_table[shift + 1];
/*TODO*///	CPU_V = !(src == 0 || (src == m68k_shift_8_table[shift + 1] && shift < 8));
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_s_16(void)
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
/*TODO*///	src &= m68k_shift_16_table[shift + 1];
/*TODO*///	CPU_V = !(src == 0 || src == m68k_shift_16_table[shift + 1]);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_s_32(void)
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
/*TODO*///	src &= m68k_shift_32_table[shift + 1];
/*TODO*///	CPU_V = !(src == 0 || src == m68k_shift_32_table[shift + 1]);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(src << shift);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 8)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///			CPU_X = CPU_C = (src >> (8 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_8(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			src &= m68k_shift_8_table[shift + 1];
/*TODO*///			CPU_V = !(src == 0 || src == m68k_shift_8_table[shift + 1]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffffff00;
/*TODO*///		CPU_X = CPU_C = (shift == 8 ? src & 1 : 0);
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = !(src == 0);
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
/*TODO*///void m68000_asl_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = DX & 0x3f;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(src << shift);
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		if (shift < 16)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///			CPU_X = CPU_C = (src >> (16 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_16(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			src &= m68k_shift_16_table[shift + 1];
/*TODO*///			CPU_V = !(src == 0 || src == m68k_shift_16_table[shift + 1]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst &= 0xffff0000;
/*TODO*///		CPU_X = CPU_C = (shift == 16 ? src & 1 : 0);
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = !(src == 0);
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
/*TODO*///void m68000_asl_r_32(void)
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
/*TODO*///			src &= m68k_shift_32_table[shift + 1];
/*TODO*///			CPU_V = !(src == 0 || src == m68k_shift_32_table[shift + 1]);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///
/*TODO*///		*d_dst = 0;
/*TODO*///		CPU_X = CPU_C = (shift == 32 ? src & 1 : 0);
/*TODO*///		CPU_N = 0;
/*TODO*///		CPU_NOT_Z = 0;
/*TODO*///		CPU_V = !(src == 0);
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
/*TODO*///void m68000_asl_ea_ai(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_ea_pi(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_ea_pd(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_ea_di(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_ea_ix(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_ea_aw(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_asl_ea_al(void)
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
/*TODO*///	src &= 0xc000;
/*TODO*///	CPU_V = !(src == 0 || src == 0xc000);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bhi_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_HI)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bhi_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_HI)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bhi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_HI)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bls_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bls_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bls_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LS)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bcc_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_CC)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bcc_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_CC)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bcc_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CC)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bcs_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_CS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bcs_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_CS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bcs_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CS)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bne_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NE)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bne_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NE)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bne_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_NE)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_beq_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_EQ)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_beq_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_EQ)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_beq_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_EQ)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bvc_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_VC)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bvc_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_VC)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bvc_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VC)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bvs_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_VS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bvs_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_VS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bvs_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VS)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bpl_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_PL)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bpl_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_PL)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bpl_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_PL)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bmi_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_MI)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bmi_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_MI)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bmi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_MI)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bge_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_GE)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bge_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_GE)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bge_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GE)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_blt_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LT)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_blt_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LT)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_blt_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LT)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bgt_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_GT)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bgt_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_GT)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bgt_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GT)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ble_8(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LE)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ble_16(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LE)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_PC += 2;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_ble_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LE)
/*TODO*///		{
/*TODO*///			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///			m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///			USE_CLKS(6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_PC += 4;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint mask = 1 << (DX & 0x1f);
/*TODO*///
/*TODO*///	CPU_NOT_Z = *d_dst & mask;
/*TODO*///	*d_dst ^= mask;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_pi7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_pd7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_r_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 0x1f);
/*TODO*///
/*TODO*///	CPU_NOT_Z = *d_dst & mask;
/*TODO*///	*d_dst ^= mask;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_ai(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_pi(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_pi7(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_pd(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_pd7(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_di(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_ix(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_aw(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bchg_s_al(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src ^ mask);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint mask = 1 << (DX & 0x1f);
/*TODO*///
/*TODO*///	CPU_NOT_Z = *d_dst & mask;
/*TODO*///	*d_dst &= ~mask;
/*TODO*///	USE_CLKS(10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_pi7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_pd7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_r_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 0x1f);
/*TODO*///
/*TODO*///	CPU_NOT_Z = *d_dst & mask;
/*TODO*///	*d_dst &= ~mask;
/*TODO*///	USE_CLKS(14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_ai(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_pi(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_pi7(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_pd(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_pd7(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_di(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_ix(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_aw(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bclr_s_al(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src & ~mask);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfchg_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	DY ^= mask;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfchg_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) ^ (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) ^ (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfchg_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) ^ (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) ^ (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfchg_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) ^ (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) ^ (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfchg_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) ^ (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) ^ (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfchg_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) ^ (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) ^ (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfclr_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	DY &= ~mask;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfclr_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfclr_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfclr_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfclr_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfclr_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_pcdi(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint base = ea + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfexts_pcix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_PCIX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	if ((CPU_N = (data >> (width - 1)) & 1))
/*TODO*///		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///	USE_CLKS(15+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_pcdi(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint base = ea + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfextu_pcix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_PCIX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_D[(word2 >> 12) & 7] = data;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(15+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_pcdi(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint base = ea + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfffo_pcix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_PCIX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = 1 << (width - 1);
/*TODO*///
/*TODO*///	for (; mask && !(data & mask); mask >>= 1, full_offset++)
/*TODO*///		;
/*TODO*///	CPU_D[(word2 >> 12) & 7] = full_offset;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(28+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfins_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
/*TODO*///	uint orig_insert = insert >> (32 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	insert = ROR_32(insert, offset);
/*TODO*///	mask = ~ROR_32(mask, offset);
/*TODO*///
/*TODO*///	DY &= mask;
/*TODO*///	DY |= insert;
/*TODO*///
/*TODO*///	CPU_N = orig_insert >> (width - 1);
/*TODO*///	CPU_NOT_Z = orig_insert;
/*TODO*///	USE_CLKS(10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfins_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)) | (insert >> offset));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))) | MASK_OUT_ABOVE_8(insert << (8 - offset)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(insert);
/*TODO*///	CPU_NOT_Z = insert;
/*TODO*///	USE_CLKS(17+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfins_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)) | (insert >> offset));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))) | MASK_OUT_ABOVE_8(insert << (8 - offset)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(insert);
/*TODO*///	CPU_NOT_Z = insert;
/*TODO*///	USE_CLKS(17+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfins_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)) | (insert >> offset));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))) | MASK_OUT_ABOVE_8(insert << (8 - offset)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(insert);
/*TODO*///	CPU_NOT_Z = insert;
/*TODO*///	USE_CLKS(17+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfins_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)) | (insert >> offset));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))) | MASK_OUT_ABOVE_8(insert << (8 - offset)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(insert);
/*TODO*///	CPU_NOT_Z = insert;
/*TODO*///	USE_CLKS(17+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfins_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)) | (insert >> offset));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))) | MASK_OUT_ABOVE_8(insert << (8 - offset)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(insert);
/*TODO*///	CPU_NOT_Z = insert;
/*TODO*///	USE_CLKS(17+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfset_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	DY |= mask;
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfset_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) | (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) | (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfset_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) | (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) | (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfset_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) | (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) | (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfset_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) | (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) | (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bfset_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));
/*TODO*///
/*TODO*///	m68ki_write_32(base, (m68ki_read_32(base) | (mask >> offset)));
/*TODO*///	if ((width + offset) > 32)
/*TODO*///		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) | (mask << (8 - offset))));
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_d(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ROL_32(DY, offset) >> (32 - width);
/*TODO*///
/*TODO*///	/* if offset + width > 32, wraps around in register */
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_ai(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_di(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_DI + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_ix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_IX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_aw(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AW + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_al(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_AL + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_pcdi(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint base = ea + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bftst_pcix(void)
/*TODO*///{
/*TODO*///	uint word2 = m68ki_read_imm_16();
/*TODO*///	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
/*TODO*///	uint base = EA_PCIX + (full_offset >> 3);
/*TODO*///	uint offset = full_offset & 7;
/*TODO*///	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
/*TODO*///	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
/*TODO*///																															 - width);
/*TODO*///
/*TODO*///	CPU_N = (data >> (width - 1)) & 1;
/*TODO*///	CPU_NOT_Z = data;
/*TODO*///	USE_CLKS(13+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_bkpt(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_bkpt_ack(CPU_MODE & CPU_MODE_EC020_PLUS ? CPU_IR & 7 : 0);	/* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(11);
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bra_8(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///	USE_CLKS(10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bra_16(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///	USE_CLKS(10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bra_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint mask = 1 << (DX & 0x1f);
/*TODO*///
/*TODO*///	CPU_NOT_Z = *d_dst & mask;
/*TODO*///	*d_dst |= mask;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_pi7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_pd7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_r_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint mask = 1 << (DX & 7);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 0x1f);
/*TODO*///
/*TODO*///	CPU_NOT_Z = *d_dst & mask;
/*TODO*///	*d_dst |= mask;
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_ai(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_pi(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_pi7(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_pd(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_pd7(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_di(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_ix(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_aw(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bset_s_al(void)
/*TODO*///{
/*TODO*///	uint mask = 1 << (m68ki_read_imm_8() & 7);
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = src & mask;
/*TODO*///	m68ki_write_8(ea, src | mask);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bsr_8(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC);
/*TODO*///	m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_bsr_16(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_push_32(CPU_PC + 2);
/*TODO*///	m68ki_branch_word(m68ki_read_16(CPU_PC));
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_bsr_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_push_32(CPU_PC + 4);
/*TODO*///		m68ki_branch_dword(m68ki_read_32(CPU_PC));
/*TODO*///		USE_CLKS(7);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_d(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = DY & (1 << (DX & 0x1f));
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_ai(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_AI) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_pi(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PI_8) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_pi7(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PI7_8) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_pd(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PD_8) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_pd7(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PD7_8) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_di(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_DI) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_ix(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_IX) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_aw(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_AW) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_al(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_AL) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	CPU_NOT_Z = m68ki_read_8(ea) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_pcix(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PCIX) & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_r_i(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = m68ki_read_imm_8() & (1 << (DX & 7));
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_d(void)
/*TODO*///{
/*TODO*///	CPU_NOT_Z = DY & (1 << (m68ki_read_imm_8() & 0x1f));
/*TODO*///	USE_CLKS(10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_ai(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_AI) & (1 << bit);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_pi(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PI_8) & (1 << bit);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_pi7(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PI7_8) & (1 << bit);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_pd(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PD_8) & (1 << bit);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_pd7(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PD7_8) & (1 << bit);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_di(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_DI) & (1 << bit);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_ix(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_IX) & (1 << bit);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_aw(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_AW) & (1 << bit);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_al(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_AL) & (1 << bit);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_pcdi(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	CPU_NOT_Z = m68ki_read_8(ea) & (1 << bit);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_btst_s_pcix(void)
/*TODO*///{
/*TODO*///	uint bit = m68ki_read_imm_8() & 7;
/*TODO*///
/*TODO*///	CPU_NOT_Z = m68ki_read_8(EA_PCIX) & (1 << bit);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_ai(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint ea = EA_AI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_di(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint ea = EA_DI;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_ix(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint ea = EA_IX;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_aw(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint ea = EA_AW;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_al(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint ea = EA_AL;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_pcdi(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_callm_pcix(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		uint ea = EA_PCIX;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_PC += 2;
/*TODO*///(void)ea;	/* just to avoid an 'unused variable' warning */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(30+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_ai_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AI;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pi_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pi7_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI7_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pd_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pd7_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD7_8;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_di_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_DI;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_ix_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_IX;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_aw_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AW;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_al_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AL;
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_ai_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AI;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI_16;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pd_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD_16;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_di_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_DI;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_ix_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_IX;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_aw_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AW;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_al_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AL;
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
/*TODO*///		else
/*TODO*///			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_ai_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AI;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PI_32;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_pd_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_PD_32;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_di_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_DI;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_ix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_IX;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_aw_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AW;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas_al_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint ea = EA_AL;
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint *d_src = &CPU_D[word2 & 7];
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - *d_src);
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(*d_src, dst, res);
/*TODO*///
/*TODO*///		if (CPU_NOT_Z)
/*TODO*///			*d_src = dst;
/*TODO*///		else
/*TODO*///			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
/*TODO*///		USE_CLKS(15+16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas2_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_32();
/*TODO*///		uint *r_src1 = &CPU_D[(word2 >> 16) & 7];
/*TODO*///		uint ea1 = m68k_cpu_dar[word2 >> 31][(word2 >> 28) & 7];
/*TODO*///		uint dst1 = m68ki_read_16(ea1);
/*TODO*///		uint res1 = MASK_OUT_ABOVE_16(dst1 - *r_src1);
/*TODO*///		uint *r_src2 = &CPU_D[word2 & 7];
/*TODO*///		uint ea2 = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint dst2 = m68ki_read_16(ea2);
/*TODO*///		uint res2;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_16(res1);
/*TODO*///		CPU_NOT_Z = res1;
/*TODO*///		CPU_V = VFLAG_SUB_16(*r_src1, dst1, res1);
/*TODO*///		CPU_C = CFLAG_SUB_16(*r_src1, dst1, res1);
/*TODO*///
/*TODO*///		if (!CPU_NOT_Z)
/*TODO*///		{
/*TODO*///			res2 = MASK_OUT_ABOVE_16(dst2 - *r_src2);
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_16(res2);
/*TODO*///			CPU_NOT_Z = res2;
/*TODO*///			CPU_V = VFLAG_SUB_16(*r_src2, dst2, res2);
/*TODO*///			CPU_C = CFLAG_SUB_16(*r_src2, dst2, res2);
/*TODO*///
/*TODO*///			if (!CPU_NOT_Z)
/*TODO*///			{
/*TODO*///				m68ki_write_16(ea1, CPU_D[(word2 >> 22) & 7]);
/*TODO*///				m68ki_write_16(ea2, CPU_D[(word2 >> 6) & 7]);
/*TODO*///				USE_CLKS(22);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		*r_src1 = BIT_1F(word2) ? MAKE_INT_16(dst1) : MASK_OUT_ABOVE_16(*r_src1) | dst1;
/*TODO*///		*r_src2 = BIT_F(word2) ? MAKE_INT_16(dst2) : MASK_OUT_ABOVE_16(*r_src2) | dst2;
/*TODO*///		USE_CLKS(25);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cas2_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_32();
/*TODO*///		uint *r_src1 = &CPU_D[(word2 >> 16) & 7];
/*TODO*///		uint ea1 = m68k_cpu_dar[word2 >> 31][(word2 >> 28) & 7];
/*TODO*///		uint dst1 = m68ki_read_32(ea1);
/*TODO*///		uint res1 = MASK_OUT_ABOVE_32(dst1 - *r_src1);
/*TODO*///		uint *r_src2 = &CPU_D[word2 & 7];
/*TODO*///		uint ea2 = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint dst2 = m68ki_read_32(ea2);
/*TODO*///		uint res2;
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_N = GET_MSB_32(res1);
/*TODO*///		CPU_NOT_Z = res1;
/*TODO*///		CPU_V = VFLAG_SUB_32(*r_src1, dst1, res1);
/*TODO*///		CPU_C = CFLAG_SUB_32(*r_src1, dst1, res1);
/*TODO*///
/*TODO*///		if (!CPU_NOT_Z)
/*TODO*///		{
/*TODO*///			res2 = MASK_OUT_ABOVE_32(dst2 - *r_src2);
/*TODO*///
/*TODO*///			CPU_N = GET_MSB_32(res2);
/*TODO*///			CPU_NOT_Z = res2;
/*TODO*///			CPU_V = VFLAG_SUB_32(*r_src2, dst2, res2);
/*TODO*///			CPU_C = CFLAG_SUB_32(*r_src2, dst2, res2);
/*TODO*///
/*TODO*///			if (!CPU_NOT_Z)
/*TODO*///			{
/*TODO*///				m68ki_write_32(ea1, CPU_D[(word2 >> 22) & 7]);
/*TODO*///				m68ki_write_32(ea2, CPU_D[(word2 >> 6) & 7]);
/*TODO*///				USE_CLKS(22);
/*TODO*///				return;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		*r_src1 = dst1;
/*TODO*///		*r_src2 = dst2;
/*TODO*///		USE_CLKS(25);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_d_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(DY);
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_ai_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_AI));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_pi_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_PI_16));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_pd_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_PD_16));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_di_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_DI));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_ix_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_IX));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_aw_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_AW));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_al_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_AL));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_pcdi_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(ea));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_pcix_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_16(EA_PCIX));
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_chk_i_16(void)
/*TODO*///{
/*TODO*///	int src = MAKE_INT_16(DX);
/*TODO*///	int bound = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///
/*TODO*///	if (src >= 0 && src <= bound)
/*TODO*///	{
/*TODO*///		USE_CLKS(10+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	CPU_N = src < 0;
/*TODO*///	m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_d_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = DY;
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_ai_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_AI);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_pi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_PI_16);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_pd_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_PD_16);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+6);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_di_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_DI);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_ix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_IX);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_aw_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_AW);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_al_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_AL);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+12);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_pcdi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		int bound = m68ki_read_32(ea);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+8);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_pcix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_32(EA_PCIX);
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+10);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk_i_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		int src = DX;
/*TODO*///		int bound = m68ki_read_imm_16();
/*TODO*///
/*TODO*///		if (src >= 0 && src <= bound)
/*TODO*///		{
/*TODO*///			USE_CLKS(8+4);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_N = src < 0;
/*TODO*///		m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_ai_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AI;
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_di_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_DI;
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_ix_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_IX;
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_aw_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AW;
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_al_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AL;
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_pcdi_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_pcix_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_PCIX;
/*TODO*///		uint lower_bound = m68ki_read_8(ea);
/*TODO*///		uint upper_bound = m68ki_read_8(ea + 1);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_8(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_8(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_8(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_ai_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AI;
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_di_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_DI;
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_ix_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_IX;
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_aw_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AW;
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_al_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AL;
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_pcdi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_pcix_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_PCIX;
/*TODO*///		uint lower_bound = m68ki_read_16(ea);
/*TODO*///		uint upper_bound = m68ki_read_16(ea + 2);
/*TODO*///
/*TODO*///		if (BIT_F(word2))
/*TODO*///		{
/*TODO*///			lower_bound = MAKE_INT_16(lower_bound);
/*TODO*///			upper_bound = MAKE_INT_16(upper_bound);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			src = MASK_OUT_ABOVE_16(src);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+10);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_ai_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AI;
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_di_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_DI;
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_ix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_IX;
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_aw_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AW;
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_al_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_AL;
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_pcdi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+12);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_chk2_cmp2_pcix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint word2 = m68ki_read_imm_16();
/*TODO*///		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
/*TODO*///		uint ea = EA_PCIX;
/*TODO*///		uint lower_bound = m68ki_read_32(ea);
/*TODO*///		uint upper_bound = m68ki_read_32(ea + 4);
/*TODO*///
/*TODO*///		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
/*TODO*///		CPU_C = src < lower_bound || src > upper_bound;
/*TODO*///
/*TODO*///		if (CPU_C && BIT_B(word2))	   /* chk2 */
/*TODO*///			m68ki_interrupt(EXCEPTION_CHK);
/*TODO*///
/*TODO*///		USE_CLKS(18+14);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_d_8(void)
/*TODO*///{
/*TODO*///	DY &= 0xffffff00;
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_ai_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pi_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pi7_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pd_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pd7_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_di_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_ix_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_aw_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_al_8(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_d_16(void)
/*TODO*///{
/*TODO*///	DY &= 0xffff0000;
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_ai_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_AI, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pi_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_PI_16, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pd_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_PD_16, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_di_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_DI, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_ix_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_IX, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_aw_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_AW, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_al_16(void)
/*TODO*///{
/*TODO*///	m68ki_write_16(EA_AL, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_d_32(void)
/*TODO*///{
/*TODO*///	DY = 0;
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_ai_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_AI, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pi_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_PI_32, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_pd_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_PD_32, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_di_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_DI, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_ix_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_IX, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_aw_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_AW, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_clr_al_32(void)
/*TODO*///{
/*TODO*///	m68ki_write_32(EA_AL, 0);
/*TODO*///
/*TODO*///	CPU_N = CPU_V = CPU_C = 0;
/*TODO*///	CPU_NOT_Z = 0;
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_d_8(void)
/*TODO*///{
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_ai_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_AI);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pi_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_PI_8);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pi7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pd_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_PD_8);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pd7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_di_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_DI);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_ix_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_IX);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_aw_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_AW);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_al_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_AL);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pcix_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(EA_PCIX);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_i_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_d_16(void)
/*TODO*///{
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_a_16(void)
/*TODO*///{
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_AI);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_PI_16);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_PD_16);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_di_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_DI);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_IX);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_AW);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_al_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_AL);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pcix_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(EA_PCIX);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_i_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_d_32(void)
/*TODO*///{
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_a_32(void)
/*TODO*///{
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_AI);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_PI_32);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_PD_32);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_di_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_DI);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_IX);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_AW);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_al_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_AL);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_32(ea);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_pcix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_PCIX);
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmp_i_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = DX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_d_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(DY);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_a_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(AY);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_AI));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_PI_16));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_PD_16));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_di_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_DI));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_IX));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_AW));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_al_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_AL));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(ea));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pcix_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_16(EA_PCIX));
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_i_16(void)
/*TODO*///{
/*TODO*///	uint src = MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_d_32(void)
/*TODO*///{
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_a_32(void)
/*TODO*///{
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_AI);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_PI_32);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_PD_32);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_di_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_DI);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_IX);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_AW);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_al_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_AL);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_32(ea);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_pcix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(EA_PCIX);
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpa_i_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = AX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_d_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = DY;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_ai_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_AI);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pi_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_PI_8);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pi7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pd_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_PD_8);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pd7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_di_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_DI);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_ix_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_IX);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_aw_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_AW);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_al_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = m68ki_read_8(EA_AL);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cmpi_pcdi_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_imm_8();
/*TODO*///		uint old_pc = (CPU_PC += 2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint dst = m68ki_read_8(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cmpi_pcix_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_imm_8();
/*TODO*///		uint dst = m68ki_read_8(EA_PCIX);
/*TODO*///		uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_d_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = DY;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_AI);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_PI_16);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_PD_16);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_di_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_DI);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_IX);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_AW);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_al_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = m68ki_read_16(EA_AL);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cmpi_pcdi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_imm_16();
/*TODO*///		uint old_pc = (CPU_PC += 2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint dst = m68ki_read_16(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cmpi_pcix_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_imm_16();
/*TODO*///		uint dst = m68ki_read_16(EA_PCIX);
/*TODO*///		uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_d_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = DY;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_AI);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_PI_32);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_PD_32);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_di_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_DI);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_IX);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_AW);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpi_al_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = m68ki_read_32(EA_AL);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cmpi_pcdi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_imm_32();
/*TODO*///		uint old_pc = (CPU_PC += 2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint dst = m68ki_read_32(ea);
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cmpi_pcix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_imm_32();
/*TODO*///		uint dst = m68ki_read_32(EA_PCIX);
/*TODO*///		uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///		CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpm_8_ax7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(AY++);
/*TODO*///	uint dst = m68ki_read_8((CPU_A[7] += 2) - 2);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpm_8_ay7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8((CPU_A[7] += 2) - 2);
/*TODO*///	uint dst = m68ki_read_8(AX++);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpm_8_axy7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8((CPU_A[7] += 2) - 2);
/*TODO*///	uint dst = m68ki_read_8((CPU_A[7] += 2) - 2);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpm_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(AY++);
/*TODO*///	uint dst = m68ki_read_8(AX++);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpm_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16((AY += 2) - 2);
/*TODO*///	uint dst = m68ki_read_16((AX += 2) - 2);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_cmpm_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32((AY += 4) - 4);
/*TODO*///	uint dst = m68ki_read_32((AX += 4) - 4);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cpbcc(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_1111();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cpdbcc(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_1111();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cpgen(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_1111();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cpscc(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_1111();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_cptrapcc(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_1111();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
}