void m68000_1010(void)
{
	m68ki_exception(EXCEPTION_1010);
	M68K_DO_LOG_EMU((M68K_LOG, "%s at %08x: called 1010 instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
}


void m68000_1111(void)
{
	m68ki_exception(EXCEPTION_1111);
	M68K_DO_LOG_EMU((M68K_LOG, "%s at %08x: called 1111 instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
}


void m68000_abcd_rr(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);

	if (res > 9)
		res += 6;
	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res -= 0xa0;

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | MASK_OUT_ABOVE_8(res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(6);
}


void m68000_abcd_mm_ax7(void)
{
	uint src = m68ki_read_8(--AY);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);

	if (res > 9)
		res += 6;
	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res -= 0xa0;

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_abcd_mm_ay7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);

	if (res > 9)
		res += 6;
	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res -= 0xa0;

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_abcd_mm_axy7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);

	if (res > 9)
		res += 6;
	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res -= 0xa0;

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_abcd_mm(void)
{
	uint src = m68ki_read_8(--AY);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(src) + LOW_NIBBLE(dst) + (CPU_X != 0);

	if (res > 9)
		res += 6;
	res += HIGH_NIBBLE(src) + HIGH_NIBBLE(dst);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res -= 0xa0;

	CPU_N = GET_MSB_8(res); /* officially undefined */

	m68ki_write_8(ea, res);

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_add_er_d_8(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(src + dst);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_add_er_8(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_8(m68ki_get_ea_8());
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(src + dst);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_add_er_d_16(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(src + dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_add_er_a_16(void)
{
	uint *d_dst = &DX;
	uint src = AY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(src + dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_add_er_16(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_16(m68ki_get_ea_16());
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(src + dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_add_er_d_32(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_add_er_a_32(void)
{
	uint *d_dst = &DX;
	uint src = AY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_add_er_32(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_32(m68ki_get_ea_32());
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_add_re_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint src = DX;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_add_re_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = DX;
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(src + dst);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_add_re_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint src = DX;
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(src + dst);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(12);
}


void m68000_adda_d_16(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(DY));
	USE_CLKS(8);
}


void m68000_adda_a_16(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(AY));
	USE_CLKS(8);
}


void m68000_adda_16(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16())));
	USE_CLKS(8);
}


void m68000_adda_d_32(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + DY);
	USE_CLKS(8);
}


void m68000_adda_a_32(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + AY);
	USE_CLKS(8);
}


void m68000_adda_32(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + m68ki_read_32(m68ki_get_ea_32()));
	USE_CLKS(6);
}


void m68000_addi_d_8(void)
{
	uint *d_dst = &DY;
	uint src = m68ki_read_imm_8();
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(src + dst);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_addi_8(void)
{
	uint src = m68ki_read_imm_8();
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(12);
}


void m68000_addi_d_16(void)
{
	uint *d_dst = &DY;
	uint src = m68ki_read_imm_16();
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(src + dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_addi_16(void)
{
	uint src = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(src + dst);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(12);
}


void m68000_addi_d_32(void)
{
	uint *d_dst = &DY;
	uint src = m68ki_read_imm_32();
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(16);
}


void m68000_addi_32(void)
{
	uint src = m68ki_read_imm_32();
	uint ea = m68ki_get_ea_32();
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(src + dst);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(20);
}


void m68000_addq_d_8(void)
{
	uint *d_dst = &DY;
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(src + dst);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_addq_8(void)
{
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_addq_d_16(void)
{
	uint *d_dst = &DY;
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(src + dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_addq_a_16(void)
{
	uint *a_dst = &AY;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + (((CPU_IR >> 9) - 1) & 7) + 1);
	USE_CLKS(4);
}


void m68000_addq_16(void)
{
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint ea = m68ki_get_ea_16();
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(src + dst);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_addq_d_32(void)
{
	uint *d_dst = &DY;
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_addq_a_32(void)
{
	uint *a_dst = &AY;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst + (((CPU_IR >> 9) - 1) & 7) + 1);
	USE_CLKS(8);
}


void m68000_addq_32(void)
{
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint ea = m68ki_get_ea_32();
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(src + dst);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(12);
}


void m68000_addx_rr_8(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_addx_rr_16(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(src + dst + (CPU_X != 0));

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_addx_rr_32(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(src + dst + (CPU_X != 0));

	CPU_N = GET_MSB_32(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_addx_mm_8_ax7(void)
{
	uint src = m68ki_read_8(--AY);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_addx_mm_8_ay7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_addx_mm_8_axy7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_addx_mm_8(void)
{
	uint src = m68ki_read_8(--AY);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(src + dst + (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_8(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_addx_mm_16(void)
{
	uint src = m68ki_read_16(AY -= 2);
	uint ea = (AX -= 2);
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(src + dst + (CPU_X != 0));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_16(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_16(src, dst, res);
	USE_CLKS(18);
}


void m68000_addx_mm_32(void)
{
	uint src = m68ki_read_32(AY -= 4);
	uint ea = (AX -= 4);
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(src + dst + (CPU_X != 0));

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = VFLAG_ADD_32(src, dst, res);
	CPU_X = CPU_C = CFLAG_ADD_32(src, dst, res);
	USE_CLKS(30);
}


void m68000_and_er_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DX &= (DY | 0xffffff00));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_and_er_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DX &= (m68ki_read_8(m68ki_get_ea_8()) | 0xffffff00));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_and_er_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DX &= (DY | 0xffff0000));

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_and_er_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DX &= (m68ki_read_16(m68ki_get_ea_16()) | 0xffff0000));

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_and_er_d_32(void)
{
	uint res = DX &= DY;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_and_er_32(void)
{
	uint res = DX &= m68ki_read_32(m68ki_get_ea_32());

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(6);
}


void m68000_and_re_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint res = MASK_OUT_ABOVE_8(DX & m68ki_read_8(ea));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_and_re_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint res = MASK_OUT_ABOVE_16(DX & m68ki_read_16(ea));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_and_re_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint res = DX & m68ki_read_32(ea);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_andi_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY &= (m68ki_read_imm_8() | 0xffffff00));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_andi_8(void)
{
	uint tmp = m68ki_read_imm_8();
	uint ea = m68ki_get_ea_8();
	uint res = tmp & m68ki_read_8(ea);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_andi_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY &= (m68ki_read_imm_16() | 0xffff0000));

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_andi_16(void)
{
	uint tmp = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint res = tmp & m68ki_read_16(ea);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_andi_d_32(void)
{
	uint res = DY &= (m68ki_read_imm_32());

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(14);
}


void m68000_andi_32(void)
{
	uint tmp = m68ki_read_imm_32();
	uint ea = m68ki_get_ea_32();
	uint res = tmp & m68ki_read_32(ea);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(20);
}


void m68000_andi_to_ccr(void)
{
	m68ki_set_ccr(m68ki_get_ccr() & m68ki_read_imm_16());
	USE_CLKS(20);
}


void m68000_andi_to_sr(void)
{
	uint and_val = m68ki_read_imm_16();

	if (CPU_S)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_set_sr(m68ki_get_sr() & and_val);
		USE_CLKS(20);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_asr_s_8(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = src >> shift;

	if (GET_MSB_8(src))
		res |= m68k_shift_8_table[shift];

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
	CPU_X = CPU_C = shift > 7 ? CPU_N : (src >> (shift - 1)) & 1;
	USE_CLKS((shift << 1) + 6);
}


void m68000_asr_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = src >> shift;

	if (GET_MSB_16(src))
		res |= m68k_shift_16_table[shift];

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
	USE_CLKS((shift << 1) + 6);
}


void m68000_asr_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = src >> shift;

	if (GET_MSB_32(src))
		res |= m68k_shift_32_table[shift];

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
	USE_CLKS((shift << 1) + 8);
}


void m68000_asr_r_8(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = src >> shift;

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift < 8)
		{
			if (GET_MSB_8(src))
				res |= m68k_shift_8_table[shift];

			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

			CPU_C = CPU_X = (src >> (shift - 1)) & 1;
			CPU_N = GET_MSB_8(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		if (GET_MSB_8(src))
		{
			*d_dst |= 0xff;
			CPU_C = CPU_X = 1;
			CPU_N = 1;
			CPU_NOT_Z = 1;
			CPU_V = 0;
			return;
		}

		*d_dst &= 0xffffff00;
		CPU_C = CPU_X = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_asr_r_16(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = src >> shift;

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift < 16)
		{
			if (GET_MSB_16(src))
				res |= m68k_shift_16_table[shift];

			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

			CPU_C = CPU_X = (src >> (shift - 1)) & 1;
			CPU_N = GET_MSB_16(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		if (GET_MSB_16(src))
		{
			*d_dst |= 0xffff;
			CPU_C = CPU_X = 1;
			CPU_N = 1;
			CPU_NOT_Z = 1;
			CPU_V = 0;
			return;
		}

		*d_dst &= 0xffff0000;
		CPU_C = CPU_X = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_asr_r_32(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = src >> shift;

	USE_CLKS((shift << 1) + 8);
	if (shift != 0)
	{
		if (shift < 32)
		{
			if (GET_MSB_32(src))
				res |= m68k_shift_32_table[shift];

			*d_dst = res;

			CPU_C = CPU_X = (src >> (shift - 1)) & 1;
			CPU_N = GET_MSB_32(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		if (GET_MSB_32(src))
		{
			*d_dst = 0xffffffff;
			CPU_C = CPU_X = 1;
			CPU_N = 1;
			CPU_NOT_Z = 1;
			CPU_V = 0;
			return;
		}

		*d_dst = 0;
		CPU_C = CPU_X = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_asr_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint res = src >> 1;

	if (GET_MSB_16(src))
		res |= 0x8000;

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
	CPU_C = CPU_X = src & 1;
	USE_CLKS(8);
}


void m68000_asl_s_8(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = MASK_OUT_ABOVE_8(src << shift);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_X = CPU_C = (src >> (8 - shift)) & 1;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	src &= m68k_shift_8_table[shift + 1];
	CPU_V = !(src == 0 || (src == m68k_shift_8_table[shift + 1] && shift < 8));

	USE_CLKS((shift << 1) + 6);
}


void m68000_asl_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = MASK_OUT_ABOVE_16(src << shift);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = (src >> (16 - shift)) & 1;
	src &= m68k_shift_16_table[shift + 1];
	CPU_V = !(src == 0 || src == m68k_shift_16_table[shift + 1]);

	USE_CLKS((shift << 1) + 6);
}


void m68000_asl_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = *d_dst;
	uint res = MASK_OUT_ABOVE_32(src << shift);

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = (src >> (32 - shift)) & 1;
	src &= m68k_shift_32_table[shift + 1];
	CPU_V = !(src == 0 || src == m68k_shift_32_table[shift + 1]);

	USE_CLKS((shift << 1) + 8);
}


void m68000_asl_r_8(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = MASK_OUT_ABOVE_8(src << shift);

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift < 8)
		{
			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
			CPU_X = CPU_C = (src >> (8 - shift)) & 1;
			CPU_N = GET_MSB_8(res);
			CPU_NOT_Z = res;
			src &= m68k_shift_8_table[shift + 1];
			CPU_V = !(src == 0 || src == m68k_shift_8_table[shift + 1]);
			return;
		}

		*d_dst &= 0xffffff00;
		CPU_X = CPU_C = (shift == 8 ? src & 1 : 0);
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = !(src == 0);
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_asl_r_16(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = MASK_OUT_ABOVE_16(src << shift);

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift < 16)
		{
			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
			CPU_X = CPU_C = (src >> (16 - shift)) & 1;
			CPU_N = GET_MSB_16(res);
			CPU_NOT_Z = res;
			src &= m68k_shift_16_table[shift + 1];
			CPU_V = !(src == 0 || src == m68k_shift_16_table[shift + 1]);
			return;
		}

		*d_dst &= 0xffff0000;
		CPU_X = CPU_C = (shift == 16 ? src & 1 : 0);
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = !(src == 0);
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_asl_r_32(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = *d_dst;
	uint res = MASK_OUT_ABOVE_32(src << shift);

	USE_CLKS((shift << 1) + 8);
	if (shift != 0)
	{
		if (shift < 32)
		{
			*d_dst = res;
			CPU_X = CPU_C = (src >> (32 - shift)) & 1;
			CPU_N = GET_MSB_32(res);
			CPU_NOT_Z = res;
			src &= m68k_shift_32_table[shift + 1];
			CPU_V = !(src == 0 || src == m68k_shift_32_table[shift + 1]);
			return;
		}

		*d_dst = 0;
		CPU_X = CPU_C = (shift == 32 ? src & 1 : 0);
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = !(src == 0);
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_asl_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(src << 1);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = GET_MSB_16(src);
	src &= 0xc000;
	CPU_V = !(src == 0 || src == 0xc000);
	USE_CLKS(8);
}


void m68000_bhi_8(void)
{
	if (CONDITION_HI)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bhi_16(void)
{
	if (CONDITION_HI)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bhi_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_HI)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bls_8(void)
{
	if (CONDITION_LS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bls_16(void)
{
	if (CONDITION_LS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bls_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LS)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bcc_8(void)
{
	if (CONDITION_CC)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bcc_16(void)
{
	if (CONDITION_CC)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bcc_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CC)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bcs_8(void)
{
	if (CONDITION_CS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bcs_16(void)
{
	if (CONDITION_CS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bcs_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CS)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bne_8(void)
{
	if (CONDITION_NE)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bne_16(void)
{
	if (CONDITION_NE)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bne_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_NE)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_beq_8(void)
{
	if (CONDITION_EQ)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_beq_16(void)
{
	if (CONDITION_EQ)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_beq_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_EQ)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bvc_8(void)
{
	if (CONDITION_VC)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bvc_16(void)
{
	if (CONDITION_VC)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bvc_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VC)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bvs_8(void)
{
	if (CONDITION_VS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bvs_16(void)
{
	if (CONDITION_VS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bvs_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VS)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bpl_8(void)
{
	if (CONDITION_PL)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bpl_16(void)
{
	if (CONDITION_PL)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bpl_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_PL)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bmi_8(void)
{
	if (CONDITION_MI)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bmi_16(void)
{
	if (CONDITION_MI)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bmi_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_MI)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bge_8(void)
{
	if (CONDITION_GE)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bge_16(void)
{
	if (CONDITION_GE)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bge_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GE)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_blt_8(void)
{
	if (CONDITION_LT)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_blt_16(void)
{
	if (CONDITION_LT)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_blt_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LT)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bgt_8(void)
{
	if (CONDITION_GT)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_bgt_16(void)
{
	if (CONDITION_GT)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_bgt_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GT)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_ble_8(void)
{
	if (CONDITION_LE)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
		USE_CLKS(10);
		return;
	}
	USE_CLKS(8);
}


void m68000_ble_16(void)
{
	if (CONDITION_LE)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68020_ble_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LE)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_dword(m68ki_read_32(CPU_PC));
			USE_CLKS(6);
			return;
		}
		CPU_PC += 4;
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bchg_r_d(void)
{
	uint *d_dst = &DY;
	uint mask = 1 << (DX & 0x1f);

	CPU_NOT_Z = *d_dst & mask;
	*d_dst ^= mask;
	USE_CLKS(8);
}


void m68000_bchg_r(void)
{
	uint ea = m68ki_get_ea_8();
	uint src = m68ki_read_8(ea);
	uint mask = 1 << (DX & 7);

	CPU_NOT_Z = src & mask;
	m68ki_write_8(ea, src ^ mask);
	USE_CLKS(8);
}


void m68000_bchg_s_d(void)
{
	uint *d_dst = &DY;
	uint mask = 1 << (m68ki_read_imm_8() & 0x1f);

	CPU_NOT_Z = *d_dst & mask;
	*d_dst ^= mask;
	USE_CLKS(12);
}


void m68000_bchg_s(void)
{
	uint mask = 1 << (m68ki_read_imm_8() & 7);
	uint ea = m68ki_get_ea_8();
	uint src = m68ki_read_8(ea);

	CPU_NOT_Z = src & mask;
	m68ki_write_8(ea, src ^ mask);
	USE_CLKS(12);
}


void m68000_bclr_r_d(void)
{
	uint *d_dst = &DY;
	uint mask = 1 << (DX & 0x1f);

	CPU_NOT_Z = *d_dst & mask;
	*d_dst &= ~mask;
	USE_CLKS(10);
}


void m68000_bclr_r(void)
{
	uint ea = m68ki_get_ea_8();
	uint src = m68ki_read_8(ea);
	uint mask = 1 << (DX & 7);

	CPU_NOT_Z = src & mask;
	m68ki_write_8(ea, src & ~mask);
	USE_CLKS(8);
}


void m68000_bclr_s_d(void)
{
	uint *d_dst = &DY;
	uint mask = 1 << (m68ki_read_imm_8() & 0x1f);

	CPU_NOT_Z = *d_dst & mask;
	*d_dst &= ~mask;
	USE_CLKS(14);
}


void m68000_bclr_s(void)
{
	uint mask = 1 << (m68ki_read_imm_8() & 7);
	uint ea = m68ki_get_ea_8();
	uint src = m68ki_read_8(ea);

	CPU_NOT_Z = src & mask;
	m68ki_write_8(ea, src & ~mask);
	USE_CLKS(12);
}


void m68020_bfchg_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	DY ^= mask;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(12);
}


void m68020_bfchg(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	m68ki_write_32(base, (m68ki_read_32(base) ^ (mask >> offset)));
	if ((width + offset) > 32)
		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) ^ (mask << (8 - offset))));

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(20);
}


void m68020_bfclr_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	DY &= ~mask;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(12);
}


void m68020_bfclr(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)));
	if ((width + offset) > 32)
		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))));

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(20);
}


void m68020_bfexts_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);

	if ((CPU_N = (data >> (width - 1)) & 1))
		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
	CPU_NOT_Z = data;

	CPU_D[(word2 >> 12) & 7] = data;
	USE_CLKS(8);
}

void m68020_bfexts(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);

	if ((CPU_N = (data >> (width - 1)) & 1))
		data |= MASK_OUT_ABOVE_32(0xffffffff << width);
	CPU_NOT_Z = data;

	CPU_D[(word2 >> 12) & 7] = data;
	USE_CLKS(15);
}


void m68020_bfextu_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);

	CPU_D[(word2 >> 12) & 7] = data;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(8);
}

void m68020_bfextu(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);

	CPU_D[(word2 >> 12) & 7] = data;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(15);
}


void m68020_bfffo_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);
	uint mask = 1 << (width - 1);

	for (; mask && !(data & mask); mask >>= 1, offset++)
		;
	CPU_D[(word2 >> 12) & 7] = offset;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(18);
}

void m68020_bfffo(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);
	uint mask = 1 << (width - 1);

	for (; mask && !(data & mask); mask >>= 1, full_offset++)
		;
	CPU_D[(word2 >> 12) & 7] = full_offset;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(28);
}


void m68020_bfins_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
	uint orig_insert = insert >> (32 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	insert = ROR_32(insert, offset);
	mask = ~ROR_32(mask, offset);

	DY &= mask;
	DY |= insert;

	CPU_N = orig_insert >> (width - 1);
	CPU_NOT_Z = orig_insert;
	USE_CLKS(10);
}


void m68020_bfins(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint insert = MASK_OUT_ABOVE_32(CPU_D[(word2 >> 12) & 7] << (32 - width));
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	m68ki_write_32(base, (m68ki_read_32(base) & ~(mask >> offset)) | (insert >> offset));
	if ((width + offset) > 32)
		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) & ~(mask << (8 - offset))) | MASK_OUT_ABOVE_8(insert << (8 - offset)));

	CPU_N = GET_MSB_32(insert);
	CPU_NOT_Z = insert;
	USE_CLKS(17);
}


void m68020_bfset_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	DY |= mask;

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(12);
}


void m68020_bfset(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);
	uint mask = MASK_OUT_ABOVE_32(0xffffffff << (32 - width));

	m68ki_write_32(base, (m68ki_read_32(base) | (mask >> offset)));
	if ((width + offset) > 32)
		m68ki_write_8(base + 4, (m68ki_read_8(base + 4) | (mask << (8 - offset))));

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(20);
}


void m68020_bftst_d(void)
{
	uint word2 = m68ki_read_imm_16();
	uint offset = (BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : word2 >> 6) & 31;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ROL_32(DY, offset) >> (32 - width);

	/* if offset + width > 32, wraps around in register */

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(6);
}

void m68020_bftst(void)
{
	uint word2 = m68ki_read_imm_16();
	uint full_offset = BIT_B(word2) ? MAKE_INT_32(CPU_D[(word2 >> 6) & 7]) : (word2 >> 6) & 31;
	uint base = m68ki_get_ea_8() + (full_offset >> 3);
	uint offset = full_offset & 7;
	uint width = (((BIT_5(word2) ? CPU_D[word2 & 7] : word2) - 1) & 31) + 1;
	uint data = ((m68ki_read_32(base) << offset) | ((offset + width > 32) ? m68ki_read_8(base + 4) >> (8 - offset) : 0)) >> (32
																															 - width);

	CPU_N = (data >> (width - 1)) & 1;
	CPU_NOT_Z = data;
	USE_CLKS(13);
}


void m68010_bkpt(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		m68ki_bkpt_ack(CPU_MODE & CPU_MODE_EC020_PLUS ? CPU_IR & 7 : 0);	/* auto-disable (see m68kcpu.h) */
		USE_CLKS(11);
	}
	m68000_illegal();
}


void m68000_bra_8(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
	USE_CLKS(10);
}


void m68000_bra_16(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_branch_word(m68ki_read_16(CPU_PC));
	USE_CLKS(10);
}


void m68020_bra_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_dword(m68ki_read_32(CPU_PC));
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68000_bset_r_d(void)
{
	uint *d_dst = &DY;
	uint mask = 1 << (DX & 0x1f);

	CPU_NOT_Z = *d_dst & mask;
	*d_dst |= mask;
	USE_CLKS(8);
}


void m68000_bset_r(void)
{
	uint ea = m68ki_get_ea_8();
	uint src = m68ki_read_8(ea);
	uint mask = 1 << (DX & 7);

	CPU_NOT_Z = src & mask;
	m68ki_write_8(ea, src | mask);
	USE_CLKS(8);
}


void m68000_bset_s_d(void)
{
	uint *d_dst = &DY;
	uint mask = 1 << (m68ki_read_imm_8() & 0x1f);

	CPU_NOT_Z = *d_dst & mask;
	*d_dst |= mask;
	USE_CLKS(12);
}


void m68000_bset_s(void)
{
	uint mask = 1 << (m68ki_read_imm_8() & 7);
	uint ea = m68ki_get_ea_8();
	uint src = m68ki_read_8(ea);

	CPU_NOT_Z = src & mask;
	m68ki_write_8(ea, src | mask);
	USE_CLKS(12);
}


void m68000_bsr_8(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_push_32(CPU_PC);
	m68ki_branch_byte(MASK_OUT_ABOVE_8(CPU_IR));
	USE_CLKS(18);
}


void m68000_bsr_16(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_push_32(CPU_PC + 2);
	m68ki_branch_word(m68ki_read_16(CPU_PC));
	USE_CLKS(18);
}


void m68020_bsr_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_push_32(CPU_PC + 4);
		m68ki_branch_dword(m68ki_read_32(CPU_PC));
		USE_CLKS(7);
		return;
	}
	m68000_illegal();
}


void m68000_btst_r_d(void)
{
	CPU_NOT_Z = DY & (1 << (DX & 0x1f));
	USE_CLKS(6);
}


void m68000_btst_r(void)
{
	CPU_NOT_Z = m68ki_read_8(m68ki_get_ea_8()) & (1 << (DX & 7));
	USE_CLKS(4);
}


void m68000_btst_s_d(void)
{
	CPU_NOT_Z = DY & (1 << (m68ki_read_imm_8() & 0x1f));
	USE_CLKS(10);
}


void m68000_btst_s(void)
{
	uint bit = m68ki_read_imm_8() & 7;

	CPU_NOT_Z = m68ki_read_8(m68ki_get_ea_8()) & (1 << bit);
	USE_CLKS(8);
}


void m68020_callm(void)
{
	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
	{
		uint ea = m68ki_get_ea_32();

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_PC += 2;
(void)ea;	/* just to avoid an 'unused variable' warning */
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		USE_CLKS(30);
		return;
	}
	m68000_illegal();
}


void m68020_cas_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint ea = m68ki_get_ea_8();
		uint dst = m68ki_read_8(ea);
		uint *d_src = &CPU_D[word2 & 7];
		uint res = MASK_OUT_ABOVE_8(dst - *d_src);

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_8(*d_src, dst, res);
		CPU_C = CFLAG_SUB_8(*d_src, dst, res);

		if (CPU_NOT_Z)
			*d_src = MASK_OUT_BELOW_8(*d_src) | dst;
		else
			m68ki_write_8(ea, CPU_D[(word2 >> 6) & 7]);
		USE_CLKS(15);
		return;
	}
	m68000_illegal();
}


void m68020_cas_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint ea = m68ki_get_ea_16();
		uint dst = m68ki_read_16(ea);
		uint *d_src = &CPU_D[word2 & 7];
		uint res = MASK_OUT_ABOVE_16(dst - *d_src);

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_16(*d_src, dst, res);
		CPU_C = CFLAG_SUB_16(*d_src, dst, res);

		if (CPU_NOT_Z)
			*d_src = MASK_OUT_BELOW_16(*d_src) | dst;
		else
			m68ki_write_16(ea, CPU_D[(word2 >> 6) & 7]);
		USE_CLKS(15);
		return;
	}
	m68000_illegal();
}


void m68020_cas_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint ea = m68ki_get_ea_32();
		uint dst = m68ki_read_32(ea);
		uint *d_src = &CPU_D[word2 & 7];
		uint res = MASK_OUT_ABOVE_32(dst - *d_src);

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_32(*d_src, dst, res);
		CPU_C = CFLAG_SUB_32(*d_src, dst, res);

		if (CPU_NOT_Z)
			*d_src = dst;
		else
			m68ki_write_32(ea, CPU_D[(word2 >> 6) & 7]);
		USE_CLKS(15);
		return;
	}
	m68000_illegal();
}


void m68020_cas2_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_32();
		uint *r_src1 = &CPU_D[(word2 >> 16) & 7];
		uint ea1 = m68k_cpu_dar[word2 >> 31][(word2 >> 28) & 7];
		uint dst1 = m68ki_read_16(ea1);
		uint res1 = MASK_OUT_ABOVE_16(dst1 - *r_src1);
		uint *r_src2 = &CPU_D[word2 & 7];
		uint ea2 = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
		uint dst2 = m68ki_read_16(ea2);
		uint res2;

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_N = GET_MSB_16(res1);
		CPU_NOT_Z = res1;
		CPU_V = VFLAG_SUB_16(*r_src1, dst1, res1);
		CPU_C = CFLAG_SUB_16(*r_src1, dst1, res1);

		if (!CPU_NOT_Z)
		{
			res2 = MASK_OUT_ABOVE_16(dst2 - *r_src2);

			CPU_N = GET_MSB_16(res2);
			CPU_NOT_Z = res2;
			CPU_V = VFLAG_SUB_16(*r_src2, dst2, res2);
			CPU_C = CFLAG_SUB_16(*r_src2, dst2, res2);

			if (!CPU_NOT_Z)
			{
				m68ki_write_16(ea1, CPU_D[(word2 >> 22) & 7]);
				m68ki_write_16(ea2, CPU_D[(word2 >> 6) & 7]);
				USE_CLKS(22);
				return;
			}
		}
		*r_src1 = BIT_1F(word2) ? MAKE_INT_16(dst1) : MASK_OUT_ABOVE_16(*r_src1) | dst1;
		*r_src2 = BIT_F(word2) ? MAKE_INT_16(dst2) : MASK_OUT_ABOVE_16(*r_src2) | dst2;
		USE_CLKS(25);
		return;
	}
	m68000_illegal();
}


void m68020_cas2_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_32();
		uint *r_src1 = &CPU_D[(word2 >> 16) & 7];
		uint ea1 = m68k_cpu_dar[word2 >> 31][(word2 >> 28) & 7];
		uint dst1 = m68ki_read_32(ea1);
		uint res1 = MASK_OUT_ABOVE_32(dst1 - *r_src1);
		uint *r_src2 = &CPU_D[word2 & 7];
		uint ea2 = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
		uint dst2 = m68ki_read_32(ea2);
		uint res2;

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_N = GET_MSB_32(res1);
		CPU_NOT_Z = res1;
		CPU_V = VFLAG_SUB_32(*r_src1, dst1, res1);
		CPU_C = CFLAG_SUB_32(*r_src1, dst1, res1);

		if (!CPU_NOT_Z)
		{
			res2 = MASK_OUT_ABOVE_32(dst2 - *r_src2);

			CPU_N = GET_MSB_32(res2);
			CPU_NOT_Z = res2;
			CPU_V = VFLAG_SUB_32(*r_src2, dst2, res2);
			CPU_C = CFLAG_SUB_32(*r_src2, dst2, res2);

			if (!CPU_NOT_Z)
			{
				m68ki_write_32(ea1, CPU_D[(word2 >> 22) & 7]);
				m68ki_write_32(ea2, CPU_D[(word2 >> 6) & 7]);
				USE_CLKS(22);
				return;
			}
		}
		*r_src1 = dst1;
		*r_src2 = dst2;
		USE_CLKS(25);
		return;
	}
	m68000_illegal();
}


void m68000_chk_d_16(void)
{
	int src = MAKE_INT_16(DX);
	int bound = MAKE_INT_16(DY);

	if (src >= 0 && src <= bound)
	{
		USE_CLKS(10);
		return;
	}
	CPU_N = src < 0;
	m68ki_interrupt(EXCEPTION_CHK);
}


void m68000_chk_16(void)
{
	int src = MAKE_INT_16(DX);
	int bound = MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16()));

	if (src >= 0 && src <= bound)
	{
		USE_CLKS(10);
		return;
	}
	CPU_N = src < 0;
	m68ki_interrupt(EXCEPTION_CHK);
}


