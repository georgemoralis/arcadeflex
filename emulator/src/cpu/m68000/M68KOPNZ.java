package cpu.m68000;

import static cpu.m68000.m68kcpu.*;
import static arcadeflex.libc_old.*;
import static cpu.m68000.m68kcpuH.*;
import static cpu.m68000.m68kopsH.*;

public class M68KOPNZ {

    /*TODO*///void m68000_nbcd_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_pi7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_pd7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nbcd_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(0x9a - dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	if (res != 0x9a)
/*TODO*///	{
/*TODO*///		if ((res & 0x0f) == 0xa)
/*TODO*///			res = (res & 0xf0) + 0x10;
/*TODO*///
/*TODO*///		m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///		if (res != 0)
/*TODO*///			CPU_NOT_Z = 1;
/*TODO*///		CPU_C = CPU_X = 1;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		CPU_C = CPU_X = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_neg_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = res != 0;
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_8(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_8(dst | res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_16(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_16(dst | res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_negx_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(-dst - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_V = GET_MSB_32(dst & res);
/*TODO*///	CPU_C = CPU_X = GET_MSB_32(dst | res);
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_nop(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_not_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~*d_dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_not_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(~m68ki_read_8(ea));
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
/*TODO*///void m68000_not_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~*d_dst);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_not_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(~m68ki_read_16(ea));
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
/*TODO*///void m68000_not_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(~*d_dst);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_not_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_not_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_not_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_not_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_not_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_not_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_not_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(~m68ki_read_32(ea));
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
/*TODO*///void m68000_or_er_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= MASK_OUT_ABOVE_8(DY)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_ai_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_AI)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pi_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_PI_8)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pi7_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_PI7_8)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pd_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_PD_8)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pd7_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_PD7_8)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_di_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_DI)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_ix_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_IX)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_aw_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_AW)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_al_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_AL)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(ea)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pcix_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_8(EA_PCIX)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_i_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DX |= m68ki_read_imm_8()));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= MASK_OUT_ABOVE_16(DY)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_AI)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_PI_16)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_PD_16)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_di_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_DI)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_IX)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_AW)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_al_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_AL)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(ea)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pcix_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_16(EA_PCIX)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_i_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16((DX |= m68ki_read_imm_16()));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= DY));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_AI)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_PI_32)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_PD_32)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_di_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_DI)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_IX)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_AW)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_al_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_AL)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(ea)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_pcix_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_32(EA_PCIX)));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_er_i_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32((DX |= m68ki_read_imm_32()));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_or_re_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DX | m68ki_read_8(ea));
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
/*TODO*///void m68000_or_re_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
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
/*TODO*///void m68000_or_re_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
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
/*TODO*///void m68000_or_re_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
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
/*TODO*///void m68000_or_re_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
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
/*TODO*///void m68000_or_re_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
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
    public static opcode m68000_or_re_aw_16 = new opcode() {
        public void handler() {
            long ea = EA_AW();
            long res = MASK_OUT_ABOVE_16(get_DX() | m68ki_read_16(ea));

            m68ki_write_16(ea, res);

            m68k_cpu.n_flag = GET_MSB_16(res);
            m68k_cpu.not_z_flag = res;
            m68k_cpu.c_flag = m68k_cpu.v_flag = 0;
            USE_CLKS(8 + 8);
            if (m68klog != null) {
                fprintf(m68klog, "m68000_or_re_aw_16 :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
            }
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///void m68000_or_re_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DX | m68ki_read_16(ea));
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
/*TODO*///void m68000_or_re_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_or_re_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_or_re_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_or_re_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_or_re_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_or_re_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_or_re_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DX | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8((DY |= m68ki_read_imm_8()));
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ori_ai_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_pi_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_pi7_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_pd_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_pd7_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_di_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_ix_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_aw_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_al_8(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp | m68ki_read_8(ea));
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
/*TODO*///void m68000_ori_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY |= m68ki_read_imm_16());
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ori_ai_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_pi_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_pd_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_di_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_ix_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_aw_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_al_16(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp | m68ki_read_16(ea));
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
/*TODO*///void m68000_ori_d_32(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_32(DY |= m68ki_read_imm_32());
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ori_ai_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_pi_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_pd_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_di_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_ix_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_aw_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_al_32(void)
/*TODO*///{
/*TODO*///	uint tmp = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(tmp | m68ki_read_32(ea));
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
/*TODO*///void m68000_ori_to_ccr(void)
/*TODO*///{
/*TODO*///	m68ki_set_ccr(m68ki_get_ccr() | m68ki_read_imm_16());
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ori_to_sr(void)
/*TODO*///{
/*TODO*///	uint or_val = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		m68ki_set_sr(m68ki_get_sr() | or_val);
/*TODO*///		USE_CLKS(20);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_pack_rr(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = DX + m68ki_read_imm_16();
/*TODO*///
/*TODO*///		DY = MASK_OUT_BELOW_8(DY) | ((src >> 4) & 0x00f0) | (src & 0x000f);
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_pack_mm_ax7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_16(AX -= 2);
/*TODO*///
/*TODO*///		src = (((src >> 8) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(CPU_A[7] -= 2, ((src >> 4) & 0xf0) | (src & 0x0f));
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_pack_mm_ay7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_32(CPU_A[7] -= 4);
/*TODO*///
/*TODO*///		src = (((src >> 16) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
/*TODO*///		/* I hate the way Motorola changes where Rx and Ry are */
/*TODO*///		m68ki_write_8(--AY, ((src >> 4) & 0xf0) | (src & 0x0f));
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_pack_mm_axy7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_32(CPU_A[7] -= 4);
/*TODO*///
/*TODO*///		src = (((src >> 16) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(CPU_A[7] -= 2, ((src >> 4) & 0xf0) | (src & 0x0f));
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_pack_mm(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_16(AX -= 2);
/*TODO*///
/*TODO*///		src = (((src >> 8) & 0x00ff) | ((src << 8) & 0xff00)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(--AY, ((src >> 4) & 0xf0) | (src & 0x0f));
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_pcdi(void)
/*TODO*///{
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_pea_pcix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PCIX;
/*TODO*///
/*TODO*///	m68ki_push_32(ea);
/*TODO*///	USE_CLKS(0+20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rst(void)
/*TODO*///{
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_output_reset();		   /* auto-disable (see m68kcpu.h) */
/*TODO*///		USE_CLKS(132);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint shift = orig_shift & 7;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = ROR_8(src, shift);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = (src >> ((shift - 1) & 7)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = ROR_16(src, shift);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = ROR_32(src, shift);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = (src >> (shift - 1)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift & 7;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = ROR_8(src, shift);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///		CPU_C = (src >> ((shift - 1) & 7)) & 1;
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
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
/*TODO*///void m68000_ror_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift & 15;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = ROR_16(src, shift);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///		CPU_C = (src >> ((shift - 1) & 15)) & 1;
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
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
/*TODO*///void m68000_ror_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift & 31;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = ROR_32(src, shift);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 8);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = res;
/*TODO*///		CPU_C = (src >> ((shift - 1) & 31)) & 1;
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
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
/*TODO*///void m68000_ror_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_ror_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = ROR_16(src, 1);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = src & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint shift = orig_shift & 7;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(ROL_8(src, shift));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = (src >> (8 - orig_shift)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, shift));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = (src >> (16 - shift)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(ROL_32(src, shift));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = (src >> (32 - shift)) & 1;
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift & 7;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(ROL_8(src, shift));
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		if (shift != 0)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///			CPU_C = (src >> (8 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_8(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_C = src & 1;
/*TODO*///		CPU_N = GET_MSB_8(src);
/*TODO*///		CPU_NOT_Z = src;
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
/*TODO*///void m68000_rol_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift & 15;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, shift));
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		if (shift != 0)
/*TODO*///		{
/*TODO*///			*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///			CPU_C = (src >> (16 - shift)) & 1;
/*TODO*///			CPU_N = GET_MSB_16(res);
/*TODO*///			CPU_NOT_Z = res;
/*TODO*///			CPU_V = 0;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		CPU_C = src & 1;
/*TODO*///		CPU_N = GET_MSB_16(src);
/*TODO*///		CPU_NOT_Z = src;
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
/*TODO*///void m68000_rol_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift & 31;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(ROL_32(src, shift));
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 8);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = res;
/*TODO*///
/*TODO*///		CPU_C = (src >> (32 - shift)) & 1;
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
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
/*TODO*///void m68000_rol_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rol_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(ROL_16(src, 1));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = GET_MSB_16(src);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint tmp = ROR_9(src | ((CPU_X != 0) << 8), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_9(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_32((ROR_33(src, shift) & ~(1 << (32 - shift))) | ((CPU_X != 0) << (32 - shift)));
/*TODO*///	uint new_x_flag = src & (1 << (shift - 1));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_C = CPU_X = new_x_flag;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift % 9;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint tmp = ROR_9(src | ((CPU_X != 0) << 8), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///		CPU_C = CPU_X = GET_MSB_9(tmp);
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = CPU_X;
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift % 17;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///		CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = CPU_X;
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift % 33;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_32((ROR_33(src, shift) & ~(1 << (32 - shift))) | ((CPU_X != 0) << (32 - shift)));
/*TODO*///	uint new_x_flag = src & (1 << (shift - 1));
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 8);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = res;
/*TODO*///		CPU_X = new_x_flag;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		res = src;
/*TODO*///	CPU_C = CPU_X;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxr_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROR_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_s_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint tmp = ROL_9(src | ((CPU_X != 0) << 8), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_9(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_s_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_s_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint shift = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_32((ROL_33(src, shift) & ~(1 << (shift - 1))) | ((CPU_X != 0) << (shift - 1)));
/*TODO*///	uint new_x_flag = src & (1 << (32 - shift));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_C = CPU_X = new_x_flag;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///
/*TODO*///	USE_CLKS((shift << 1) + 6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_r_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift % 9;
/*TODO*///	uint src = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	uint tmp = ROL_9(src | ((CPU_X != 0) << 8), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(tmp);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///		CPU_C = CPU_X = GET_MSB_9(tmp);
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = CPU_X;
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_r_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift % 17;
/*TODO*///	uint src = MASK_OUT_ABOVE_16(*d_dst);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), shift);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 6);
/*TODO*///	if (orig_shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///		CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	CPU_C = CPU_X;
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_r_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint orig_shift = DX & 0x3f;
/*TODO*///	uint shift = orig_shift % 33;
/*TODO*///	uint src = MASK_OUT_ABOVE_32(*d_dst);
/*TODO*///	uint res = MASK_OUT_ABOVE_32((ROL_33(src, shift) & ~(1 << (shift - 1))) | ((CPU_X != 0) << (shift - 1)));
/*TODO*///	uint new_x_flag = src & (1 << (32 - shift));
/*TODO*///
/*TODO*///	USE_CLKS((orig_shift << 1) + 8);
/*TODO*///	if (shift != 0)
/*TODO*///	{
/*TODO*///		*d_dst = res;
/*TODO*///		CPU_X = new_x_flag;
/*TODO*///	}
/*TODO*///	else
/*TODO*///		res = src;
/*TODO*///	CPU_C = CPU_X;
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = 0;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_roxl_ea_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint tmp = ROL_17(src | ((CPU_X != 0) << 16), 1);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(tmp);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_C = CPU_X = GET_MSB_17(tmp);
/*TODO*///	CPU_V = 0;
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68010_rtd(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_010_PLUS)
/*TODO*///	{
/*TODO*///		uint new_pc = m68ki_pull_32();
/*TODO*///
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_A[7] += MAKE_INT_16(m68ki_read_imm_16());
/*TODO*///		m68ki_branch_long(new_pc);
/*TODO*///		USE_CLKS(16);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rte(void)
/*TODO*///{
/*TODO*///	uint new_sr;
/*TODO*///	uint new_pc;
/*TODO*///	uint format_word;
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace(); /* auto-disable (see m68kcpu.h) */
/*TODO*///		new_sr = m68ki_pull_16();
/*TODO*///		new_pc = m68ki_pull_32();
/*TODO*///		m68ki_branch_long(new_pc);
/*TODO*///		if (!(CPU_MODE & CPU_MODE_010_PLUS))
/*TODO*///		{
/*TODO*///			m68ki_set_sr(new_sr);
/*TODO*///			USE_CLKS(20);
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		format_word = (m68ki_pull_16() >> 12) & 0xf;
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		/* I'm ignoring code 8 (bus error and address error) */
/*TODO*///		if (format_word != 0)
/*TODO*///		/* Generate a new program counter from the format error vector */
/*TODO*///			m68ki_set_pc(m68ki_read_32((EXCEPTION_FORMAT_ERROR<<2)+CPU_VBR));
/*TODO*///		USE_CLKS(20);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_rtm(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & (CPU_MODE_EC020 | CPU_MODE_020))
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		M68K_DO_LOG((M68K_LOG, "%s at %08x: called unimplemented instruction %04x (%s)\n",
/*TODO*///					 m68k_cpu_names[CPU_MODE], ADDRESS_68K(CPU_PC - 2), CPU_IR,
/*TODO*///					 m68k_disassemble_quick(ADDRESS_68K(CPU_PC - 2))));
/*TODO*///		USE_CLKS(19);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rtr(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_set_ccr(m68ki_pull_16());
/*TODO*///	m68ki_branch_long(m68ki_pull_32());
/*TODO*///	USE_CLKS(20);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_rts(void)
/*TODO*///{
/*TODO*///	m68ki_add_trace();				   /* auto-disable (see m68kcpu.h) */
/*TODO*///	m68ki_branch_long(m68ki_pull_32());
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sbcd_rr(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res -= 6;
/*TODO*///	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res += 0xa0;
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
/*TODO*///void m68000_sbcd_mm_ax7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--(AY));
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res -= 6;
/*TODO*///	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res += 0xa0;
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
/*TODO*///void m68000_sbcd_mm_ay7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res -= 6;
/*TODO*///	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res += 0xa0;
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
/*TODO*///void m68000_sbcd_mm_axy7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res -= 6;
/*TODO*///	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res += 0xa0;
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
/*TODO*///void m68000_sbcd_mm(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--AY);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = LOW_NIBBLE(dst) - LOW_NIBBLE(src) - (CPU_X != 0);
/*TODO*///
/*TODO*///	if (res > 9)
/*TODO*///		res -= 6;
/*TODO*///	res += HIGH_NIBBLE(dst) - HIGH_NIBBLE(src);
/*TODO*///	if ((CPU_X = CPU_C = res > 0x99) != 0)
/*TODO*///		res += 0xa0;
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
/*TODO*///void m68000_st_d(void)
/*TODO*///{
/*TODO*///	DY |= 0xff;
/*TODO*///	USE_CLKS(6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, 0xff);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, 0xff);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, 0xff);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, 0xff);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, 0xff);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, 0xff);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, 0xff);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, 0xff);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_st_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, 0xff);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_d(void)
/*TODO*///{
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sf_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_HI)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_shi_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_HI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LS)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sls_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_LS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_CC)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scc_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_CC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_CS)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_scs_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_CS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_NE)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sne_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_NE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_EQ)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_seq_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_EQ ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_VC)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svc_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_VC ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_VS)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_svs_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_VS ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_PL)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_spl_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_PL ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_MI)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_smi_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_MI ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_GE)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sge_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_GE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LT)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_slt_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_LT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_GT)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sgt_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_GT ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_d(void)
/*TODO*///{
/*TODO*///	if (CONDITION_LE)
/*TODO*///	{
/*TODO*///		DY |= 0xff;
/*TODO*///		USE_CLKS(6);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	DY &= 0xffffff00;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_ai(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AI, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_pi(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI_8, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_pi7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PI7_8, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_pd(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD_8, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_pd7(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_PD7_8, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_di(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_DI, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_ix(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_IX, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_aw(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AW, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sle_al(void)
/*TODO*///{
/*TODO*///	m68ki_write_8(EA_AL, CONDITION_LE ? 0xff : 0);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_stop(void)
/*TODO*///{
/*TODO*///	uint new_sr = m68ki_read_imm_16();
/*TODO*///
/*TODO*///	if (CPU_S)
/*TODO*///	{
/*TODO*///		m68ki_add_trace();			   /* auto-disable (see m68kcpu.h) */
/*TODO*///		CPU_STOPPED = 1;
/*TODO*///		m68ki_set_sr(new_sr);
/*TODO*///		m68k_clks_left = 0;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_exception(EXCEPTION_PRIVILEGE_VIOLATION);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_ai_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_AI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pi_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PI_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pi7_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PI7_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pd_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PD_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pd7_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PD7_8);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_di_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_DI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_ix_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_IX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_aw_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_AW);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_al_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_AL);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pcdi_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_8(ea);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pcix_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_8(EA_PCIX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_i_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_a_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_ai_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PI_16);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pd_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PD_16);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_di_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_DI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_ix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_IX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_aw_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AW);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_al_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_AL);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_16(ea);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_16(EA_PCIX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_i_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_a_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = AY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_ai_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_AI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pi_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_PI_32);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pd_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_PD_32);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_di_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_DI);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_ix_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_IX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
    public static opcode m68000_sub_er_aw_32 = new opcode() {
        public void handler() {
            long d_dst = get_DX();
            long src = m68ki_read_32(EA_AW());
            long dst = d_dst;
            set_DX(MASK_OUT_ABOVE_32(dst - src));
            long res = get_DX();

            m68k_cpu.n_flag = GET_MSB_32(res);
            m68k_cpu.not_z_flag = res;
            m68k_cpu.x_flag = m68k_cpu.c_flag = CFLAG_SUB_32(src, dst, res);
            m68k_cpu.v_flag = VFLAG_SUB_32(src, dst, res);
            USE_CLKS(6 + 12);
            if (m68klog != null) {
                fprintf(m68klog, "m68000_sub_er_aw_32 :PC:%d,PPC:%d,mode:%d,dr0:%d,dr1:%d,dr2:%d,dr3:%d,dr4:%d,dr5:%d,dr6:%d,dr7:%d,ar0:%d,ar1:%d,ar2:%d,ar3:%d,ar4:%d,ar5:%d,ar6:%d,ar7:%d,sp0:%d,sp1:%d,sp2:%d,sp3:%d,vbr:%d,sfc:%d,dfc:%d,cacr:%d,caar:%d,ir:%d,t1:%d,t0:%d,s:%d,m:%d,x:%d,n:%d,nz:%d,v:%d,c:%d,intm:%d,ints:%d,stop:%d,halt:%d,intc:%d,prefa:%d,prefd:%d\n", m68k_cpu.pc, m68k_cpu.ppc, m68k_cpu.mode, m68k_cpu.dr[0], m68k_cpu.dr[1], m68k_cpu.dr[2], m68k_cpu.dr[3], m68k_cpu.dr[4], m68k_cpu.dr[5], m68k_cpu.dr[6], m68k_cpu.dr[7], m68k_cpu.ar[0], m68k_cpu.ar[1], m68k_cpu.ar[2], m68k_cpu.ar[3], m68k_cpu.ar[4], m68k_cpu.ar[5], m68k_cpu.ar[6], m68k_cpu.ar[7], m68k_cpu.sp[0], m68k_cpu.sp[1], m68k_cpu.sp[2], m68k_cpu.sp[3], m68k_cpu.vbr, m68k_cpu.sfc, m68k_cpu.dfc, m68k_cpu.cacr, m68k_cpu.caar, m68k_cpu.ir, m68k_cpu.t1_flag, m68k_cpu.t0_flag, m68k_cpu.s_flag, m68k_cpu.m_flag, m68k_cpu.x_flag, m68k_cpu.n_flag, m68k_cpu.not_z_flag, m68k_cpu.v_flag, m68k_cpu.c_flag, m68k_cpu.int_mask, m68k_cpu.int_state, m68k_cpu.stopped, m68k_cpu.halted, m68k_cpu.int_cycles, m68k_cpu.pref_addr, m68k_cpu.pref_data);
            }
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_al_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_AL);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	uint src = m68ki_read_32(ea);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_pcix_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_32(EA_PCIX);
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_er_i_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = *d_dst = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_ai_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pi_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pd_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_di_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_ix_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_aw_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_al_16(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_ai_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pi_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_pd_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_di_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_ix_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_aw_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_sub_re_al_32(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint src = DX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_d_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(DY));
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_a_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(AY));
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_ai_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_AI)));
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pi_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_PI_16)));
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pd_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_PD_16)));
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_di_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_DI)));
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_ix_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_IX)));
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_aw_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_AW)));
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_al_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_AL)));
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pcdi_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(ea)));
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pcix_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_16(EA_PCIX)));
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_i_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - MAKE_INT_16(m68ki_read_imm_16()));
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_d_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - DY);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_a_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - AY);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_ai_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_AI));
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pi_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_PI_32));
/*TODO*///	USE_CLKS(6+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pd_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_PD_32));
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_di_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_DI));
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_ix_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_IX));
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_aw_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_AW));
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_al_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_AL));
/*TODO*///	USE_CLKS(6+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pcdi_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	uint old_pc = (CPU_PC+=2) - 2;
/*TODO*///	uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(ea));
/*TODO*///	USE_CLKS(6+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_pcix_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_32(EA_PCIX));
/*TODO*///	USE_CLKS(6+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_suba_i_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AX;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - m68ki_read_imm_32());
/*TODO*///	USE_CLKS(6+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_ai_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pi_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pi7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pd_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pd7_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_di_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_ix_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_aw_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_al_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_8();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_di_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_al_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_16();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_di_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subi_al_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_imm_32();
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(20+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_d_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_ai_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pi_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pi7_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pd_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pd7_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_di_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_ix_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_aw_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_al_8(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_d_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_a_16(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AY;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - ((((CPU_IR >> 9) - 1) & 7) + 1));
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_ai_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pi_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pd_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD_16;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_di_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_ix_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_aw_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_al_16(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(8+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_d_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_a_32(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AY;
/*TODO*///
/*TODO*///	*a_dst = MASK_OUT_ABOVE_32(*a_dst - ((((CPU_IR >> 9) - 1) & 7) + 1));
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_ai_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pi_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PI_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_pd_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_PD_32;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_di_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_ix_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_aw_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subq_al_32(void)
/*TODO*///{
/*TODO*///	uint src = (((CPU_IR >> 9) - 1) & 7) + 1;
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src);
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(12+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_rr_8(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_8(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_rr_16(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = MASK_OUT_BELOW_16(*d_dst) | res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_rr_32(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DX;
/*TODO*///	uint src = DY;
/*TODO*///	uint dst = *d_dst;
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	*d_dst = res;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_mm_8_ax7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--AY);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_mm_8_ay7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = --AX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_mm_8_axy7(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///	uint ea = CPU_A[7] -= 2;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_mm_8(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_8(--(AY));
/*TODO*///	uint ea = --(AX);
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_8(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_8(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_8(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_8(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_mm_16(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_16(AY -= 2);
/*TODO*///	uint ea = (AX -= 2);
/*TODO*///	uint dst = m68ki_read_16(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_16(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_16(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_16(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_16(src, dst, res);
/*TODO*///	USE_CLKS(18);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_subx_mm_32(void)
/*TODO*///{
/*TODO*///	uint src = m68ki_read_32(AY -= 4);
/*TODO*///	uint ea = (AX -= 4);
/*TODO*///	uint dst = m68ki_read_32(ea);
/*TODO*///	uint res = MASK_OUT_ABOVE_32(dst - src - (CPU_X != 0));
/*TODO*///
/*TODO*///	m68ki_write_32(ea, res);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	if (res != 0)
/*TODO*///		CPU_NOT_Z = 1;
/*TODO*///	CPU_X = CPU_C = CFLAG_SUB_32(src, dst, res);
/*TODO*///	CPU_V = VFLAG_SUB_32(src, dst, res);
/*TODO*///	USE_CLKS(30);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_swap(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///
/*TODO*///	*d_dst ^= (*d_dst >> 16) & 0x0000ffff;
/*TODO*///	*d_dst ^= (*d_dst << 16) & 0xffff0000;
/*TODO*///	*d_dst ^= (*d_dst >> 16) & 0x0000ffff;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(*d_dst);
/*TODO*///	CPU_NOT_Z = *d_dst;
/*TODO*///	CPU_C = CPU_V = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_d(void)
/*TODO*///{
/*TODO*///	uint *d_dst = &DY;
/*TODO*///
/*TODO*///	CPU_NOT_Z = MASK_OUT_ABOVE_8(*d_dst);
/*TODO*///	CPU_N = GET_MSB_8(*d_dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	*d_dst |= 0x80;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_ai(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_pi(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_pi7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_pd(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_pd7(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_di(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_ix(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_aw(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tas_al(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint dst = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_NOT_Z = dst;
/*TODO*///	CPU_N = GET_MSB_8(dst);
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	m68ki_write_8(ea, dst | 0x80);
/*TODO*///	USE_CLKS(14+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_trap(void)
/*TODO*///{
/*TODO*///	m68ki_interrupt(EXCEPTION_TRAP_BASE + (CPU_IR & 0xf));	/* HJB 990403 */
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapt_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapt_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapt_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapf_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapf_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapf_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traphi_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_HI)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traphi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_HI)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traphi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_HI)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapls_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapls_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapls_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapcc_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CC)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapcc_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CC)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapcc_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CC)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapcs_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapcs_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapcs_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_CS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapne_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_NE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapne_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_NE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapne_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_NE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapeq_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_EQ)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapeq_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_EQ)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapeq_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_EQ)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapvc_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VC)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapvc_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VC)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapvc_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VC)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapvs_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapvs_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapvs_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_VS)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trappl_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_PL)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trappl_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_PL)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trappl_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_PL)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapmi_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_MI)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapmi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_MI)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapmi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_MI)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapge_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapge_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapge_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traplt_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LT)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traplt_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LT)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traplt_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LT)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapgt_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GT)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapgt_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GT)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_trapgt_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_GT)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traple_0(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traple_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(6);
/*TODO*///		CPU_PC += 2;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_traple_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		if (CONDITION_LE)
/*TODO*///			m68ki_interrupt(EXCEPTION_TRAPV);	/* HJB 990403 */
/*TODO*///		else
/*TODO*///			USE_CLKS(8);
/*TODO*///		CPU_PC += 4;
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_trapv(void)
/*TODO*///{
/*TODO*///	if (!CPU_V)
/*TODO*///	{
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68ki_interrupt(EXCEPTION_TRAPV);  /* HJB 990403 */
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_d_8(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_8(DY);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_ai_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AI;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pi_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI_8;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pi7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PI7_8;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pd_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD_8;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pd7_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_PD7_8;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_di_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_DI;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_ix_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_IX;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_aw_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AW;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_al_8(void)
/*TODO*///{
/*TODO*///	uint ea = EA_AL;
/*TODO*///	uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_8(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_pcdi_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint old_pc = (CPU_PC += 2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint res = m68ki_read_8(ea);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_pcix_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = m68ki_read_8(EA_PCIX);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_imm_8(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = m68ki_read_imm_8();
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_8(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_d_16(void)
/*TODO*///{
/*TODO*///	uint res = MASK_OUT_ABOVE_16(DY);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_a_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = MASK_OUT_ABOVE_16(AY);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_ai_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AI);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pi_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PI_16);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pd_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_PD_16);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+6);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_di_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_DI);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_ix_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_IX);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_aw_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AW);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_al_16(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_16(EA_AL);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_16(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_pcdi_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint old_pc = (CPU_PC += 2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint res = m68ki_read_16(ea);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_pcix_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = m68ki_read_16(EA_PCIX);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_imm_16(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = m68ki_read_imm_16();
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_16(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_d_32(void)
/*TODO*///{
/*TODO*///	uint res = DY;
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_a_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = AY;
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_ai_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AI);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pi_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PI_32);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+8);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_pd_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_PD_32);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+10);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_di_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_DI);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_ix_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_IX);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+14);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_aw_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AW);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_tst_al_32(void)
/*TODO*///{
/*TODO*///	uint res = m68ki_read_32(EA_AL);
/*TODO*///
/*TODO*///	CPU_N = GET_MSB_32(res);
/*TODO*///	CPU_NOT_Z = res;
/*TODO*///	CPU_V = CPU_C = 0;
/*TODO*///	USE_CLKS(4+16);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_pcdi_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint old_pc = (CPU_PC += 2) - 2;
/*TODO*///		uint ea = old_pc + MAKE_INT_16(m68ki_read_16(old_pc));
/*TODO*///		uint res = m68ki_read_32(ea);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_pcix_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = m68ki_read_32(EA_PCIX);
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_tst_imm_32(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint res = m68ki_read_imm_32();
/*TODO*///
/*TODO*///		CPU_N = GET_MSB_32(res);
/*TODO*///		CPU_NOT_Z = res;
/*TODO*///		CPU_V = CPU_C = 0;
/*TODO*///		USE_CLKS(4);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_unlk_a7(void)
/*TODO*///{
/*TODO*///	CPU_A[7] = m68ki_read_32(CPU_A[7]);
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68000_unlk(void)
/*TODO*///{
/*TODO*///	uint *a_dst = &AY;
/*TODO*///
/*TODO*///	CPU_A[7] = *a_dst;
/*TODO*///	*a_dst = m68ki_pull_32();
/*TODO*///	USE_CLKS(12);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_unpk_rr(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = DX;
/*TODO*///
/*TODO*///		DY = MASK_OUT_BELOW_16(DY) | (((((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16()) & 0xffff);
/*TODO*///		USE_CLKS(8);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_unpk_mm_ax7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_8(--AX);
/*TODO*///
/*TODO*///		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(CPU_A[7] -= 2, (src >> 8) & 0xff);
/*TODO*///		m68ki_write_8(CPU_A[7] -= 2, src & 0xff);
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_unpk_mm_ay7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///
/*TODO*///		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(--AY, (src >> 8) & 0xff);
/*TODO*///		m68ki_write_8(--AY, src & 0xff);
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_unpk_mm_axy7(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_8(CPU_A[7] -= 2);
/*TODO*///
/*TODO*///		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(CPU_A[7] -= 2, (src >> 8) & 0xff);
/*TODO*///		m68ki_write_8(CPU_A[7] -= 2, src & 0xff);
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void m68020_unpk_mm(void)
/*TODO*///{
/*TODO*///	if (CPU_MODE & CPU_MODE_EC020_PLUS)
/*TODO*///	{
/*TODO*///		uint src = m68ki_read_8(--AX);
/*TODO*///
/*TODO*///		src = (((src << 4) & 0x0f00) | (src & 0x000f)) + m68ki_read_imm_16();
/*TODO*///		m68ki_write_8(--AY, (src >> 8) & 0xff);
/*TODO*///		m68ki_write_8(--AY, src & 0xff);
/*TODO*///		USE_CLKS(13);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///	m68000_illegal();
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
}