void m68020_chk_d_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		int src = DX;
		int bound = DY;

		if (src >= 0 && src <= bound)
		{
			USE_CLKS(8);
			return;
		}
		CPU_N = src < 0;
		m68ki_interrupt(EXCEPTION_CHK);
		return;
	}
	m68000_illegal();
}


void m68020_chk_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		int src = DX;
		int bound = m68ki_read_32(m68ki_get_ea_32());

		if (src >= 0 && src <= bound)
		{
			USE_CLKS(8);
			return;
		}
		CPU_N = src < 0;
		m68ki_interrupt(EXCEPTION_CHK);
		return;
	}
	m68000_illegal();
}


void m68020_chk2_cmp2_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
		uint ea = m68ki_get_ea_8();
		uint lower_bound = m68ki_read_8(ea);
		uint upper_bound = m68ki_read_8(ea + 1);

		if (BIT_F(word2))
		{
			lower_bound = MAKE_INT_8(lower_bound);
			upper_bound = MAKE_INT_8(upper_bound);
		}
		else
			src = MASK_OUT_ABOVE_8(src);

		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
		CPU_C = src < lower_bound || src > upper_bound;

		if (CPU_C && BIT_B(word2))	   /* chk2 */
			m68ki_interrupt(EXCEPTION_CHK);

		USE_CLKS(18);
		return;
	}
	m68000_illegal();
}


void m68020_chk2_cmp2_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
		uint ea = m68ki_get_ea_16();
		uint lower_bound = m68ki_read_16(ea);
		uint upper_bound = m68ki_read_16(ea + 2);

		if (BIT_F(word2))
		{
			lower_bound = MAKE_INT_16(lower_bound);
			upper_bound = MAKE_INT_16(upper_bound);
		}
		else
			src = MASK_OUT_ABOVE_16(src);

		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
		CPU_C = src < lower_bound || src > upper_bound;

		if (CPU_C && BIT_B(word2))	   /* chk2 */
			m68ki_interrupt(EXCEPTION_CHK);

		USE_CLKS(18);
		return;
	}
	m68000_illegal();
}


void m68020_chk2_cmp2_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint src = m68k_cpu_dar[word2 >> 15][(word2 >> 12) & 7];
		uint ea = m68ki_get_ea_32();
		uint lower_bound = m68ki_read_32(ea);
		uint upper_bound = m68ki_read_32(ea + 4);

		CPU_NOT_Z = !(src == lower_bound || src == upper_bound);
		CPU_C = src < lower_bound || src > upper_bound;

		if (CPU_C && BIT_B(word2))	   /* chk2 */
			m68ki_interrupt(EXCEPTION_CHK);

		USE_CLKS(18);
		return;
	}
	m68000_illegal();
}


void m68000_clr_d_8(void)
{
	DY &= 0xffffff00;

	CPU_N = CPU_V = CPU_C = 0;
	CPU_NOT_Z = 0;
	USE_CLKS(4);
}


void m68000_clr_8(void)
{
	m68ki_write_8(m68ki_get_ea_8(), 0);

	CPU_N = CPU_V = CPU_C = 0;
	CPU_NOT_Z = 0;
	USE_CLKS(8);
}


void m68000_clr_d_16(void)
{
	DY &= 0xffff0000;

	CPU_N = CPU_V = CPU_C = 0;
	CPU_NOT_Z = 0;
	USE_CLKS(4);
}


void m68000_clr_16(void)
{
	m68ki_write_16(m68ki_get_ea_16(), 0);

	CPU_N = CPU_V = CPU_C = 0;
	CPU_NOT_Z = 0;
	USE_CLKS(8);
}


void m68000_clr_d_32(void)
{
	DY = 0;

	CPU_N = CPU_V = CPU_C = 0;
	CPU_NOT_Z = 0;
	USE_CLKS(6);
}


void m68000_clr_32(void)
{
	m68ki_write_32(m68ki_get_ea_32(), 0);

	CPU_N = CPU_V = CPU_C = 0;
	CPU_NOT_Z = 0;
	USE_CLKS(12);
}


void m68000_cmp_d_8(void)
{
	uint src = DY;
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_cmp_8(void)
{
	uint src = m68ki_read_8(m68ki_get_ea_8());
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_cmp_d_16(void)
{
	uint src = DY;
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_16(src, dst, res);
	CPU_C = CFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_cmp_a_16(void)
{
	uint src = AY;
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_16(src, dst, res);
	CPU_C = CFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_cmp_16(void)
{
	uint src = m68ki_read_16(m68ki_get_ea_16());
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_16(src, dst, res);
	CPU_C = CFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_cmp_d_32(void)
{
	uint src = DY;
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmp_a_32(void)
{
	uint src = AY;
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmp_32(void)
{
	uint src = m68ki_read_32(m68ki_get_ea_32());
	uint dst = DX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpa_d_16(void)
{
	uint src = MAKE_INT_16(DY);
	uint dst = AX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpa_a_16(void)
{
	uint src = MAKE_INT_16(AY);
	uint dst = AX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpa_16(void)
{
	uint src = MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16()));
	uint dst = AX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpa_d_32(void)
{
	uint src = DY;
	uint dst = AX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpa_a_32(void)
{
	uint src = AY;
	uint dst = AX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpa_32(void)
{
	uint src = m68ki_read_32(m68ki_get_ea_32());
	uint dst = AX;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_cmpi_d_8(void)
{
	uint src = m68ki_read_imm_8();
	uint dst = DY;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_cmpi_8(void)
{
	uint src = m68ki_read_imm_8();
	uint dst = m68ki_read_8(m68ki_get_ea_8());
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(8);
}


void m68020_cmpi_pcdi_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_imm_8();
		uint old_pc = (CPU_PC += 2) - 2;
		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
		uint dst = m68ki_read_8(ea);
		uint res = MASK_OUT_ABOVE_8(dst - src);

		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_8(src, dst, res);
		CPU_C = CFLAG_SUB_8(src, dst, res);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68020_cmpi_pcix_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_imm_8();
		uint dst = m68ki_read_8(EA_PCIX);
		uint res = MASK_OUT_ABOVE_8(dst - src);

		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_8(src, dst, res);
		CPU_C = CFLAG_SUB_8(src, dst, res);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68000_cmpi_d_16(void)
{
	uint src = m68ki_read_imm_16();
	uint dst = DY;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_16(src, dst, res);
	CPU_C = CFLAG_SUB_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_cmpi_16(void)
{
	uint src = m68ki_read_imm_16();
	uint dst = m68ki_read_16(m68ki_get_ea_16());
	uint res = MASK_OUT_ABOVE_16(dst - src);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_16(src, dst, res);
	CPU_C = CFLAG_SUB_16(src, dst, res);
	USE_CLKS(8);
}


void m68020_cmpi_pcdi_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_imm_16();
		uint old_pc = (CPU_PC += 2) - 2;
		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
		uint dst = m68ki_read_16(ea);
		uint res = MASK_OUT_ABOVE_16(dst - src);

		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_16(src, dst, res);
		CPU_C = CFLAG_SUB_16(src, dst, res);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68020_cmpi_pcix_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_imm_16();
		uint dst = m68ki_read_16(EA_PCIX);
		uint res = MASK_OUT_ABOVE_16(dst - src);

		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_16(src, dst, res);
		CPU_C = CFLAG_SUB_16(src, dst, res);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68000_cmpi_d_32(void)
{
	uint src = m68ki_read_imm_32();
	uint dst = DY;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(14);
}


void m68000_cmpi_32(void)
{
	uint src = m68ki_read_imm_32();
	uint dst = m68ki_read_32(m68ki_get_ea_32());
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(12);
}


void m68020_cmpi_pcdi_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_imm_32();
		uint old_pc = (CPU_PC += 2) - 2;
		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
		uint dst = m68ki_read_32(ea);
		uint res = MASK_OUT_ABOVE_32(dst - src);

		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_32(src, dst, res);
		CPU_C = CFLAG_SUB_32(src, dst, res);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68020_cmpi_pcix_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_imm_32();
		uint dst = m68ki_read_32(EA_PCIX);
		uint res = MASK_OUT_ABOVE_32(dst - src);

		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = VFLAG_SUB_32(src, dst, res);
		CPU_C = CFLAG_SUB_32(src, dst, res);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68000_cmpm_8_ax7(void)
{
	uint src = m68ki_read_8(AY++);
	uint dst = m68ki_read_8((CPU_A[7] += 2) - 2);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(12);
}


void m68000_cmpm_8_ay7(void)
{
	uint src = m68ki_read_8((CPU_A[7] += 2) - 2);
	uint dst = m68ki_read_8(AX++);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(12);
}


void m68000_cmpm_8_axy7(void)
{
	uint src = m68ki_read_8((CPU_A[7] += 2) - 2);
	uint dst = m68ki_read_8((CPU_A[7] += 2) - 2);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(12);
}


void m68000_cmpm_8(void)
{
	uint src = m68ki_read_8(AY++);
	uint dst = m68ki_read_8(AX++);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_8(src, dst, res);
	CPU_C = CFLAG_SUB_8(src, dst, res);
	USE_CLKS(12);
}


void m68000_cmpm_16(void)
{
	uint src = m68ki_read_16((AY += 2) - 2);
	uint dst = m68ki_read_16((AX += 2) - 2);
	uint res = MASK_OUT_ABOVE_16(dst - src);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_16(src, dst, res);
	CPU_C = CFLAG_SUB_16(src, dst, res);
	USE_CLKS(12);
}


void m68000_cmpm_32(void)
{
	uint src = m68ki_read_32((AY += 4) - 4);
	uint dst = m68ki_read_32((AX += 4) - 4);
	uint res = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = VFLAG_SUB_32(src, dst, res);
	CPU_C = CFLAG_SUB_32(src, dst, res);
	USE_CLKS(20);
}


void m68020_cpbcc(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		return;
	}
	m68000_1111();
}


void m68020_cpdbcc(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		return;
	}
	m68000_1111();
}


void m68020_cpgen(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		return;
	}
	m68000_1111();
}


void m68020_cpscc(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		return;
	}
	m68000_1111();
}


void m68020_cptrapcc(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		return;
	}
	m68000_1111();
}


void m68000_dbt(void)
{
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbf(void)
{
	uint *d_reg = &DY;
	uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

	*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
	if (res != 0xffff)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_branch_word(m68ki_read_16(CPU_PC));
		USE_CLKS(10);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(14);
}


void m68000_dbhi(void)
{
	if (CONDITION_NOT_HI)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbls(void)
{
	if (CONDITION_NOT_LS)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbcc(void)
{
	if (CONDITION_NOT_CC)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbcs(void)
{
	if (CONDITION_NOT_CS)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbne(void)
{
	if (CONDITION_NOT_NE)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbeq(void)
{
	if (CONDITION_NOT_EQ)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbvc(void)
{
	if (CONDITION_NOT_VC)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbvs(void)
{
	if (CONDITION_NOT_VS)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbpl(void)
{
	if (CONDITION_NOT_PL)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbmi(void)
{
	if (CONDITION_NOT_MI)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbge(void)
{
	if (CONDITION_NOT_GE)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dblt(void)
{
	if (CONDITION_NOT_LT)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dbgt(void)
{
	if (CONDITION_NOT_GT)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_dble(void)
{
	if (CONDITION_NOT_LE)
	{
		uint *d_reg = &DY;
		uint res = MASK_OUT_ABOVE_16(*d_reg - 1);

		*d_reg = MASK_OUT_BELOW_16(*d_reg) | res;
		if (res != 0xffff)
		{
			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			m68ki_branch_word(m68ki_read_16(CPU_PC));
			USE_CLKS(10);
			return;
		}
		CPU_PC += 2;
		USE_CLKS(14);
		return;
	}
	CPU_PC += 2;
	USE_CLKS(12);
}


void m68000_divs_d_16(void)
{
	uint *d_dst = &DX;
	int src = MAKE_INT_16(DY);

	CPU_C = 0;

	if (src != 0)
	{
		int quotient = MAKE_INT_32(*d_dst) / src;
		int remainder = MAKE_INT_32(*d_dst) % src;

		if (quotient == MAKE_INT_16(quotient))
		{
			CPU_NOT_Z = quotient;
			CPU_N = GET_MSB_16(quotient);
			CPU_V = 0;
			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
			USE_CLKS(158);
			return;
		}
		CPU_V = 1;
		USE_CLKS(158);
		return;
	}
	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
}


void m68000_divs_16(void)
{
	uint *d_dst = &DX;
	int src = MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16()));

	CPU_C = 0;

	if (src != 0)
	{
		int quotient = MAKE_INT_32(*d_dst) / src;
		int remainder = MAKE_INT_32(*d_dst) % src;

		if (quotient == MAKE_INT_16(quotient))
		{
			CPU_NOT_Z = quotient;
			CPU_N = GET_MSB_16(quotient);
			CPU_V = 0;
			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
			USE_CLKS(158);
			return;
		}
		CPU_V = 1;
		USE_CLKS(158);
		return;
	}
	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
}


void m68000_divu_d_16(void)
{
	uint *d_dst = &DX;
	uint src = MASK_OUT_ABOVE_16(DY);

	CPU_C = 0;

	if (src != 0)
	{
		uint quotient = *d_dst / src;
		uint remainder = *d_dst % src;

		if (quotient < 0x10000)
		{
			CPU_NOT_Z = quotient;
			CPU_N = GET_MSB_16(quotient);
			CPU_V = 0;
			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
			USE_CLKS(140);
			return;
		}
		CPU_V = 1;
		USE_CLKS(140);
		return;
	}
	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
}


void m68000_divu_16(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_16(m68ki_get_ea_16());

	CPU_C = 0;

	if (src != 0)
	{
		uint quotient = *d_dst / src;
		uint remainder = *d_dst % src;

		if (quotient < 0x10000)
		{
			CPU_NOT_Z = quotient;
			CPU_N = GET_MSB_16(quotient);
			CPU_V = 0;
			*d_dst = MASK_OUT_ABOVE_32(MASK_OUT_ABOVE_16(quotient) | (remainder << 16));
			USE_CLKS(140);
			return;
		}
		CPU_V = 1;
		USE_CLKS(140);
		return;
	}
	m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
}


void m68020_divl_d_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint divisor = DY;
		uint dividend_hi = CPU_D[word2 & 7];
		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
		uint quotient = 0;
		uint remainder = 0;
		uint dividend_neg = 0;
		uint divisor_neg = 0;
		int i;

		if (divisor != 0)
		{
			/* quad / long : long quotient, long remainder */
			if (BIT_A(word2))
			{
				/* if dividing the upper long does not clear it, we're overflowing. */
				if (dividend_hi / divisor)
				{
					CPU_V = 1;
					USE_CLKS(78);
					return;
				}

				if (BIT_B(word2))	   /* signed */
				{
					if (GET_MSB_32(dividend_hi))
					{
						dividend_neg = 1;
						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
					}
					if (GET_MSB_32(divisor))
					{
						divisor_neg = 1;
						divisor = MASK_OUT_ABOVE_32(-divisor);
					}
				}
				for (i = 31; i >= 0; i--)
				{
					quotient <<= 1;
					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
					if (remainder >= divisor)
					{
						remainder -= divisor;
						quotient++;
					}
				}
				for (i = 31; i >= 0; i--)
				{
					quotient <<= 1;
					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
					if (remainder >= divisor)
					{
						remainder -= divisor;
						quotient++;
					}
				}

				if (BIT_B(word2))	   /* signed */
				{
					if (dividend_neg)
					{
						remainder = MASK_OUT_ABOVE_32(-remainder);
						quotient = MASK_OUT_ABOVE_32(-quotient);
					}
					if (divisor_neg)
						quotient = MASK_OUT_ABOVE_32(-quotient);
				}

				CPU_D[word2 & 7] = remainder;
				CPU_D[(word2 >> 12) & 7] = quotient;

				CPU_N = GET_MSB_32(quotient);
				CPU_NOT_Z = quotient;
				CPU_V = 0;
				USE_CLKS(78);
				return;
			}

			/* long / long: long quotient, maybe long remainder */
			if (BIT_B(word2))	   /* signed */
			{
				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
			}
			else
			{
				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
			}

			CPU_N = GET_MSB_32(quotient);
			CPU_NOT_Z = quotient;
			CPU_V = 0;
			USE_CLKS(78);
			return;
		}
		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
		return;
	}
	m68000_illegal();
}


void m68020_divl_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint divisor = m68ki_read_32(m68ki_get_ea_32());
		uint dividend_hi = CPU_D[word2 & 7];
		uint dividend_lo = CPU_D[(word2 >> 12) & 7];
		uint quotient = 0;
		uint remainder = 0;
		uint dividend_neg = 0;
		uint divisor_neg = 0;
		int i;

		if (divisor != 0)
		{
			/* quad / long : long quotient, long remainder */
			if (BIT_A(word2))
			{
				/* if dividing the upper long does not clear it, we're overflowing. */
				if (dividend_hi / divisor)
				{
					CPU_V = 1;
					USE_CLKS(78);
					return;
				}

				if (BIT_B(word2))	   /* signed */
				{
					if (GET_MSB_32(dividend_hi))
					{
						dividend_neg = 1;
						dividend_hi = MASK_OUT_ABOVE_32((-dividend_hi) - (dividend_lo != 0));
						dividend_lo = MASK_OUT_ABOVE_32(-dividend_lo);
					}
					if (GET_MSB_32(divisor))
					{
						divisor_neg = 1;
						divisor = MASK_OUT_ABOVE_32(-divisor);
					}
				}
				for (i = 31; i >= 0; i--)
				{
					quotient <<= 1;
					remainder = (remainder << 1) + ((dividend_hi >> i) & 1);
					if (remainder >= divisor)
					{
						remainder -= divisor;
						quotient++;
					}
				}
				for (i = 31; i >= 0; i--)
				{
					quotient <<= 1;
					remainder = (remainder << 1) + ((dividend_lo >> i) & 1);
					if (remainder >= divisor)
					{
						remainder -= divisor;
						quotient++;
					}
				}

				if (BIT_B(word2))	   /* signed */
				{
					if (dividend_neg)
					{
						remainder = MASK_OUT_ABOVE_32(-remainder);
						quotient = MASK_OUT_ABOVE_32(-quotient);
					}
					if (divisor_neg)
						quotient = MASK_OUT_ABOVE_32(-quotient);
				}
				CPU_D[word2 & 7] = remainder;
				CPU_D[(word2 >> 12) & 7] = quotient;

				CPU_N = GET_MSB_32(quotient);
				CPU_NOT_Z = quotient;
				CPU_V = 0;
				USE_CLKS(78);
				return;
			}

			/* long / long: long quotient, maybe long remainder */
			if (BIT_B(word2))	   /* signed */
			{
				CPU_D[word2 & 7] = MAKE_INT_32(dividend_lo) % MAKE_INT_32(divisor);
				CPU_D[(word2 >> 12) & 7] = MAKE_INT_32(dividend_lo) / MAKE_INT_32(divisor);
			}
			else
			{
				CPU_D[word2 & 7] = MASK_OUT_ABOVE_32(dividend_lo) % MASK_OUT_ABOVE_32(divisor);
				CPU_D[(word2 >> 12) & 7] = MASK_OUT_ABOVE_32(dividend_lo) / MASK_OUT_ABOVE_32(divisor);
			}

			CPU_N = GET_MSB_32(quotient);
			CPU_NOT_Z = quotient;
			CPU_V = 0;
			USE_CLKS(78);
			return;
		}
		m68ki_interrupt(EXCEPTION_ZERO_DIVIDE);
		return;
	}
	m68000_illegal();
}


void m68000_eor_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY ^= MASK_OUT_ABOVE_8(DX));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_eor_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint res = MASK_OUT_ABOVE_8(DX ^ m68ki_read_8(ea));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_eor_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY ^= MASK_OUT_ABOVE_16(DX));

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_eor_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint res = MASK_OUT_ABOVE_16(DX ^ m68ki_read_16(ea));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_eor_d_32(void)
{
	uint res = DY ^= DX;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_eor_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint res = DX ^ m68ki_read_32(ea);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_eori_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY ^= m68ki_read_imm_8());

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_eori_8(void)
{
	uint tmp = m68ki_read_imm_8();
	uint ea = m68ki_get_ea_8();
	uint res = tmp ^ m68ki_read_8(ea);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_eori_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY ^= m68ki_read_imm_16());

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_eori_16(void)
{
	uint tmp = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint res = tmp ^ m68ki_read_16(ea);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_eori_d_32(void)
{
	uint res = DY ^= m68ki_read_imm_32();

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(16);
}


void m68000_eori_32(void)
{
	uint tmp = m68ki_read_imm_32();
	uint ea = m68ki_get_ea_32();
	uint res = tmp ^ m68ki_read_32(ea);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(20);
}


void m68000_eori_to_ccr(void)
{
	m68ki_set_ccr(m68ki_get_ccr() ^ m68ki_read_imm_16());
	USE_CLKS(20);
}


void m68000_eori_to_sr(void)
{
	uint eor_val = m68ki_read_imm_16();

	if (CPU_S)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_set_sr(m68ki_get_sr() ^ eor_val);
		USE_CLKS(20);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_exg_dd(void)
{
	uint *reg_a = &DX;
	uint *reg_b = &DY;
	uint tmp = *reg_a;

	*reg_a = *reg_b;
	*reg_b = tmp;

	USE_CLKS(6);
}


void m68000_exg_aa(void)
{
	uint *reg_a = &AX;
	uint *reg_b = &AY;
	uint tmp = *reg_a;

	*reg_a = *reg_b;
	*reg_b = tmp;

	USE_CLKS(6);
}


void m68000_exg_da(void)
{
	uint *reg_a = &DX;
	uint *reg_b = &AY;
	uint tmp = *reg_a;

	*reg_a = *reg_b;
	*reg_b = tmp;

	USE_CLKS(6);
}


void m68000_ext_16(void)
{
	uint *d_dst = &DY;

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | MASK_OUT_ABOVE_8(*d_dst) | (GET_MSB_8(*d_dst) ? 0xff00 : 0);

	CPU_N = GET_MSB_16(*d_dst);
	CPU_NOT_Z = MASK_OUT_ABOVE_16(*d_dst);
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_ext_32(void)
{
	uint *d_dst = &DY;

	*d_dst = MASK_OUT_ABOVE_16(*d_dst) | (GET_MSB_16(*d_dst) ? 0xffff0000 : 0);

	CPU_N = GET_MSB_32(*d_dst);
	CPU_NOT_Z = *d_dst;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68020_extb(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint *d_dst = &DY;

		*d_dst = MASK_OUT_ABOVE_8(*d_dst) | (GET_MSB_8(*d_dst) ? 0xffffff00 : 0);

		CPU_N = GET_MSB_32(*d_dst);
		CPU_NOT_Z = *d_dst;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68000_illegal(void)
{
	m68ki_exception(EXCEPTION_ILLEGAL_INSTRUCTION);
	M68K_DO_LOG((M68K_LOG, "%s at %08x: illegal instruction %04x (%s)\n",
				 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PPC), CPU_IR,
				 m68k_disassemble_quick(ADDRESS_68K(CPU_PPC))));
#ifdef M68K_LOG
	/* I use this to get the proper offset when disassembling the offending instruction */
	m68k_pc_offset = 2;
#endif
}


void m68000_jmp(void)
{
	m68ki_branch_long(m68ki_get_ea_32());
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	USE_CLKS(0);
}


void m68000_jsr(void)
{
	uint ea = m68ki_get_ea_32();

	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_push_32(CPU_PC);
	m68ki_branch_long(ea);
	USE_CLKS(0);
}


void m68000_lea(void)
{
	AX = m68ki_get_ea_32();
	USE_CLKS(0);
}


void m68000_link_16_a7(void)
{
	CPU_A[7] -= 4;
	m68ki_write_32(CPU_A[7], CPU_A[7]);
	CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + MAKE_INT_16(m68ki_read_imm_16()));
	USE_CLKS(16);
}


void m68000_link_16(void)
{
	uint *a_dst = &AY;

	m68ki_push_32(*a_dst);
	*a_dst = CPU_A[7];
	CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + MAKE_INT_16(m68ki_read_imm_16()));
	USE_CLKS(16);
}


void m68020_link_32_a7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		CPU_A[7] -= 4;
		m68ki_write_32(CPU_A[7], CPU_A[7]);
		CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + m68ki_read_imm_32());
		USE_CLKS(16);
		return;
	}
	m68000_illegal();
}


void m68020_link_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint *a_dst = &AY;

		m68ki_push_32(*a_dst);
		*a_dst = CPU_A[7];
		CPU_A[7] = MASK_OUT_ABOVE_32(CPU_A[7] + m68ki_read_imm_32());
		USE_CLKS(16);
		return;
	}
	m68000_illegal();
}


void m68000_lsr_s_8(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = src >> shift;

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = 0;
	CPU_NOT_Z = res;
	CPU_X = CPU_C = shift > 8 ? 0 : (src >> (shift - 1)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_lsr_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = src >> shift;

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = 0;
	CPU_NOT_Z = res;
	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_lsr_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = *d_dst;
	uint res = src >> shift;

	*d_dst = res;

	CPU_N = 0;
	CPU_NOT_Z = res;
	CPU_X = CPU_C = (src >> (shift - 1)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 8);
}


void m68000_lsr_r_8(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = src >> shift;

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift <= 8)
		{
			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
			CPU_X = CPU_C = (src >> (shift - 1)) & 1;
			CPU_N = 0;
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		*d_dst &= 0xffffff00;
		CPU_X = CPU_C = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_lsr_r_16(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = src >> shift;

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift <= 16)
		{
			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
			CPU_X = CPU_C = (src >> (shift - 1)) & 1;
			CPU_N = 0;
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		*d_dst &= 0xffff0000;
		CPU_X = CPU_C = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_lsr_r_32(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = *d_dst;
	uint res = src >> shift;

	USE_CLKS((shift << 1) + 8);
	if (shift != 0)
	{
		if (shift < 32)
		{
			*d_dst = res;
			CPU_X = CPU_C = (src >> (shift - 1)) & 1;
			CPU_N = 0;
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		*d_dst = 0;
		CPU_X = CPU_C = (shift == 32 ? GET_MSB_32(src) : 0);
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_lsr_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint res = src >> 1;

	m68ki_write_16(ea, res);

	CPU_N = 0;
	CPU_NOT_Z = res;
	CPU_C = CPU_X = src & 1;
	CPU_V = 0;
	USE_CLKS(8);
}


void m68000_lsl_s_8(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = MASK_OUT_ABOVE_8(src << shift);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = shift > 8 ? 0 : (src >> (8 - shift)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_lsl_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = MASK_OUT_ABOVE_16(src << shift);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = (src >> (16 - shift)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_lsl_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = *d_dst;
	uint res = MASK_OUT_ABOVE_32(src << shift);

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = (src >> (32 - shift)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 8);
}


void m68000_lsl_r_8(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = MASK_OUT_ABOVE_8(src << shift);

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift <= 8)
		{
			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
			CPU_X = CPU_C = (src >> (8 - shift)) & 1;
			CPU_N = GET_MSB_8(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		*d_dst &= 0xffffff00;
		CPU_X = CPU_C = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_lsl_r_16(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = MASK_OUT_ABOVE_16(src << shift);

	USE_CLKS((shift << 1) + 6);
	if (shift != 0)
	{
		if (shift <= 16)
		{
			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
			CPU_X = CPU_C = (src >> (16 - shift)) & 1;
			CPU_N = GET_MSB_16(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		*d_dst &= 0xffff0000;
		CPU_X = CPU_C = 0;
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_lsl_r_32(void)
{
	uint *d_dst = &DY;
	uint shift = DX & 0x3f;
	uint src = *d_dst;
	uint res = MASK_OUT_ABOVE_32(src << shift);

	USE_CLKS((shift << 1) + 8);
	if (shift != 0)
	{
		if (shift < 32)
		{
			*d_dst = res;
			CPU_X = CPU_C = (src >> (32 - shift)) & 1;
			CPU_N = GET_MSB_32(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}

		*d_dst = 0;
		CPU_X = CPU_C = (shift == 32 ? src & 1 : 0);
		CPU_N = 0;
		CPU_NOT_Z = 0;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_lsl_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(src << 1);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = GET_MSB_16(src);
	CPU_V = 0;
	USE_CLKS(8);
}


void m68000_move_dd_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint *d_dst = &DX;

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_dd_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint *d_dst = &DX;

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_ai_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = AX;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_ai_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = AX;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi7_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = (CPU_A[7] += 2) - 2;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = AX++;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi7_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = (CPU_A[7] += 2) - 2;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = AX++;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd7_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = CPU_A[7] -= 2;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = --AX;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd7_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = CPU_A[7] -= 2;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = --AX;

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_di_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_di_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_ix_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);

	m68ki_write_8(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(14);
}


void m68000_move_ix_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());

	m68ki_write_8(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(14);
}


void m68000_move_aw_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_aw_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_al_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_al_8(void)
{
	uint res = m68ki_read_8(m68ki_get_ea_8());
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_8(ea_dst, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_dd_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint *d_dst = &DX;

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_dd_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint *d_dst = &DX;

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_dd_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint *d_dst = &DX;

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_ai_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint ea_dst = AX;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_ai_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint ea_dst = AX;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_ai_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint ea_dst = AX;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint ea_dst = (AX += 2) - 2;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint ea_dst = (AX += 2) - 2;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pi_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint ea_dst = (AX += 2) - 2;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint ea_dst = AX -= 2;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint ea_dst = AX -= 2;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_pd_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint ea_dst = AX -= 2;

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(8);
}


void m68000_move_di_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_di_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_di_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_ix_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);

	m68ki_write_16(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(14);
}


void m68000_move_ix_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);

	m68ki_write_16(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(14);
}


void m68000_move_ix_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());

	m68ki_write_16(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(14);
}


void m68000_move_aw_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_aw_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_aw_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_al_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_al_a_16(void)
{
	uint res = MASK_OUT_ABOVE_16(AY);
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_al_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_16(ea_dst, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_dd_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint *d_dst = &DX;

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_dd_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint *d_dst = &DX;

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_dd_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint *d_dst = &DX;

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_move_ai_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint ea_dst = AX;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_ai_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint ea_dst = AX;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_ai_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint ea_dst = AX;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_pi_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint ea_dst = (AX += 4) - 4;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_pi_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint ea_dst = (AX += 4) - 4;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_pi_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint ea_dst = (AX += 4) - 4;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_pd_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint ea_dst = AX -= 4;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_pd_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint ea_dst = AX -= 4;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_pd_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint ea_dst = AX -= 4;

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(12);
}


void m68000_move_di_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_di_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_di_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint ea_dst = AX + MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_ix_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);

	m68ki_write_32(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(18);
}


void m68000_move_ix_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);

	m68ki_write_32(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(18);
}


void m68000_move_ix_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());

	m68ki_write_32(m68ki_get_ea_ix_dst(), res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(18);
}


void m68000_move_aw_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_aw_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_aw_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint ea_dst = MAKE_INT_16(m68ki_read_imm_16());

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(16);
}


void m68000_move_al_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY);
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(20);
}


void m68000_move_al_a_32(void)
{
	uint res = MASK_OUT_ABOVE_32(AY);
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(20);
}


void m68000_move_al_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());
	uint ea_dst = m68ki_read_imm_32();

	m68ki_write_32(ea_dst, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(20);
}


void m68000_movea_d_16(void)
{
	AX = MAKE_INT_16(DY);
	USE_CLKS(4);
}


void m68000_movea_a_16(void)
{
	AX = MAKE_INT_16(AY);
	USE_CLKS(4);
}


void m68000_movea_16(void)
{
	AX = MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16()));
	USE_CLKS(4);
}


void m68000_movea_d_32(void)
{
	AX = MASK_OUT_ABOVE_32(DY);
	USE_CLKS(4);
}


void m68000_movea_a_32(void)
{
	AX = MASK_OUT_ABOVE_32(AY);
	USE_CLKS(4);
}


void m68000_movea_32(void)
{
	AX = m68ki_read_32(m68ki_get_ea_32());
	USE_CLKS(4);
}


void m68010_move_fr_ccr_d(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		DY = MASK_OUT_BELOW_16(DY) | m68ki_get_ccr();
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68010_move_fr_ccr(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		m68ki_write_16(m68ki_get_ea_16(), m68ki_get_ccr());
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68000_move_to_ccr_d(void)
{
	m68ki_set_ccr(DY);
	USE_CLKS(12);
}


void m68000_move_to_ccr(void)
{
	m68ki_set_ccr(m68ki_read_16(m68ki_get_ea_16()));
	USE_CLKS(12);
}


void m68000_move_fr_sr_d(void)
{
	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
	{
		DY = MASK_OUT_BELOW_16(DY) | m68ki_get_sr();
		USE_CLKS(6);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_move_fr_sr(void)
{
	uint ea = m68ki_get_ea_16();

	if ((CPU_MODE & CPU_MODE_000) || CPU_S)	/* NS990408 */
	{
		m68ki_write_16(ea, m68ki_get_sr());
		USE_CLKS(8);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_move_to_sr_d(void)
{
	if (CPU_S)
	{
		m68ki_set_sr(DY);
		USE_CLKS(12);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_move_to_sr(void)
{
	uint new_sr = m68ki_read_16(m68ki_get_ea_16());

	if (CPU_S)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_set_sr(new_sr);
		USE_CLKS(12);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_move_fr_usp(void)
{
	if (CPU_S)
	{
		AY = CPU_USP;
		USE_CLKS(4);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_move_to_usp(void)
{
	if (CPU_S)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_USP = AY;
		USE_CLKS(4);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68010_movec_cr(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		if (CPU_S)
		{
			uint next_word = m68ki_read_imm_16();

			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			USE_CLKS(12);
			switch (next_word & 0xfff)
			{
			case 0x000:			   /* SFC */
				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_SFC;
				return;
			case 0x001:			   /* DFC */
				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_DFC;
				return;
			case 0x002:			   /* CACR */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_CACR;
					return;
				}
				return;
			case 0x800:			   /* USP */
				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_USP;
				return;
			case 0x801:			   /* VBR */
				m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_VBR;
				return;
			case 0x802:			   /* CAAR */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_CAAR;
					return;
				}
				m68000_illegal();
				break;
			case 0x803:			   /* MSP */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_MSP;
					return;
				}
				m68000_illegal();
				return;
			case 0x804:			   /* ISP */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = CPU_ISP;
					return;
				}
				m68000_illegal();
				return;
			default:
#ifdef M68K_LOG
				m68k_pc_offset = 4;
#endif
				m68000_illegal();
				return;
			}
		}
		m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
		return;
	}
	m68000_illegal();
}


void m68010_movec_rc(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		if (CPU_S)
		{
			uint next_word = m68ki_read_imm_16();

			m68ki_add_trace();		   /* auto-disable (see m68kcpu.h) */
			USE_CLKS(10);
			switch (next_word & 0xfff)
			{
			case 0x000:			   /* SFC */
				CPU_SFC = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
				return;
			case 0x001:			   /* DFC */
				CPU_DFC = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
				return;
			case 0x002:			   /* CACR */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					CPU_CACR = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
					return;
				}
				m68000_illegal();
				return;
			case 0x800:			   /* USP */
				CPU_USP = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7];
				return;
			case 0x801:			   /* VBR */
				CPU_VBR = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7];
				return;
			case 0x802:			   /* CAAR */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					CPU_CAAR = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
					return;
				}
				m68000_illegal();
				return;
			case 0x803:			   /* MSP */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					CPU_MSP = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
					return;
				}
				m68000_illegal();
				return;
			case 0x804:			   /* ISP */
				if (CPU_MODE & CPU_MODE_EC020_PLUS)
				{
					CPU_ISP = m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] & 7;
					return;
				}
				m68000_illegal();
				return;
			default:
#ifdef M68K_LOG
				m68k_pc_offset = 4;
#endif
				m68000_illegal();
				return;
			}
		}
		m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
		return;
	}
	m68000_illegal();
}


void m68000_movem_pd_16(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = AY;
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			ea -= 2;
			m68ki_write_16(ea, *(m68k_movem_pd_table[i]));
			count++;
		}
	AY = ea;
	USE_CLKS((count << 2) + 8);
}


void m68000_movem_pd_32(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = AY;
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			ea -= 4;
			m68ki_write_32(ea, *(m68k_movem_pd_table[i]));
			count++;
		}
	AY = ea;
	/* ASG: changed from (count << 4) to (count << 3) */
	USE_CLKS((count << 3) + 8);
}


void m68000_movem_pi_16(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = AY;
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
			ea += 2;
			count++;
		}
	AY = ea;
	USE_CLKS((count << 2) + 12);
}


void m68000_movem_pi_32(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = AY;
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
			ea += 4;
			count++;
		}
	AY = ea;
	/* ASG: changed from (count << 4) to (count << 3) */
	USE_CLKS((count << 3) + 12);
}


void m68000_movem_re_16(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			m68ki_write_16(ea, *(m68k_movem_pi_table[i]));
			ea += 2;
			count++;
		}
	USE_CLKS((count << 2) + 4);
}


void m68000_movem_re_32(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_32();
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			m68ki_write_32(ea, *(m68k_movem_pi_table[i]));
			ea += 4;
			count++;
		}
	/* ASG: changed from (count << 4) to (count << 3) */
	USE_CLKS((count << 3) + 4);
}


void m68000_movem_er_16(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			*(m68k_movem_pi_table[i]) = MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_16(ea)));
			ea += 2;
			count++;
		}
	USE_CLKS((count << 2) + 8);
}


void m68000_movem_er_32(void)
{
	uint i = 0;
	uint register_list = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_32();
	uint count = 0;

	for (; i < 16; i++)
		if (register_list & (1 << i))
		{
			*(m68k_movem_pi_table[i]) = m68ki_read_32(ea);
			ea += 4;
			count++;
		}
	/* ASG: changed from (count << 4) to (count << 3) */
	USE_CLKS((count << 3) + 8);
}


void m68000_movep_re_16(void)
{
	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
	uint src = DX;

	m68ki_write_8(ea, MASK_OUT_ABOVE_8(src >> 8));
	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src));
	USE_CLKS(16);
}


void m68000_movep_re_32(void)
{
	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
	uint src = DX;

	m68ki_write_8(ea, MASK_OUT_ABOVE_8(src >> 24));
	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src >> 16));
	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src >> 8));
	m68ki_write_8(ea += 2, MASK_OUT_ABOVE_8(src));
	USE_CLKS(24);
}


void m68000_movep_er_16(void)
{
	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));
	uint *d_dst = &DX;

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | ((m68ki_read_8(ea) << 8) + m68ki_read_8(ea + 2));
	USE_CLKS(16);
}


void m68000_movep_er_32(void)
{
	uint ea = AY + MAKE_INT_16(MASK_OUT_ABOVE_16(m68ki_read_imm_16()));

	DX = (m68ki_read_8(ea) << 24) + (m68ki_read_8(ea + 2) << 16)
		+ (m68ki_read_8(ea + 4) << 8) + m68ki_read_8(ea + 6);
	USE_CLKS(24);
}


void m68010_moves_8(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		uint next_word = m68ki_read_imm_16();
		uint ea = m68ki_get_ea_8();

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		USE_CLKS(0);
		if (BIT_B(next_word))		   /* Register to memory */
		{
			m68ki_write_8_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
			return;
		}
		if (BIT_F(next_word))		   /* Memory to address register */
		{
			CPU_A[(next_word >> 12) & 7] = MAKE_INT_8(m68ki_read_8_fc(ea, CPU_SFC));
			return;
		}
		/* Memory to data register */
		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_8(CPU_D[(next_word >> 12) & 7]) | m68ki_read_8_fc(ea, CPU_SFC);
		return;
	}
	m68000_illegal();
}


void m68010_moves_16(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		uint next_word = m68ki_read_imm_16();
		uint ea = m68ki_get_ea_16();

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		USE_CLKS(0);
		if (BIT_B(next_word))		   /* Register to memory */
		{
			m68ki_write_16_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
			return;
		}
		if (BIT_F(next_word))		   /* Memory to address register */
		{
			CPU_A[(next_word >> 12) & 7] = MAKE_INT_16(m68ki_read_16_fc(ea, CPU_SFC));
			return;
		}
		/* Memory to data register */
		CPU_D[(next_word >> 12) & 7] = MASK_OUT_BELOW_16(CPU_D[(next_word >> 12) & 7]) | m68ki_read_16_fc(ea, CPU_SFC);
		return;
	}
	m68000_illegal();
}


void m68010_moves_32(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		uint next_word = m68ki_read_imm_16();
		uint ea = m68ki_get_ea_32();

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		USE_CLKS(0);
		if (BIT_B(next_word))		   /* Register to memory */
		{
			m68ki_write_32_fc(ea, CPU_DFC, m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7]);
			return;
		}
		/* Memory to register */
		m68k_cpu_dar[next_word >> 15][(next_word >> 12) & 7] = m68ki_read_32_fc(ea, CPU_SFC);
		return;
	}
	m68000_illegal();
}


void m68000_moveq(void)
{
	uint res = DX = MAKE_INT_8(MASK_OUT_ABOVE_8(CPU_IR));

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_muls_d_16(void)
{
	uint *d_dst = &DX;
	uint res = MAKE_INT_16(DY) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));

	*d_dst = res;

	CPU_NOT_Z = res;
	CPU_N = GET_MSB_32(res);
	CPU_V = CPU_C = 0;
	USE_CLKS(54);
}


void m68000_muls_16(void)
{
	uint *d_dst = &DX;
	uint res = MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16())) * MAKE_INT_16(MASK_OUT_ABOVE_16(*d_dst));

	*d_dst = res;

	CPU_NOT_Z = res;
	CPU_N = GET_MSB_32(res);
	CPU_V = CPU_C = 0;
	USE_CLKS(54);
}


void m68000_mulu_d_16(void)
{
	uint *d_dst = &DX;
	uint res = MASK_OUT_ABOVE_16(DY) * MASK_OUT_ABOVE_16(*d_dst);

	*d_dst = res;

	CPU_NOT_Z = res;
	CPU_N = GET_MSB_32(res);
	CPU_V = CPU_C = 0;
	USE_CLKS(54);
}


void m68000_mulu_16(void)
{
	uint *d_dst = &DX;
	uint res = m68ki_read_16(m68ki_get_ea_16()) * MASK_OUT_ABOVE_16(*d_dst);

	*d_dst = res;

	CPU_NOT_Z = res;
	CPU_N = GET_MSB_32(res);
	CPU_V = CPU_C = 0;
	USE_CLKS(54);
}


void m68020_mull_d_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint src = DY;
		uint dst = CPU_D[(word2 >> 12) & 7];
		uint neg = GET_MSB_32(src ^ dst);
		uint r1;
		uint r2;
		uint r3;
		uint lo;
		uint hi;

		if (BIT_B(word2))			   /* signed */
		{
			if (GET_MSB_32(src))
				src = MASK_OUT_ABOVE_32(-src);
			if (GET_MSB_32(dst))
				dst = MASK_OUT_ABOVE_32(-dst);
		}

		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);

		r1 = lo;
		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);

		if (BIT_B(word2) && neg)
		{
			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
			lo = MASK_OUT_ABOVE_32(-lo);
		}

		CPU_D[(word2 >> 12) & 7] = lo;
		if (BIT_A(word2))
		{
			CPU_D[word2 & 7] = hi;
			CPU_N = GET_MSB_32(hi);
			CPU_NOT_Z = hi | lo;
			CPU_V = 0;
			USE_CLKS(43);
			return;
		}

		CPU_N = GET_MSB_32(lo);
		CPU_NOT_Z = lo;
		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
		USE_CLKS(43);
		return;
	}
	m68000_illegal();
}


void m68020_mull_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint word2 = m68ki_read_imm_16();
		uint src = m68ki_read_32(m68ki_get_ea_32());
		uint dst = CPU_D[(word2 >> 12) & 7];
		uint neg = GET_MSB_32(src ^ dst);
		uint r1;
		uint r2;
		uint r3;
		uint lo;
		uint hi;

		if (BIT_B(word2))			   /* signed */
		{
			if (GET_MSB_32(src))
				src = MASK_OUT_ABOVE_32(-src);
			if (GET_MSB_32(dst))
				dst = MASK_OUT_ABOVE_32(-dst);
		}

		r1 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst);
		r2 = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst);
		r3 = MASK_OUT_ABOVE_16(src) * MASK_OUT_ABOVE_16(dst >> 16);
		lo = r1 + MASK_OUT_BELOW_16(r2 << 16);
		hi = MASK_OUT_ABOVE_16(src >> 16) * MASK_OUT_ABOVE_16(dst >> 16) + (lo < r1);

		r1 = lo;
		lo = r1 + MASK_OUT_BELOW_16(r3 << 16);
		hi += (lo < r1) + MASK_OUT_ABOVE_16(r2 >> 16) + MASK_OUT_ABOVE_16(r3 >> 16);

		if (BIT_B(word2) && neg)
		{
			hi = MASK_OUT_ABOVE_32((-hi) - (lo != 0));
			lo = MASK_OUT_ABOVE_32(-lo);
		}

		CPU_D[(word2 >> 12) & 7] = lo;
		if (BIT_A(word2))
		{
			CPU_D[word2 & 7] = hi;
			CPU_N = GET_MSB_32(hi);
			CPU_NOT_Z = hi | lo;
			CPU_V = 0;
			USE_CLKS(43);
			return;
		}

		CPU_N = GET_MSB_32(lo);
		CPU_NOT_Z = lo;
		CPU_V = (GET_MSB_32(lo) && hi == 0xffffffff) || (!GET_MSB_32(lo) && !hi);
		USE_CLKS(43);
		return;
	}
	m68000_illegal();
}


void m68000_nbcd_d(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));

	if (res != 0x9a)
	{
		if ((res & 0x0f) == 0xa)
			res = (res & 0xf0) + 0x10;

		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

		if (res != 0)
			CPU_NOT_Z = 1;
		CPU_C = CPU_X = 1;
	}
	else
		CPU_C = CPU_X = 0;
	USE_CLKS(6);
}


void m68000_nbcd(void)
{
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));

	if (res != 0x9a)
	{
		if ((res & 0x0f) == 0xa)
			res = (res & 0xf0) + 0x10;

		m68ki_write_8(ea, res);

		if (res != 0)
			CPU_NOT_Z = 1;
		CPU_C = CPU_X = 1;
	}
	else
		CPU_C = CPU_X = 0;
	USE_CLKS(8);
}


void m68000_neg_d_8(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(-dst);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = GET_MSB_8(dst & res);
	CPU_C = CPU_X = res != 0;
	USE_CLKS(4);
}


void m68000_neg_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(-dst);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = GET_MSB_8(dst & res);
	CPU_C = CPU_X = res != 0;
	USE_CLKS(8);
}


void m68000_neg_d_16(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(-dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = GET_MSB_16(dst & res);
	CPU_C = CPU_X = res != 0;
	USE_CLKS(4);
}


void m68000_neg_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(-dst);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = GET_MSB_16(dst & res);
	CPU_C = CPU_X = res != 0;
	USE_CLKS(8);
}


void m68000_neg_d_32(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(-dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = GET_MSB_32(dst & res);
	CPU_C = CPU_X = res != 0;
	USE_CLKS(6);
}


void m68000_neg_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(-dst);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = GET_MSB_32(dst & res);
	CPU_C = CPU_X = res != 0;
	USE_CLKS(12);
}


void m68000_negx_d_8(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = GET_MSB_8(dst & res);
	CPU_C = CPU_X = GET_MSB_8(dst | res);
	USE_CLKS(4);
}


void m68000_negx_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = GET_MSB_8(dst & res);
	CPU_C = CPU_X = GET_MSB_8(dst | res);
	USE_CLKS(8);
}


void m68000_negx_d_16(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = GET_MSB_16(dst & res);
	CPU_C = CPU_X = GET_MSB_16(dst | res);
	USE_CLKS(4);
}


void m68000_negx_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = GET_MSB_16(dst & res);
	CPU_C = CPU_X = GET_MSB_16(dst | res);
	USE_CLKS(8);
}


void m68000_negx_d_32(void)
{
	uint *d_dst = &DY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));

	CPU_N = GET_MSB_32(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = GET_MSB_32(dst & res);
	CPU_C = CPU_X = GET_MSB_32(dst | res);
	USE_CLKS(6);
}


void m68000_negx_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_V = GET_MSB_32(dst & res);
	CPU_C = CPU_X = GET_MSB_32(dst | res);
	USE_CLKS(12);
}


void m68000_nop(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	USE_CLKS(4);
}


void m68000_not_d_8(void)
{
	uint *d_dst = &DY;
	uint res = MASK_OUT_ABOVE_8(~*d_dst);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_not_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_not_d_16(void)
{
	uint *d_dst = &DY;
	uint res = MASK_OUT_ABOVE_16(~*d_dst);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_not_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_not_d_32(void)
{
	uint *d_dst = &DY;
	uint res = *d_dst = MASK_OUT_ABOVE_32(~*d_dst);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(6);
}


void m68000_not_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_or_er_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8((DX |= MASK_OUT_ABOVE_8(DY)));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_or_er_8(void)
{
	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(m68ki_get_ea_8())));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_or_er_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16((DX |= MASK_OUT_ABOVE_16(DY)));

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_or_er_16(void)
{
	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(m68ki_get_ea_16())));

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_or_er_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32((DX |= DY));

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_or_er_32(void)
{
	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(m68ki_get_ea_32())));

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(6);
}


void m68000_or_re_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_or_re_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_or_re_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_ori_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8((DY |= m68ki_read_imm_8()));

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_ori_8(void)
{
	uint tmp = m68ki_read_imm_8();
	uint ea = m68ki_get_ea_8();
	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_ori_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY |= m68ki_read_imm_16());

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(8);
}


void m68000_ori_16(void)
{
	uint tmp = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(12);
}


void m68000_ori_d_32(void)
{
	uint res = MASK_OUT_ABOVE_32(DY |= m68ki_read_imm_32());

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(16);
}


void m68000_ori_32(void)
{
	uint tmp = m68ki_read_imm_32();
	uint ea = m68ki_get_ea_32();
	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_V = 0;
	USE_CLKS(20);
}


void m68000_ori_to_ccr(void)
{
	m68ki_set_ccr(m68ki_get_ccr() | m68ki_read_imm_16());
	USE_CLKS(20);
}


void m68000_ori_to_sr(void)
{
	uint or_val = m68ki_read_imm_16();

	if (CPU_S)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		m68ki_set_sr(m68ki_get_sr() | or_val);
		USE_CLKS(20);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68020_pack_rr(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = DX + m68ki_read_imm_16();

		DY = MASK_OUT_BELOW_8(DY) | ((src >> 4) & 0x00f0) | (src & 0x000f);
		USE_CLKS(6);
		return;
	}
	m68000_illegal();
}


void m68020_pack_mm_ax7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_16(AX -= 2);

		src = (((src >> 8) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
		m68ki_write_8(CPU_A[7] -= 2, ((src >> 4) & 0xf0) | (src & 0x0f));
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68020_pack_mm_ay7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_32(CPU_A[7] -= 4);

		src = (((src >> 16) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
		/* I hate the way Motorola changes where Rx and Ry are */
		m68ki_write_8(--AY, ((src >> 4) & 0xf0) | (src & 0x0f));
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68020_pack_mm_axy7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_32(CPU_A[7] -= 4);

		src = (((src >> 16) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
		m68ki_write_8(CPU_A[7] -= 2, ((src >> 4) & 0xf0) | (src & 0x0f));
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68020_pack_mm(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_16(AX -= 2);

		src = (((src >> 8) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
		m68ki_write_8(--AY, ((src >> 4) & 0xf0) | (src & 0x0f));
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68000_pea(void)
{
	uint ea = m68ki_get_ea_32();

	m68ki_push_32(ea);
	USE_CLKS(0);
}


void m68000_rst(void)
{
	if (CPU_S)
	{
		m68ki_output_reset();		   /* auto-disable (see m68kcpu.h) */
		USE_CLKS(132);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_ror_s_8(void)
{
	uint *d_dst = &DY;
	uint orig_shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint shift = orig_shift & 7;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = ROR_8(src, shift);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = (src >> ((shift - 1) & 7)) & 1;
	CPU_V = 0;
	USE_CLKS((orig_shift << 1) + 6);
}


void m68000_ror_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = ROR_16(src, shift);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = (src >> (shift - 1)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_ror_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = ROR_32(src, shift);

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = (src >> (shift - 1)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 8);
}


void m68000_ror_r_8(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift & 7;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = ROR_8(src, shift);

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
		CPU_C = (src >> ((shift - 1) & 7)) & 1;
		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_ror_r_16(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift & 15;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = ROR_16(src, shift);

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
		CPU_C = (src >> ((shift - 1) & 15)) & 1;
		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_ror_r_32(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift & 31;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = ROR_32(src, shift);

	USE_CLKS((orig_shift << 1) + 8);
	if (orig_shift != 0)
	{
		*d_dst = res;
		CPU_C = (src >> ((shift - 1) & 31)) & 1;
		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_ror_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint res = ROR_16(src, 1);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = src & 1;
	CPU_V = 0;
	USE_CLKS(8);
}


void m68000_rol_s_8(void)
{
	uint *d_dst = &DY;
	uint orig_shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint shift = orig_shift & 7;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = MASK_OUT_ABOVE_8(ROL_8(src, shift));

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = (src >> (8 - orig_shift)) & 1;
	CPU_V = 0;
	USE_CLKS((orig_shift << 1) + 6);
}


void m68000_rol_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = MASK_OUT_ABOVE_16(ROL_16(src, shift));

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = (src >> (16 - shift)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_rol_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = MASK_OUT_ABOVE_32(ROL_32(src, shift));

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_C = (src >> (32 - shift)) & 1;
	CPU_V = 0;
	USE_CLKS((shift << 1) + 8);
}


void m68000_rol_r_8(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift & 7;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint res = MASK_OUT_ABOVE_8(ROL_8(src, shift));

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		if (shift != 0)
		{
			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
			CPU_C = (src >> (8 - shift)) & 1;
			CPU_N = GET_MSB_8(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}
		CPU_C = src & 1;
		CPU_N = GET_MSB_8(src);
		CPU_NOT_Z = src;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_rol_r_16(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift & 15;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint res = MASK_OUT_ABOVE_16(ROL_16(src, shift));

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		if (shift != 0)
		{
			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
			CPU_C = (src >> (16 - shift)) & 1;
			CPU_N = GET_MSB_16(res);
			CPU_NOT_Z = res;
			CPU_V = 0;
			return;
		}
		CPU_C = src & 1;
		CPU_N = GET_MSB_16(src);
		CPU_NOT_Z = src;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_rol_r_32(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift & 31;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = MASK_OUT_ABOVE_32(ROL_32(src, shift));

	USE_CLKS((orig_shift << 1) + 8);
	if (orig_shift != 0)
	{
		*d_dst = res;

		CPU_C = (src >> (32 - shift)) & 1;
		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = 0;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_rol_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = GET_MSB_16(src);
	CPU_V = 0;
	USE_CLKS(8);
}


void m68000_roxr_s_8(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint tmp = ROR_9(src | ((CPU_X != 0) << 8), shift);
	uint res = MASK_OUT_ABOVE_8(tmp);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_X = GET_MSB_9(tmp);
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_roxr_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), shift);
	uint res = MASK_OUT_ABOVE_16(tmp);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_X = GET_MSB_17(tmp);
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_roxr_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = MASK_OUT_ABOVE_32((ROR_33(src, shift) & ~(1 << (32 - shift))) | ((CPU_X != 0) << (32 - shift)));
	uint new_x_flag = src & (1 << (shift - 1));

	*d_dst = res;

	CPU_C = CPU_X = new_x_flag;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;

	USE_CLKS((shift << 1) + 8);
}


void m68000_roxr_r_8(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift % 9;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint tmp = ROR_9(src | ((CPU_X != 0) << 8), shift);
	uint res = MASK_OUT_ABOVE_8(tmp);

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
		CPU_C = CPU_X = GET_MSB_9(tmp);
		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = CPU_X;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_roxr_r_16(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift % 17;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), shift);
	uint res = MASK_OUT_ABOVE_16(tmp);

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
		CPU_C = CPU_X = GET_MSB_17(tmp);
		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = CPU_X;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_roxr_r_32(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift % 33;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = MASK_OUT_ABOVE_32((ROR_33(src, shift) & ~(1 << (32 - shift))) | ((CPU_X != 0) << (32 - shift)));
	uint new_x_flag = src & (1 << (shift - 1));

	USE_CLKS((orig_shift << 1) + 8);
	if (shift != 0)
	{
		*d_dst = res;
		CPU_X = new_x_flag;
	}
	else
		res = src;
	CPU_C = CPU_X;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_roxr_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
	uint res = MASK_OUT_ABOVE_16(tmp);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_X = GET_MSB_17(tmp);
	CPU_V = 0;
	USE_CLKS(8);
}


void m68000_roxl_s_8(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint tmp = ROL_9(src | ((CPU_X != 0) << 8), shift);
	uint res = MASK_OUT_ABOVE_8(tmp);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_X = GET_MSB_9(tmp);
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_roxl_s_16(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), shift);
	uint res = MASK_OUT_ABOVE_16(tmp);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_X = GET_MSB_17(tmp);
	CPU_V = 0;
	USE_CLKS((shift << 1) + 6);
}


void m68000_roxl_s_32(void)
{
	uint *d_dst = &DY;
	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = MASK_OUT_ABOVE_32((ROL_33(src, shift) & ~(1 << (shift - 1))) | ((CPU_X != 0) << (shift - 1)));
	uint new_x_flag = src & (1 << (32 - shift));

	*d_dst = res;

	CPU_C = CPU_X = new_x_flag;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;

	USE_CLKS((shift << 1) + 6);
}


void m68000_roxl_r_8(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift % 9;
	uint src = MASK_OUT_ABOVE_8(*d_dst);
	uint tmp = ROL_9(src | ((CPU_X != 0) << 8), shift);
	uint res = MASK_OUT_ABOVE_8(tmp);

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
		CPU_C = CPU_X = GET_MSB_9(tmp);
		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = CPU_X;
	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_roxl_r_16(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift % 17;
	uint src = MASK_OUT_ABOVE_16(*d_dst);
	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), shift);
	uint res = MASK_OUT_ABOVE_16(tmp);

	USE_CLKS((orig_shift << 1) + 6);
	if (orig_shift != 0)
	{
		*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
		CPU_C = CPU_X = GET_MSB_17(tmp);
		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = 0;
		return;
	}

	CPU_C = CPU_X;
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_roxl_r_32(void)
{
	uint *d_dst = &DY;
	uint orig_shift = DX & 0x3f;
	uint shift = orig_shift % 33;
	uint src = MASK_OUT_ABOVE_32(*d_dst);
	uint res = MASK_OUT_ABOVE_32((ROL_33(src, shift) & ~(1 << (shift - 1))) | ((CPU_X != 0) << (shift - 1)));
	uint new_x_flag = src & (1 << (32 - shift));

	USE_CLKS((orig_shift << 1) + 8);
	if (shift != 0)
	{
		*d_dst = res;
		CPU_X = new_x_flag;
	}
	else
		res = src;
	CPU_C = CPU_X;
	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = 0;
}


void m68000_roxl_ea(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = m68ki_read_16(ea);
	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
	uint res = MASK_OUT_ABOVE_16(tmp);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_C = CPU_X = GET_MSB_17(tmp);
	CPU_V = 0;
	USE_CLKS(8);
}


void m68010_rtd(void)
{
	if (CPU_MODE & CPU_MODE_010_PLUS)
	{
		uint new_pc = m68ki_pull_32();

		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_A[7] += MAKE_INT_16(m68ki_read_imm_16());
		m68ki_branch_long(new_pc);
		USE_CLKS(16);
		return;
	}
	m68000_illegal();
}


void m68000_rte(void)
{
	uint new_sr;
	uint new_pc;
	uint format_word;

	if (CPU_S)
	{
		m68ki_add_trace(); /* auto-disable (see m68kcpu.h) */
		new_sr = m68ki_pull_16();
		new_pc = m68ki_pull_32();
		m68ki_branch_long(new_pc);
		if (!(CPU_MODE & CPU_MODE_010_PLUS))
		{
			m68ki_set_sr(new_sr);
			USE_CLKS(20);
			return;
		}
		format_word = (m68ki_pull_16() >> 12) & 0xf;
		m68ki_set_sr(new_sr);
		/* I'm ignoring code 8 (bus error and address error) */
		if (format_word != 0)
		/* Generate a new program counter from the format error vector */
			m68ki_set_pc(m68ki_read_32((EXCEPTION_FORMAT_ERROR<<2)+CPU_VBR));
		USE_CLKS(20);
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}




void m68020_rtm(void)
{
	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
		USE_CLKS(19);
		return;
	}
	m68000_illegal();
}


void m68000_rtr(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_set_ccr(m68ki_pull_16());
	m68ki_branch_long(m68ki_pull_32());
	USE_CLKS(20);
}


void m68000_rts(void)
{
	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
	m68ki_branch_long(m68ki_pull_32());
	USE_CLKS(16);
}


void m68000_sbcd_rr(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);

	if (res > 9)
		res -= 6;
	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res += 0xa0;

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | MASK_OUT_ABOVE_8(res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(6);
}


void m68000_sbcd_mm_ax7(void)
{
	uint src = m68ki_read_8(--(AY));
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);

	if (res > 9)
		res -= 6;
	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res += 0xa0;

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_sbcd_mm_ay7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);

	if (res > 9)
		res -= 6;
	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res += 0xa0;

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_sbcd_mm_axy7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);

	if (res > 9)
		res -= 6;
	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res += 0xa0;

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res); /* officially undefined */

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_sbcd_mm(void)
{
	uint src = m68ki_read_8(--AY);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);

	if (res > 9)
		res -= 6;
	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
	if ((CPU_X = CPU_C = res > 0x99) != 0)
		res += 0xa0;

	CPU_N = GET_MSB_8(res); /* officially undefined */

	m68ki_write_8(ea, res);

	if (MASK_OUT_ABOVE_8(res) != 0)
		CPU_NOT_Z = 1;
	USE_CLKS(18);
}


void m68000_st_d(void)
{
	DY |= 0xff;
	USE_CLKS(6);
}


void m68000_st(void)
{
	m68ki_write_8(m68ki_get_ea_8(), 0xff);
	USE_CLKS(8);
}


void m68000_sf_d(void)
{
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_sf(void)
{
	m68ki_write_8(m68ki_get_ea_8(), 0);
	USE_CLKS(8);
}


void m68000_shi_d(void)
{
	if (CONDITION_HI)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_shi(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_HI ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_sls_d(void)
{
	if (CONDITION_LS)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_sls(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_LS ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_scc_d(void)
{
	if (CONDITION_CC)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_scc(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_CC ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_scs_d(void)
{
	if (CONDITION_CS)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_scs(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_CS ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_sne_d(void)
{
	if (CONDITION_NE)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_sne(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_NE ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_seq_d(void)
{
	if (CONDITION_EQ)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_seq(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_EQ ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_svc_d(void)
{
	if (CONDITION_VC)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_svc(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_VC ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_svs_d(void)
{
	if (CONDITION_VS)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_svs(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_VS ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_spl_d(void)
{
	if (CONDITION_PL)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_spl(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_PL ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_smi_d(void)
{
	if (CONDITION_MI)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_smi(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_MI ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_sge_d(void)
{
	if (CONDITION_GE)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_sge(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_GE ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_slt_d(void)
{
	if (CONDITION_LT)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_slt(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_LT ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_sgt_d(void)
{
	if (CONDITION_GT)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_sgt(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_GT ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_sle_d(void)
{
	if (CONDITION_LE)
	{
		DY |= 0xff;
		USE_CLKS(6);
		return;
	}
	DY &= 0xffffff00;
	USE_CLKS(4);
}


void m68000_sle(void)
{
	m68ki_write_8(m68ki_get_ea_8(), CONDITION_LE ? 0xff : 0);
	USE_CLKS(8);
}


void m68000_stop(void)
{
	uint new_sr = m68ki_read_imm_16();

	if (CPU_S)
	{
		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
		CPU_STOPPED = 1;
		m68ki_set_sr(new_sr);
		m68k_clks_left = 0;
		return;
	}
	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
}


void m68000_sub_er_d_8(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_sub_er_8(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_8(m68ki_get_ea_8());
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_sub_er_d_16(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_sub_er_a_16(void)
{
	uint *d_dst = &DX;
	uint src = AY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_sub_er_16(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_16(m68ki_get_ea_16());
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_sub_er_d_32(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_sub_er_a_32(void)
{
	uint *d_dst = &DX;
	uint src = AY;
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_sub_er_32(void)
{
	uint *d_dst = &DX;
	uint src = m68ki_read_32(m68ki_get_ea_32());
	uint dst = *d_dst;
	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(6);
}


void m68000_sub_re_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint src = DX;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_sub_re_16(void)
{
	uint ea = m68ki_get_ea_16();
	uint src = DX;
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(dst - src);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_sub_re_32(void)
{
	uint ea = m68ki_get_ea_32();
	uint src = DX;
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(dst - src);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(12);
}


void m68000_suba_d_16(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(DY));
	USE_CLKS(8);
}


void m68000_suba_a_16(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(AY));
	USE_CLKS(8);
}


void m68000_suba_16(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(m68ki_get_ea_16())));
	USE_CLKS(8);
}


void m68000_suba_d_32(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - DY);
	USE_CLKS(8);
}


void m68000_suba_a_32(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - AY);
	USE_CLKS(8);
}


void m68000_suba_32(void)
{
	uint *a_dst = &AX;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(m68ki_get_ea_32()));
	USE_CLKS(6);
}


void m68000_subi_d_8(void)
{
	uint *d_dst = &DY;
	uint src = m68ki_read_imm_8();
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_subi_8(void)
{
	uint src = m68ki_read_imm_8();
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(12);
}


void m68000_subi_d_16(void)
{
	uint *d_dst = &DY;
	uint src = m68ki_read_imm_16();
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_subi_16(void)
{
	uint src = m68ki_read_imm_16();
	uint ea = m68ki_get_ea_16();
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(dst - src);

	m68ki_write_16(ea, res);
	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(12);
}


void m68000_subi_d_32(void)
{
	uint *d_dst = &DY;
	uint src = m68ki_read_imm_32();
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(16);
}


void m68000_subi_32(void)
{
	uint src = m68ki_read_imm_32();
	uint ea = m68ki_get_ea_32();
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(dst - src);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(20);
}


void m68000_subq_d_8(void)
{
	uint *d_dst = &DY;
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(dst - src);

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_subq_8(void)
{
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src);

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(8);
}


void m68000_subq_d_16(void)
{
	uint *d_dst = &DY;
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(dst - src);

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_subq_a_16(void)
{
	uint *a_dst = &AY;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - ((((CPU_IR >> 9) - 1) & 7) + 1));
	USE_CLKS(8);
}


void m68000_subq_16(void)
{
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint ea = m68ki_get_ea_16();
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(dst - src);

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(8);
}


void m68000_subq_d_32(void)
{
	uint *d_dst = &DY;
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_32(dst - src);

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_subq_a_32(void)
{
	uint *a_dst = &AY;

	*a_dst = MASK_OUT_ABOVE_32(*a_dst - ((((CPU_IR >> 9) - 1) & 7) + 1));
	USE_CLKS(8);
}


void m68000_subq_32(void)
{
	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
	uint ea = m68ki_get_ea_32();
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(dst - src);

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(12);
}


void m68000_subx_rr_8(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));

	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(4);
}


void m68000_subx_rr_16(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_16(dst - src - (CPU_X != 0));

	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;

	CPU_N = GET_MSB_16(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(4);
}


void m68000_subx_rr_32(void)
{
	uint *d_dst = &DX;
	uint src = DY;
	uint dst = *d_dst;
	uint res = MASK_OUT_ABOVE_32(dst - src - (CPU_X != 0));

	*d_dst = res;

	CPU_N = GET_MSB_32(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(8);
}


void m68000_subx_mm_8_ax7(void)
{
	uint src = m68ki_read_8(--AY);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_subx_mm_8_ay7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = --AX;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_subx_mm_8_axy7(void)
{
	uint src = m68ki_read_8(CPU_A[7] -= 2);
	uint ea = CPU_A[7] -= 2;
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_subx_mm_8(void)
{
	uint src = m68ki_read_8(--(AY));
	uint ea = --(AX);
	uint dst = m68ki_read_8(ea);
	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));

	m68ki_write_8(ea, res);

	CPU_N = GET_MSB_8(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
	CPU_V = VFLAG_SUB_8(src, dst, res);
	USE_CLKS(18);
}


void m68000_subx_mm_16(void)
{
	uint src = m68ki_read_16(AY -= 2);
	uint ea = (AX -= 2);
	uint dst = m68ki_read_16(ea);
	uint res = MASK_OUT_ABOVE_16(dst - src - (CPU_X != 0));

	m68ki_write_16(ea, res);

	CPU_N = GET_MSB_16(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
	CPU_V = VFLAG_SUB_16(src, dst, res);
	USE_CLKS(18);
}


void m68000_subx_mm_32(void)
{
	uint src = m68ki_read_32(AY -= 4);
	uint ea = (AX -= 4);
	uint dst = m68ki_read_32(ea);
	uint res = MASK_OUT_ABOVE_32(dst - src - (CPU_X != 0));

	m68ki_write_32(ea, res);

	CPU_N = GET_MSB_32(res);
	if (res != 0)
		CPU_NOT_Z = 1;
	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
	CPU_V = VFLAG_SUB_32(src, dst, res);
	USE_CLKS(30);
}


void m68000_swap(void)
{
	uint *d_dst = &DY;

	*d_dst ^= (*d_dst >> 16) & 0x0000ffff;
	*d_dst ^= (*d_dst << 16) & 0xffff0000;
	*d_dst ^= (*d_dst >> 16) & 0x0000ffff;

	CPU_N = GET_MSB_32(*d_dst);
	CPU_NOT_Z = *d_dst;
	CPU_C = CPU_V = 0;
	USE_CLKS(4);
}


void m68000_tas_d(void)
{
	uint *d_dst = &DY;

	CPU_NOT_Z = MASK_OUT_ABOVE_8(*d_dst);
	CPU_N = GET_MSB_8(*d_dst);
	CPU_V = CPU_C = 0;
	*d_dst |= 0x80;
	USE_CLKS(4);
}


void m68000_tas(void)
{
	uint ea = m68ki_get_ea_8();
	uint dst = m68ki_read_8(ea);

	CPU_NOT_Z = dst;
	CPU_N = GET_MSB_8(dst);
	CPU_V = CPU_C = 0;
	m68ki_write_8(ea, dst | 0x80);
	USE_CLKS(14);
}


void m68000_trap(void)
{
	m68ki_interrupt(EXCEPTION_TRAP_BASE + (CPU_IR & 0xf));	/* HJB 990403 */
}


void m68020_trapt_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		return;
	}
	m68000_illegal();
}


void m68020_trapt_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		return;
	}
	m68000_illegal();
}


void m68020_trapt_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		return;
	}
	m68000_illegal();
}


void m68020_trapf_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapf_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapf_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_traphi_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_HI)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_traphi_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_HI)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_traphi_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_HI)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapls_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapls_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapls_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapcc_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CC)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapcc_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CC)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapcc_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CC)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapcs_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapcs_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapcs_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_CS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapne_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_NE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapne_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_NE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapne_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_NE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapeq_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_EQ)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapeq_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_EQ)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapeq_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_EQ)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapvc_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VC)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapvc_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VC)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapvc_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VC)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapvs_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapvs_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapvs_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_VS)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trappl_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_PL)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trappl_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_PL)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trappl_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_PL)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapmi_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_MI)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapmi_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_MI)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapmi_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_MI)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapge_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapge_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapge_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_traplt_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LT)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_traplt_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LT)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_traplt_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LT)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_trapgt_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GT)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_trapgt_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GT)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_trapgt_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_GT)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68020_traple_0(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_traple_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(6);
		CPU_PC += 2;
		return;
	}
	m68000_illegal();
}


void m68020_traple_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		if (CONDITION_LE)
			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
		else
			USE_CLKS(8);
		CPU_PC += 4;
		return;
	}
	m68000_illegal();
}


void m68000_trapv(void)
{
	if (!CPU_V)
	{
		USE_CLKS(4);
		return;
	}
	m68ki_interrupt(EXCEPTION_TRAPV);  /* HJB 990403 */
}


void m68000_tst_d_8(void)
{
	uint res = MASK_OUT_ABOVE_8(DY);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68000_tst_8(void)
{
	uint ea = m68ki_get_ea_8();
	uint res = m68ki_read_8(ea);

	CPU_N = GET_MSB_8(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68020_tst_pcdi_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint old_pc = (CPU_PC += 2) - 2;
		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
		uint res = m68ki_read_8(ea);

		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_tst_pcix_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = m68ki_read_8(EA_PCIX);

		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_tst_imm_8(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = m68ki_read_imm_8();

		CPU_N = GET_MSB_8(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68000_tst_d_16(void)
{
	uint res = MASK_OUT_ABOVE_16(DY);

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68020_tst_a_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = MASK_OUT_ABOVE_16(AY);

		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68000_tst_16(void)
{
	uint res = m68ki_read_16(m68ki_get_ea_16());

	CPU_N = GET_MSB_16(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68020_tst_pcdi_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint old_pc = (CPU_PC += 2) - 2;
		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
		uint res = m68ki_read_16(ea);

		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_tst_pcix_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = m68ki_read_16(EA_PCIX);

		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_tst_imm_16(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = m68ki_read_imm_16();

		CPU_N = GET_MSB_16(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68000_tst_d_32(void)
{
	uint res = DY;

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68020_tst_a_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = AY;

		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68000_tst_32(void)
{
	uint res = m68ki_read_32(m68ki_get_ea_32());

	CPU_N = GET_MSB_32(res);
	CPU_NOT_Z = res;
	CPU_V = CPU_C = 0;
	USE_CLKS(4);
}


void m68020_tst_pcdi_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint old_pc = (CPU_PC += 2) - 2;
		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
		uint res = m68ki_read_32(ea);

		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_tst_pcix_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = m68ki_read_32(EA_PCIX);

		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68020_tst_imm_32(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint res = m68ki_read_imm_32();

		CPU_N = GET_MSB_32(res);
		CPU_NOT_Z = res;
		CPU_V = CPU_C = 0;
		USE_CLKS(4);
		return;
	}
	m68000_illegal();
}


void m68000_unlk_a7(void)
{
	CPU_A[7] = m68ki_read_32(CPU_A[7]);
	USE_CLKS(12);
}


void m68000_unlk(void)
{
	uint *a_dst = &AY;

	CPU_A[7] = *a_dst;
	*a_dst = m68ki_pull_32();
	USE_CLKS(12);
}


void m68020_unpk_rr(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = DX;

		DY = MASK_OUT_BELOW_16(DY) | (((((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16()) & 0xffff);
		USE_CLKS(8);
		return;
	}
	m68000_illegal();
}


void m68020_unpk_mm_ax7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_8(--AX);

		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
		m68ki_write_8(CPU_A[7] -= 2, (src >> 8) & 0xff);
		m68ki_write_8(CPU_A[7] -= 2, src & 0xff);
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68020_unpk_mm_ay7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_8(CPU_A[7] -= 2);

		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
		m68ki_write_8(--AY, (src >> 8) & 0xff);
		m68ki_write_8(--AY, src & 0xff);
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68020_unpk_mm_axy7(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_8(CPU_A[7] -= 2);

		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
		m68ki_write_8(CPU_A[7] -= 2, (src >> 8) & 0xff);
		m68ki_write_8(CPU_A[7] -= 2, src & 0xff);
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}


void m68020_unpk_mm(void)
{
	if (CPU_MODE & CPU_MODE_EC020_PLUS)
	{
		uint src = m68ki_read_8(--AX);

		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
		m68ki_write_8(--AY, (src >> 8) & 0xff);
		m68ki_write_8(--AY, src & 0xff);
		USE_CLKS(13);
		return;
	}
	m68000_illegal();
}
