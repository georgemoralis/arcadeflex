/*** TMS34010: Portable TMS34010 emulator ************************************

	Copyright (C) Alex Pasadyn/Zsolt Vasvari 1998

    Opcodes

	Most of the opcodes were painfully tested to produce the fastest
	possible code on DJGPP. If you make any changes, please test for
	performace. Even a trivial change such as reordering a couple of
	lines can have a change in performance.

	Most games seem to be especially sensitive to the XY instructions,
	such as ADDXY and SUBXY.

*****************************************************************************/

/* clears flags */
#define CLR_V (V_FLAG = 0)

#define ZEXTEND(val,width) if (width) (val) &= ((UINT32)0xffffffff>>(32-(width)))
#define SEXTEND(val,width) if (width)														  \
						   {																  \
						       (val) &= ((UINT32)0xffffffff>>(32-(width))); 			 	  \
							   (val) |= (((val)&(1<<((width)-1)))?((0xffffffff)<<(width)):0); \
						   }

#define SET_Z(val) (NOTZ_FLAG = (val))
#define SET_N(val) (N_FLAG = SIGN(val))
#define SET_NZ(val) {SET_Z(val); SET_N(NOTZ_FLAG);}
#define SET_V_SUB(a,b,r) (V_FLAG = SIGN( ((a)^(b))&((a)^(r))))
#define SET_V_ADD(a,b,r) (V_FLAG = SIGN(~((a)^(b))&((a)^(r))))
#define SET_C_SUB(a,b)  (C_FLAG = (((UINT32)  (b)) >((UINT32)(a))))
#define SET_C_ADD(a,b)  (C_FLAG = (((UINT32)(~(a)))<((UINT32)(b))))
#define SET_NZV_SUB(a,b,r)   {SET_NZ(r);SET_V_SUB(a,b,r);}
#define SET_NZCV_SUB(a,b,r)  {SET_NZV_SUB(a,b,r);SET_C_SUB(a,b);}
#define SET_NZCV_ADD(a,b,r)  {SET_NZ(r);SET_V_ADD(a,b,r);SET_C_ADD(a,b);}

#define XYTOL(val) ((((INT32)((UINT16)(val.y) )<<state.xytolshiftcount1) |	\
				    (((INT32)((UINT16)(val.x)))<<state.xytolshiftcount2)) + OFFSET)


#define COUNT_CYCLES(x)	tms34010_ICount -= x
#define COUNT_UNKNOWN_CYCLES(x) COUNT_CYCLES(x)


static void unimpl(void)
{
	PUSH(PC);
	PUSH(GET_ST());
	RESET_ST();
	PC = RLONG(0xfffffc20);
  	COUNT_UNKNOWN_CYCLES(16);

	/* extra check to prevent bad things */
	if (PC == 0 || opcode_table[cpu_readop16(TOBYTE(PC)) >> 4] == unimpl)
	{
		cpu_set_halt_line(cpu_getactivecpu(),ASSERT_LINE);
#if MAME_DEBUG
		{
			extern int debug_key_pressed;
			debug_key_pressed = 1;
		}
#endif
	}
}

#define ADD_XY(R)								\
{												\
	XY res;										\
	XY  a =  R##REG_XY(R##SRCREG);				\
	XY *b = &R##REG_XY(R##DSTREG);				\
	res.x = b->x + a.x;							\
	   N_FLAG = !res.x;							\
	   V_FLAG = res.x & 0x8000;					\
	res.y = b->y + a.y;							\
	NOTZ_FLAG = res.y;							\
	   C_FLAG = res.y & 0x8000;					\
  	*b = res;									\
  	COUNT_CYCLES(1);							\
}
static void add_xy_a(void) { ADD_XY(A); }
static void add_xy_b(void) { ADD_XY(B); }

#define SUB_XY(R)								\
{												\
	XY  a =  R##REG_XY(R##SRCREG);				\
	XY *b = &R##REG_XY(R##DSTREG);				\
	   N_FLAG = (a.x == b->x);					\
	   V_FLAG = (a.x >  b->x);					\
	   C_FLAG = (a.y >  b->y);					\
	NOTZ_FLAG = (a.y != b->y);					\
	b->x -= a.x;								\
	b->y -= a.y;								\
  	COUNT_CYCLES(1);							\
}
static void sub_xy_a(void) { SUB_XY(A); }
static void sub_xy_b(void) { SUB_XY(B); }

#define CMP_XY(R)								\
{												\
	INT16 res;									\
	XY a = R##REG_XY(R##DSTREG);				\
	XY b = R##REG_XY(R##SRCREG);				\
	res = a.x-b.x;								\
	   N_FLAG = !res;							\
	   V_FLAG = (res & 0x8000);					\
	res = a.y-b.y;								\
	NOTZ_FLAG =  res;							\
	   C_FLAG = (res & 0x8000);					\
  	COUNT_CYCLES(1);							\
}
static void cmp_xy_a(void) { CMP_XY(A); }
static void cmp_xy_b(void) { CMP_XY(B); }

#define CPW(R)									\
{												\
	INT32 res = 0;								\
	INT16 x = R##REG_X(R##SRCREG);				\
	INT16 y = R##REG_Y(R##SRCREG);				\
												\
	res |= ((WSTART_X > x) ? 0x20  : 0);		\
	res |= ((x > WEND_X)   ? 0x40  : 0);		\
	res |= ((WSTART_Y > y) ? 0x80  : 0);		\
	res |= ((y > WEND_Y)   ? 0x100 : 0);		\
	R##REG(R##DSTREG) = V_FLAG = res;			\
  	COUNT_CYCLES(1);							\
}
static void cpw_a(void) { CPW(A); }
static void cpw_b(void) { CPW(B); }

#define CVXYL(R)									\
{													\
    R##REG(R##DSTREG) = XYTOL(R##REG_XY(R##SRCREG));\
  	COUNT_CYCLES(3);								\
}
static void cvxyl_a(void) { CVXYL(A); }
static void cvxyl_b(void) { CVXYL(B); }

#define MOVX(R)										\
{													\
	R##REG(R##DSTREG) = (R##REG(R##DSTREG) & 0xffff0000) | (UINT16)R##REG(R##SRCREG);	\
  	COUNT_CYCLES(1);																	\
}
static void movx_a(void) { MOVX(A); }
static void movx_b(void) { MOVX(B); }

#define MOVY(R)										\
{													\
	R##REG(R##DSTREG) = (R##REG(R##SRCREG) & 0xffff0000) | (UINT16)R##REG(R##DSTREG);	\
  	COUNT_CYCLES(1);																	\
}
static void movy_a(void) { MOVY(A); }
static void movy_b(void) { MOVY(B); }

#define PIXT_RI(R)			                        \
{							 						\
	WPIXEL(R##REG(R##DSTREG),R##REG(R##SRCREG));	\
	FINISH_PIX_OP;									\
  	COUNT_UNKNOWN_CYCLES(2);						\
}
static void pixt_ri_a(void) { PIXT_RI(A); }
static void pixt_ri_b(void) { PIXT_RI(B); }

#define PIXT_RIXY(R)		                        \
{													\
	if (state.window_checking != 0 && state.window_checking != 3 && errorlog)			\
	{												\
		fprintf(errorlog, "PIXT R,XY  %08X - Window Checking Mode %d not supported\n", PC, state.window_checking);	\
	}												\
													\
	if (state.window_checking != 3 ||						\
		(R##REG_X(R##DSTREG) >= WSTART_X && R##REG_X(R##DSTREG) <= WEND_X &&		\
		 R##REG_Y(R##DSTREG) >= WSTART_Y && R##REG_Y(R##DSTREG) <= WEND_Y))			\
		WPIXEL(XYTOL(R##REG_XY(R##DSTREG)),R##REG(R##SRCREG));	\
	FINISH_PIX_OP;									\
  	COUNT_UNKNOWN_CYCLES(4);						\
}
static void pixt_rixy_a(void) { PIXT_RIXY(A); }
static void pixt_rixy_b(void) { PIXT_RIXY(B); }

#define PIXT_IR(R)			                        \
{													\
	R##REG(R##DSTREG) = V_FLAG = RPIXEL(R##REG(R##SRCREG));	\
	FINISH_PIX_OP;									\
	COUNT_CYCLES(4);								\
}
static void pixt_ir_a(void) { PIXT_IR(A); }
static void pixt_ir_b(void) { PIXT_IR(B); }

#define PIXT_II(R)			                       	\
{													\
	WPIXEL(R##REG(R##DSTREG),RPIXEL(R##REG(R##SRCREG)));	\
	FINISH_PIX_OP;									\
  	COUNT_UNKNOWN_CYCLES(4);						\
}
static void pixt_ii_a(void) { PIXT_II(A); }
static void pixt_ii_b(void) { PIXT_II(B); }

#define PIXT_IXYR(R)			              		\
{													\
	R##REG(R##DSTREG) = V_FLAG = RPIXEL(XYTOL(R##REG_XY(R##SRCREG)));	\
	FINISH_PIX_OP;									\
	COUNT_CYCLES(6);								\
}
static void pixt_ixyr_a(void) { PIXT_IXYR(A); }
static void pixt_ixyr_b(void) { PIXT_IXYR(B); }

#define PIXT_IXYIXY(R)			              			      		\
{																	\
	WPIXEL(XYTOL(R##REG_XY(R##DSTREG)),RPIXEL(XYTOL(R##REG_XY(R##SRCREG))));	\
	FINISH_PIX_OP;													\
  	COUNT_UNKNOWN_CYCLES(7);										\
}
static void pixt_ixyixy_a(void) { PIXT_IXYIXY(A); }
static void pixt_ixyixy_b(void) { PIXT_IXYIXY(B); }

#define DRAV(R)			              			      		\
{															\
	if (state.window_checking && errorlog)					\
	{														\
		fprintf(errorlog, "DRAV  %08X - Window Checking Mode %d not supported\n", PC, state.window_checking);	\
	}														\
															\
	WPIXEL(XYTOL(R##REG_XY(R##DSTREG)),COLOR1);				\
															\
	R##REG_X(R##DSTREG) += R##REG_X(R##SRCREG);				\
	R##REG_Y(R##DSTREG) += R##REG_Y(R##SRCREG);				\
	FINISH_PIX_OP;											\
  	COUNT_UNKNOWN_CYCLES(4);								\
}
static void drav_a(void) { DRAV(A); }
static void drav_b(void) { DRAV(B); }


/* General Instructions */
#define ABS(R)			              			      		\
{															\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 r = 0 - *rd;										\
	SET_NZV_SUB(0,*rd,r);									\
	if (!N_FLAG)											\
	{														\
		*rd = r;											\
	}														\
	COUNT_CYCLES(1);										\
}
static void abs_a(void) { ABS(A); }
static void abs_b(void) { ABS(B); }

#define ADD(R)			              			      		\
{							 								\
	INT32 a = R##REG(R##SRCREG);							\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 b = *rd;											\
	INT32 r = *rd = a + b;									\
	SET_NZCV_ADD(a,b,r);									\
	COUNT_CYCLES(1);										\
}
static void add_a(void) { ADD(A); }
static void add_b(void) { ADD(B); }

#define ADDC(R)			              			      		\
{			  												\
	/* I'm not sure to which side the carry is added to, should	*/	\
	/* verify it against the examples */					\
	INT32 a = R##REG(R##SRCREG) + (C_FLAG?1:0);				\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 b = *rd;											\
	INT32 r = *rd = a + b;									\
	SET_NZCV_ADD(a,b,r);									\
	COUNT_CYCLES(1);										\
}
static void addc_a(void) { ADDC(A); }
static void addc_b(void) { ADDC(B); }

#define ADDI_W(R)			              			      	\
{			  												\
	INT32 a = PARAM_WORD();									\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 b = *rd;											\
	INT32 r = *rd = a + b;									\
	SET_NZCV_ADD(a,b,r);									\
	COUNT_CYCLES(2);										\
}
static void addi_w_a(void) { ADDI_W(A); }
static void addi_w_b(void) { ADDI_W(B); }

#define ADDI_L(R)			              			      	\
{			  												\
	INT32 a = PARAM_LONG();									\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 b = *rd;											\
	INT32 r = *rd = a + b;									\
	SET_NZCV_ADD(a,b,r);									\
	COUNT_CYCLES(3);										\
}
static void addi_l_a(void) { ADDI_L(A); }
static void addi_l_b(void) { ADDI_L(B); }

#define ADDK(R)				              			      	\
{			  												\
	INT32 r,b,*rd;											\
	INT32 a = PARAM_K; if (!a) a = 32;						\
	rd = &R##REG(R##DSTREG);								\
	b = *rd;												\
	r = *rd = a + b;										\
	SET_NZCV_ADD(a,b,r);									\
	COUNT_CYCLES(1);										\
}
static void addk_a(void) { ADDK(A); }
static void addk_b(void) { ADDK(B); }

#define AND(R)				              			      	\
{			  												\
	INT32 *rd = &R##REG(R##DSTREG);							\
	*rd &= R##REG(R##SRCREG);								\
	SET_Z(*rd);												\
	COUNT_CYCLES(1);										\
}
static void and_a(void) { AND(A); }
static void and_b(void) { AND(B); }

#define ANDI(R)				              			      	\
{			  												\
	INT32 *rd = &R##REG(R##DSTREG);							\
	*rd &= ~PARAM_LONG();									\
	SET_Z(*rd);												\
	COUNT_CYCLES(3);										\
}
static void andi_a(void) { ANDI(A); }
static void andi_b(void) { ANDI(B); }

#define ANDN(R)				              			      	\
{			  												\
	INT32 *rd = &R##REG(R##DSTREG);							\
	*rd &= ~R##REG(R##SRCREG);								\
	SET_Z(*rd);												\
	COUNT_CYCLES(1);										\
}
static void andn_a(void) { ANDN(A); }
static void andn_b(void) { ANDN(B); }

#define BTST_K(R)				              			    \
{							 								\
	SET_Z(R##REG(R##DSTREG) & (1<<(31-PARAM_K)));			\
	COUNT_CYCLES(1);										\
}
static void btst_k_a(void) { BTST_K(A); }
static void btst_k_b(void) { BTST_K(B); }

#define BTST_R(R)				              			    \
{															\
	SET_Z(R##REG(R##DSTREG) & (1<<(R##REG(R##SRCREG)&0x1f)));	\
	COUNT_CYCLES(2);										\
}
static void btst_r_a(void) { BTST_R(A); }
static void btst_r_b(void) { BTST_R(B); }

static void clrc(void)
{
	C_FLAG = 0;
	COUNT_CYCLES(1);
}

#define CMP(R)				       		       			    \
{															\
	INT32 *rs = &R##REG(R##SRCREG);							\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 r = *rd - *rs;									\
	SET_NZCV_SUB(*rd,*rs,r);								\
	COUNT_CYCLES(1);										\
}
static void cmp_a(void) { CMP(A); }
static void cmp_b(void) { CMP(B); }

#define CMPI_W(R)			       		       			    \
{															\
	INT32 r;												\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 t = ~PARAM_WORD();								\
	r = *rd - t;											\
	SET_NZCV_SUB(*rd,t,r);									\
	COUNT_CYCLES(2);										\
}
static void cmpi_w_a(void) { CMPI_W(A); }
static void cmpi_w_b(void) { CMPI_W(B); }

#define CMPI_L(R)			       		       			    \
{															\
	INT32 *rd = &R##REG(R##DSTREG);							\
	INT32 t = ~PARAM_LONG();								\
	INT32 r = *rd - t;										\
	SET_NZCV_SUB(*rd,t,r);									\
	COUNT_CYCLES(3);										\
}
static void cmpi_l_a(void) { CMPI_L(A); }
static void cmpi_l_b(void) { CMPI_L(B); }

static void dint(void)
{
	IE_FLAG = 0;
	COUNT_CYCLES(3);
}

#define DIVS(R,N)			       		       			    \
{															\
	INT32 *rs  = &R##REG(R##SRCREG);						\
	INT32 *rd1 = &R##REG(R##DSTREG);						\
	V_FLAG = N_FLAG = 0;									\
	NOTZ_FLAG = 1;											\
	if (!(R##DSTREG & (1*N)))								\
	{														\
		if (!*rs)											\
		{													\
			V_FLAG = 0;										\
		}													\
		else												\
		{													\
			INT32 *rd2 = &R##REG(R##DSTREG+N);				\
			INT64 dividend  = COMBINE_64_32_32(*rd1, *rd2); \
			INT64 quotient  = DIV_64_64_32(dividend, *rs); 	\
			INT32 remainder = MOD_32_64_32(dividend, *rs); 	\
			UINT32 signbits = ((quotient & 0x80000000) ? 0xffffffff : 0); 	\
			if (HI32_32_64(quotient) != signbits)			\
			{												\
				V_FLAG = 0;									\
			}												\
			else											\
			{												\
				*rd1 = quotient;							\
				*rd2 = remainder;							\
				SET_NZ(*rd1);								\
			}												\
		}													\
		COUNT_CYCLES(40);									\
	}														\
	else													\
	{														\
		if (!*rs)											\
		{													\
			V_FLAG = 0;										\
		}													\
		else												\
		{													\
			*rd1 /= *rs;									\
			SET_NZ(*rd1);									\
		}													\
		COUNT_CYCLES(39);									\
	}														\
}
static void divs_a(void) { DIVS(A,1   ); }
static void divs_b(void) { DIVS(B,0x10); }

#define DIVU(R,N)			       		       			    \
{										  					\
	INT32 *rs  = &R##REG(R##SRCREG);						\
	INT32 *rd1 = &R##REG(R##DSTREG);						\
	V_FLAG = 0;												\
	NOTZ_FLAG = 1;											\
	if (!(R##DSTREG & (1*N)))								\
	{														\
		if (!*rs)											\
		{													\
			V_FLAG = 0;										\
		}													\
		else												\
		{													\
			INT32 *rd2 = &R##REG(R##DSTREG+N);				\
			UINT64 dividend  = COMBINE_U64_U32_U32(*rd1, *rd2);	\
			UINT64 quotient  = DIV_U64_U64_U32(dividend, *rs);	\
			UINT32 remainder = MOD_U32_U64_U32(dividend, *rs); 	\
			if (HI32_U32_U64(quotient) != 0)				\
			{												\
				V_FLAG = 0;									\
			}												\
			else											\
			{												\
				*rd1 = quotient;							\
				*rd2 = remainder;							\
				SET_Z(*rd1);								\
			}												\
		}													\
	}														\
	else													\
	{														\
		if (!*rs)											\
		{													\
			V_FLAG = 0;										\
		}													\
		else												\
		{													\
			*rd1 = (UINT32)*rd1 / (UINT32)*rs;			  	\
			SET_Z(*rd1);									\
		}													\
	}														\
	COUNT_CYCLES(37);										\
}
static void divu_a(void) { DIVU(A,1   ); }
static void divu_b(void) { DIVU(B,0x10); }

static void eint(void)
{
	IE_FLAG = 1;
	check_interrupt();
	COUNT_CYCLES(3);
}

#define EXGF(F,R)			       		       			    	\
{																\
	INT32 *rd = &R##REG(R##DSTREG);								\
	UINT32 temp = (FE##F##_FLAG ? 0x20 : 0) | FW(F);			\
	FE##F##_FLAG = (*rd&0x20);									\
	FW(F) = (*rd&0x1f);											\
	SET_FW();													\
	*rd = temp;													\
	COUNT_CYCLES(1);											\
}
static void exgf0_a(void) { EXGF(0,A); }
static void exgf0_b(void) { EXGF(0,B); }
static void exgf1_a(void) { EXGF(1,A); }
static void exgf1_b(void) { EXGF(1,B); }

#define LMO(R)			       		       			    		\
{																\
	UINT32 res = 0;												\
	UINT32 rs  = R##REG(R##SRCREG);								\
	 INT32 *rd = &R##REG(R##DSTREG);							\
	SET_Z(rs);													\
	if (rs)														\
	{															\
		while (!(rs & 0x80000000))								\
		{														\
			res++;												\
			rs <<= 1;											\
		}														\
	}															\
	*rd = res;													\
	COUNT_CYCLES(1);											\
}
static void lmo_a(void) { LMO(A); }
static void lmo_b(void) { LMO(B); }

#if TMS34010_FAST_STACK
#define MMFM(R,N)			       		       			    	\
{																\
	INT32 i;													\
	UINT16 l = (UINT16) PARAM_WORD();							\
	COUNT_CYCLES(3);											\
	if (R##DSTREG == 15*N)										\
	{															\
		UINT32 bitaddr1 = SP;									\
		UINT8 *bitaddr2 = STACKPTR(bitaddr1);					\
		for (i = 15; i >= 0 ; i--)								\
		{														\
			if (l & 0x8000)										\
			{													\
				R##REG(i*N) = READ_WORD(bitaddr2) + (READ_WORD(bitaddr2+2) << 16); \
				bitaddr1 += 0x20;								\
				bitaddr2 += 4;									\
				COUNT_CYCLES(4);								\
			}													\
			l <<= 1;											\
		}														\
		SP = bitaddr1;											\
	}															\
	else														\
	{															\
		INT32 rd = R##DSTREG;									\
		for (i = 15; i >= 0 ; i--)								\
		{														\
			if (l & 0x8000)										\
			{													\
				R##REG(i*N) = RLONG(R##REG(rd));				\
				R##REG(rd) += 0x20;								\
				COUNT_CYCLES(4);								\
			}													\
			l <<= 1;											\
		}														\
	}															\
}
#else
#define MMFM(R,N)			       		       			    	\
{																\
	INT32 i;													\
	UINT16 l = (UINT16) PARAM_WORD();							\
	COUNT_CYCLES(3);											\
	{															\
		INT32 rd = R##DSTREG;									\
		for (i = 15; i >= 0 ; i--)								\
		{														\
			if (l & 0x8000)										\
			{													\
				R##REG(i*N) = RLONG(R##REG(rd));				\
				R##REG(rd) += 0x20;								\
				COUNT_CYCLES(4);								\
			}													\
			l <<= 1;											\
		}														\
	}															\
}
#endif
static void mmfm_a(void) { MMFM(A,1   ); }
static void mmfm_b(void) { MMFM(B,0x10); }

#if TMS34010_FAST_STACK
#define MMTM(R,N)			       		       			    	\
{			  													\
	UINT32 i;													\
	UINT16 l = (UINT16) PARAM_WORD();							\
	COUNT_CYCLES(2);											\
	if (R##DSTREG == 15*N)										\
	{															\
		UINT32 bitaddr1 = SP;									\
		UINT8 *bitaddr2 = STACKPTR(bitaddr1);					\
		SET_N(bitaddr1^0x80000000);								\
		for (i = 0; i  < 16; i++)								\
		{														\
			if (l & 0x8000)										\
			{													\
				UINT32 reg = R##REG(i*N);						\
				bitaddr1 -= 0x20;								\
				bitaddr2 -= 4;						 			\
				WRITE_WORD(bitaddr2  , (UINT16)reg);			\
				WRITE_WORD(bitaddr2+2, reg >> 16);				\
				COUNT_CYCLES(4);								\
			}													\
			l <<= 1;											\
		}														\
		SP = bitaddr1;											\
	}															\
	else														\
	{															\
		INT32 rd = R##DSTREG;									\
		SET_N(R##REG(rd)^0x80000000);							\
		for (i = 0; i  < 16; i++)								\
		{														\
			if (l & 0x8000)										\
			{													\
				R##REG(rd) -= 0x20;								\
				WLONG(R##REG(rd),R##REG(i*N));					\
				COUNT_CYCLES(4);								\
			}													\
			l <<= 1;											\
		}														\
	}															\
}
#else
#define MMTM(R,N)			       		       			    	\
{			  													\
	UINT32 i;													\
	UINT16 l = (UINT16) PARAM_WORD();							\
	COUNT_CYCLES(2);											\
	{															\
		INT32 rd = R##DSTREG;									\
		SET_N(R##REG(rd)^0x80000000);							\
		for (i = 0; i  < 16; i++)								\
		{														\
			if (l & 0x8000)										\
			{													\
				R##REG(rd) -= 0x20;								\
				WLONG(R##REG(rd),R##REG(i*N));					\
				COUNT_CYCLES(4);								\
			}													\
			l <<= 1;											\
		}														\
	}															\
}
#endif
static void mmtm_a(void) { MMTM(A,1   ); }
static void mmtm_b(void) { MMTM(B,0x10); }

#define MODS(R)			       		       			    		\
{				  												\
	INT32 *rs = &R##REG(R##SRCREG);								\
	INT32 *rd = &R##REG(R##DSTREG);								\
	V_FLAG = (*rs == 0);										\
	if (!V_FLAG)												\
	{															\
		*rd %= *rs;												\
		SET_Z(*rd);												\
	}															\
	COUNT_CYCLES(40);											\
}
static void mods_a(void) { MODS(A); }
static void mods_b(void) { MODS(B); }

#define MODU(R)			       		       			    		\
{				  												\
	INT32 *rs = &R##REG(R##SRCREG);								\
	INT32 *rd = &R##REG(R##DSTREG);								\
	V_FLAG = (*rs == 0);										\
	if (!V_FLAG)												\
	{															\
		*rd = (UINT32)*rd % (UINT32)*rs;						\
		SET_Z(*rd);												\
	}															\
	COUNT_CYCLES(35);											\
}
static void modu_a(void) { MODU(A); }
static void modu_b(void) { MODU(B); }

#define MPYS(R,N)			       		       			    	\
{																\
	INT32 *rd1 = &R##REG(R##DSTREG);							\
																\
	INT32 m1 = R##REG(R##SRCREG);								\
	SEXTEND(m1, FW(1));											\
																\
	if (!(R##DSTREG & (1*N)))									\
	{															\
		INT64 product    = MUL_64_32_32(m1, *rd1);				\
		*rd1             = HI32_32_64(product);					\
		R##REG(R##DSTREG+N) = LO32_32_64(product);				\
		SET_Z(product!=0);										\
		SET_N(*rd1);											\
	}															\
	else														\
	{															\
		*rd1 *= m1;												\
		SET_NZ(*rd1);											\
	}															\
	COUNT_CYCLES(20);											\
}
static void mpys_a(void) { MPYS(A,1   ); }
static void mpys_b(void) { MPYS(B,0x10); }

#define MPYU(R,N)			       		       			    	\
{				  												\
	INT32 *rd1 = &R##REG(R##DSTREG);							\
																\
	UINT32 m1 = R##REG(R##SRCREG);								\
	ZEXTEND(m1, FW(1));											\
																\
	if (!(R##DSTREG & (1*N)))									\
	{															\
		UINT64 product   = MUL_U64_U32_U32(m1, *rd1);			\
		*rd1             = HI32_U32_U64(product);				\
		R##REG(R##DSTREG+N) = LO32_U32_U64(product);			\
		SET_Z(product!=0);										\
	}															\
	else														\
	{															\
		*rd1 = (UINT32)*rd1 * m1;								\
		SET_Z(*rd1);											\
	}															\
	COUNT_CYCLES(21);											\
}
static void mpyu_a(void) { MPYU(A,1   ); }
static void mpyu_b(void) { MPYU(B,0x10); }

#define NEG(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 r = 0 - *rd;											\
	SET_NZCV_SUB(0,*rd,r);										\
	*rd = r;													\
	COUNT_CYCLES(1);											\
}
static void neg_a(void) { NEG(A); }
static void neg_b(void) { NEG(B); }

#define NEGB(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 t = *rd + (C_FLAG?1:0);								\
	INT32 r = 0 - t;											\
	SET_NZCV_SUB(0,t,r);										\
	*rd = r;													\
	COUNT_CYCLES(1);											\
}
static void negb_a(void) { NEGB(A); }
static void negb_b(void) { NEGB(B); }

static void nop(void)
{
	COUNT_CYCLES(1);
}

#define NOT(R)			       		       			    		\
{								 								\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd = ~(*rd);												\
	SET_Z(*rd);													\
	COUNT_CYCLES(1);											\
}
static void not_a(void) { NOT(A); }
static void not_b(void) { NOT(B); }

#define OR(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd |= R##REG(R##SRCREG);									\
	SET_Z(*rd);													\
	COUNT_CYCLES(1);											\
}
static void or_a(void) { OR(A); }
static void or_b(void) { OR(B); }

#define ORI(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd |= PARAM_LONG();										\
	SET_Z(*rd);													\
	COUNT_CYCLES(3);											\
}
static void ori_a(void) { ORI(A); }
static void ori_b(void) { ORI(B); }

static void setc(void)
{
	C_FLAG = 1;
	COUNT_CYCLES(1);
}

#define SETF(F)													\
{																\
	FE##F##_FLAG = state.op & 0x20;								\
	FW(F) = state.op & 0x1f;									\
	SET_FW();													\
	COUNT_CYCLES(1+F);											\
}
static void setf0(void) { SETF(0); }
static void setf1(void) { SETF(1); }

#define SEXT(F,R)												\
{							   									\
	INT32 *rd = &R##REG(R##DSTREG);								\
	SEXTEND(*rd,FW(F));											\
	SET_NZ(*rd);												\
	COUNT_CYCLES(3);											\
}
static void sext0_a(void) { SEXT(0,A); }
static void sext0_b(void) { SEXT(0,B); }
static void sext1_a(void) { SEXT(1,A); }
static void sext1_b(void) { SEXT(1,B); }

#define RL(R,K)			       		       			    		\
{			 													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 res = *rd;											\
	INT32 k = (K);												\
	if (k)														\
	{															\
		res<<=(k-1);											\
		C_FLAG = SIGN(res);										\
		res<<=1;												\
		res |= (((UINT32)*rd)>>((-k)&0x1f));					\
		*rd = res;												\
	}															\
	else														\
	{															\
		C_FLAG = 0;												\
	}															\
	SET_Z(res);													\
	COUNT_CYCLES(1);											\
}
static void rl_k_a(void) { RL(A,PARAM_K); }
static void rl_k_b(void) { RL(B,PARAM_K); }
static void rl_r_a(void) { RL(A,AREG(ASRCREG)&0x1f); }
static void rl_r_b(void) { RL(B,BREG(BSRCREG)&0x1f); }

#define SLA(R,K)												\
{				 												\
	 INT32 *rd = &R##REG(R##DSTREG);							\
	UINT32 res = *rd;											\
	 INT32 k = K;												\
	if (k)														\
	{															\
		UINT32 mask = (0xffffffff<<(31-k))&0x7fffffff;			\
		UINT32 res2 = SIGN(res) ? res^mask : res;				\
		V_FLAG = (res2 & mask);									\
																\
		res<<=(k-1);											\
		C_FLAG = SIGN(res);										\
		res<<=1;												\
		*rd = res;												\
	}															\
	else														\
	{															\
		C_FLAG = V_FLAG = 0;									\
	}															\
	SET_NZ(res);												\
	COUNT_CYCLES(3);											\
}
static void sla_k_a(void) { SLA(A,PARAM_K); }
static void sla_k_b(void) { SLA(B,PARAM_K); }
static void sla_r_a(void) { SLA(A,AREG(ASRCREG)&0x1f); }
static void sla_r_b(void) { SLA(B,BREG(BSRCREG)&0x1f); }

#define SLL(R,K)												\
{			 													\
	 INT32 *rd = &R##REG(R##DSTREG);							\
	UINT32 res = *rd;											\
	 INT32 k = K;												\
	if (k)														\
	{															\
		res<<=(k-1);											\
		C_FLAG = SIGN(res);										\
		res<<=1;												\
		*rd = res;												\
	}															\
	else														\
	{															\
		C_FLAG = 0;												\
	}															\
	SET_Z(res);													\
	COUNT_CYCLES(1);											\
}
static void sll_k_a(void) { SLL(A,PARAM_K); }
static void sll_k_b(void) { SLL(B,PARAM_K); }
static void sll_r_a(void) { SLL(A,AREG(ASRCREG)&0x1f); }
static void sll_r_b(void) { SLL(B,BREG(BSRCREG)&0x1f); }

#define SRA(R,K)												\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 res = *rd;											\
	INT32 k = (-(K)) & 0x1f;									\
	if (k)														\
	{															\
		res>>=(k-1);											\
		C_FLAG = res & 1;										\
		res>>=1;												\
		*rd = res;												\
	}															\
	else														\
	{															\
		C_FLAG = 0;												\
	}															\
	SET_NZ(res);												\
	COUNT_CYCLES(1);											\
}
static void sra_k_a(void) { SRA(A,PARAM_K); }
static void sra_k_b(void) { SRA(B,PARAM_K); }
static void sra_r_a(void) { SRA(A,AREG(ASRCREG)); }
static void sra_r_b(void) { SRA(B,BREG(BSRCREG)); }

#define SRL(R,K)												\
{			  													\
	 INT32 *rd = &R##REG(R##DSTREG);							\
	UINT32 res = *rd;											\
	 INT32 k = (-(K)) & 0x1f;									\
	if (k)														\
	{															\
		res>>=(k-1);											\
		C_FLAG = res & 1;										\
		res>>=1;												\
		*rd = res;												\
	}															\
	else														\
	{															\
		C_FLAG = 0;												\
	}															\
	SET_NZ(res);												\
	COUNT_CYCLES(1);											\
}
static void srl_k_a(void) { SRL(A,PARAM_K); }
static void srl_k_b(void) { SRL(B,PARAM_K); }
static void srl_r_a(void) { SRL(A,AREG(ASRCREG)); }
static void srl_r_b(void) { SRL(B,BREG(BSRCREG)); }

#define SUB(R)			       		       			    		\
{			  													\
	INT32 *rs = &R##REG(R##SRCREG);								\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 r = *rd - *rs;										\
	SET_NZCV_SUB(*rd,*rs,r);									\
	*rd = r;													\
	COUNT_CYCLES(1);											\
}
static void sub_a(void) { SUB(A); }
static void sub_b(void) { SUB(B); }

#define SUBB(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 t = R##REG(R##SRCREG) + (C_FLAG?1:0);					\
	INT32 r = *rd - t;											\
	SET_NZCV_SUB(*rd,t,r);										\
	*rd = r;													\
	COUNT_CYCLES(1);											\
}
static void subb_a(void) { SUBB(A); }
static void subb_b(void) { SUBB(B); }

#define SUBI_W(R)			       		       			    	\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 r;													\
	INT32 t = ~PARAM_WORD();									\
	r = *rd - t;												\
	SET_NZCV_SUB(*rd,t,r);										\
	*rd = r;													\
	COUNT_CYCLES(2);											\
}
static void subi_w_a(void) { SUBI_W(A); }
static void subi_w_b(void) { SUBI_W(B); }

#define SUBI_L(R)			       		       			    	\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 t = ~PARAM_LONG();									\
	INT32 r = *rd - t;											\
	SET_NZCV_SUB(*rd,t,r);										\
	*rd = r;													\
	COUNT_CYCLES(3);											\
}
static void subi_l_a(void) { SUBI_L(A); }
static void subi_l_b(void) { SUBI_L(B); }

#define SUBK(R)			       		       			    		\
{			  													\
	INT32 r;													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 t = PARAM_K; if (!t) t = 32;							\
	r = *rd - t;												\
	SET_NZCV_SUB(*rd,t,r);										\
	*rd = r;													\
	COUNT_CYCLES(1);											\
}
static void subk_a(void) { SUBK(A); }
static void subk_b(void) { SUBK(B); }

#define XOR(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd ^= R##REG(R##SRCREG);									\
	SET_Z(*rd);													\
	COUNT_CYCLES(1);											\
}
static void xor_a(void) { XOR(A); }
static void xor_b(void) { XOR(B); }

#define XORI(R)			       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd ^= PARAM_LONG();										\
	SET_Z(*rd);													\
	COUNT_CYCLES(3);											\
}
static void xori_a(void) { XORI(A); }
static void xori_b(void) { XORI(B); }

#define ZEXT(F,R)												\
{																\
	INT32 *rd = &R##REG(R##DSTREG);								\
	ZEXTEND(*rd,FW(F));											\
	SET_Z(*rd);													\
	COUNT_CYCLES(1);											\
}
static void zext0_a(void) { ZEXT(0,A); }
static void zext0_b(void) { ZEXT(0,B); }
static void zext1_a(void) { ZEXT(1,A); }
static void zext1_b(void) { ZEXT(1,B); }


/* Move Instructions */
#define MOVI_W(R)		       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd=PARAM_WORD();											\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(2);											\
}
static void movi_w_a(void) { MOVI_W(A); }
static void movi_w_b(void) { MOVI_W(B); }

#define MOVI_L(R)		       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd=PARAM_LONG();											\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(3);											\
}
static void movi_l_a(void) { MOVI_L(A); }
static void movi_l_b(void) { MOVI_L(B); }

#define MOVK(R)		       		       			    			\
{																\
	INT32 k = PARAM_K; if (!k) k = 32;							\
	R##REG(R##DSTREG) = k;										\
	COUNT_CYCLES(1);											\
}
static void movk_a(void) { MOVK(A); }
static void movk_b(void) { MOVK(B); }

#define MOVB_RN(R)		       		       			    		\
{																\
	WBYTE(R##REG(R##DSTREG),R##REG(R##SRCREG));					\
	COUNT_CYCLES(1);											\
}
static void movb_rn_a(void) { MOVB_RN(A); }
static void movb_rn_b(void) { MOVB_RN(B); }

#define MOVB_NR(R)		       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd = RBYTE(R##REG(R##SRCREG));								\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(3);											\
}
static void movb_nr_a(void) { MOVB_NR(A); }
static void movb_nr_b(void) { MOVB_NR(B); }

#define MOVB_NN(R)												\
{																\
	WBYTE(R##REG(R##DSTREG),(UINT32)(UINT8)RBYTE(R##REG(R##SRCREG)));	\
	COUNT_CYCLES(3);											\
}
static void movb_nn_a(void) { MOVB_NN(A); }
static void movb_nn_b(void) { MOVB_NN(B); }

#define MOVB_R_NO(R)	       		       			    		\
{							  									\
	INT32 o = PARAM_WORD();										\
	WBYTE(R##REG(R##DSTREG)+o,R##REG(R##SRCREG));				\
	COUNT_CYCLES(3);											\
}
static void movb_r_no_a(void) { MOVB_R_NO(A); }
static void movb_r_no_b(void) { MOVB_R_NO(B); }

#define MOVB_NO_R(R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 o = PARAM_WORD();										\
	*rd = RBYTE(R##REG(R##SRCREG)+o);							\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(5);											\
}
static void movb_no_r_a(void) { MOVB_NO_R(A); }
static void movb_no_r_b(void) { MOVB_NO_R(B); }

#define MOVB_NO_NO(R)	       		       			    		\
{																\
	INT32 o1 = PARAM_WORD();									\
	INT32 o2 = PARAM_WORD();									\
	WBYTE(R##REG(R##DSTREG)+o2,(UINT32)(UINT8)RBYTE(R##REG(R##SRCREG)+o1));	\
	COUNT_CYCLES(5);											\
}
static void movb_no_no_a(void) { MOVB_NO_NO(A); }
static void movb_no_no_b(void) { MOVB_NO_NO(B); }

#define MOVB_RA(R)	       		       			    			\
{																\
	WBYTE(PARAM_LONG(),R##REG(R##DSTREG));						\
	COUNT_CYCLES(1);											\
}
static void movb_ra_a(void) { MOVB_RA(A); }
static void movb_ra_b(void) { MOVB_RA(B); }

#define MOVB_AR(R)	       		       			    			\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd = RBYTE(PARAM_LONG());									\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(5);											\
}
static void movb_ar_a(void) { MOVB_AR(A); }
static void movb_ar_b(void) { MOVB_AR(B); }

static void movb_aa(void)
{
	UINT32 bitaddrs=PARAM_LONG();
	WBYTE(PARAM_LONG(),(UINT32)(UINT8)RBYTE(bitaddrs));
	COUNT_CYCLES(6);
}

#define MOVE_RR(RS,RD)	       		       			    		\
{																\
	INT32 *rd = &RD##REG(RD##DSTREG);							\
	*rd = RS##REG(RS##SRCREG);									\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(1);											\
}
static void move_rr_a (void) { MOVE_RR(A,A); }
static void move_rr_b (void) { MOVE_RR(B,B); }
static void move_rr_ax(void) { MOVE_RR(A,B); }
static void move_rr_bx(void) { MOVE_RR(B,A); }

#define MOVE_RN(F,R)	       		       			    		\
{																\
	WFIELD##F(R##REG(R##DSTREG),R##REG(R##SRCREG));				\
	COUNT_CYCLES(1);											\
}
static void move0_rn_a (void) { MOVE_RN(0,A); }
static void move0_rn_b (void) { MOVE_RN(0,B); }
static void move1_rn_a (void) { MOVE_RN(1,A); }
static void move1_rn_b (void) { MOVE_RN(1,B); }

#define MOVE_R_DN(F,R)	       		       			    		\
{																\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd-=FW_INC(F);												\
	WFIELD##F(*rd,R##REG(R##SRCREG));							\
	COUNT_CYCLES(2);											\
}
static void move0_r_dn_a (void) { MOVE_R_DN(0,A); }
static void move0_r_dn_b (void) { MOVE_R_DN(0,B); }
static void move1_r_dn_a (void) { MOVE_R_DN(1,A); }
static void move1_r_dn_b (void) { MOVE_R_DN(1,B); }

#define MOVE_R_NI(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
    WFIELD##F(*rd,R##REG(R##SRCREG));							\
    *rd+=FW_INC(F);												\
	COUNT_CYCLES(1);											\
}
static void move0_r_ni_a (void) { MOVE_R_NI(0,A); }
static void move0_r_ni_b (void) { MOVE_R_NI(0,B); }
static void move1_r_ni_a (void) { MOVE_R_NI(1,A); }
static void move1_r_ni_b (void) { MOVE_R_NI(1,B); }

#define MOVE_NR(F,R)	       		       			    		\
{																\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd = RFIELD##F(R##REG(R##SRCREG));							\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(3);											\
}
static void move0_nr_a (void) { MOVE_NR(0,A); }
static void move0_nr_b (void) { MOVE_NR(0,B); }
static void move1_nr_a (void) { MOVE_NR(1,A); }
static void move1_nr_b (void) { MOVE_NR(1,B); }

#define MOVE_DN_R(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 *rs = &R##REG(R##SRCREG);								\
	*rs-=FW_INC(F);												\
	*rd = RFIELD##F(*rs);										\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(4);											\
}
static void move0_dn_r_a (void) { MOVE_DN_R(0,A); }
static void move0_dn_r_b (void) { MOVE_DN_R(0,B); }
static void move1_dn_r_a (void) { MOVE_DN_R(1,A); }
static void move1_dn_r_b (void) { MOVE_DN_R(1,B); }

#define MOVE_NI_R(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 *rs = &R##REG(R##SRCREG);								\
	INT32 data = RFIELD##F(*rs);								\
	*rs+=FW_INC(F);												\
	*rd = data;													\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(3);											\
}
static void move0_ni_r_a (void) { MOVE_NI_R(0,A); }
static void move0_ni_r_b (void) { MOVE_NI_R(0,B); }
static void move1_ni_r_a (void) { MOVE_NI_R(1,A); }
static void move1_ni_r_b (void) { MOVE_NI_R(1,B); }

#define MOVE_NN(F,R)	       		       			    		\
{										  						\
	WFIELD##F(R##REG(R##DSTREG),RFIELD##F(R##REG(R##SRCREG)));	\
	COUNT_CYCLES(3);											\
}
static void move0_nn_a (void) { MOVE_NN(0,A); }
static void move0_nn_b (void) { MOVE_NN(0,B); }
static void move1_nn_a (void) { MOVE_NN(1,A); }
static void move1_nn_b (void) { MOVE_NN(1,B); }

#define MOVE_DN_DN(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 *rs = &R##REG(R##SRCREG);								\
	INT32 data;													\
	*rs-=FW_INC(F);												\
	data = RFIELD##F(*rs);										\
	*rd-=FW_INC(F);												\
	WFIELD##F(*rd,data);										\
	COUNT_CYCLES(4);											\
}
static void move0_dn_dn_a (void) { MOVE_DN_DN(0,A); }
static void move0_dn_dn_b (void) { MOVE_DN_DN(0,B); }
static void move1_dn_dn_a (void) { MOVE_DN_DN(1,A); }
static void move1_dn_dn_b (void) { MOVE_DN_DN(1,B); }

#define MOVE_NI_NI(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 *rs = &R##REG(R##SRCREG);								\
	INT32 data = RFIELD##F(*rs);								\
	*rs+=FW_INC(F);												\
	WFIELD##F(*rd,data);										\
	*rd+=FW_INC(F);												\
	COUNT_CYCLES(4);											\
}
static void move0_ni_ni_a (void) { MOVE_NI_NI(0,A); }
static void move0_ni_ni_b (void) { MOVE_NI_NI(0,B); }
static void move1_ni_ni_a (void) { MOVE_NI_NI(1,A); }
static void move1_ni_ni_b (void) { MOVE_NI_NI(1,B); }

#define MOVE_R_NO(F,R)	       		       			    		\
{								  								\
	INT32 o = PARAM_WORD();										\
	WFIELD##F(R##REG(R##DSTREG)+o,R##REG(R##SRCREG));			\
	COUNT_CYCLES(3);											\
}
static void move0_r_no_a (void) { MOVE_R_NO(0,A); }
static void move0_r_no_b (void) { MOVE_R_NO(0,B); }
static void move1_r_no_a (void) { MOVE_R_NO(1,A); }
static void move1_r_no_b (void) { MOVE_R_NO(1,B); }

#define MOVE_NO_R(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 o = PARAM_WORD();										\
	*rd = RFIELD##F(R##REG(R##SRCREG)+o);						\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(5);											\
}
static void move0_no_r_a (void) { MOVE_NO_R(0,A); }
static void move0_no_r_b (void) { MOVE_NO_R(0,B); }
static void move1_no_r_a (void) { MOVE_NO_R(1,A); }
static void move1_no_r_b (void) { MOVE_NO_R(1,B); }

#define MOVE_NO_NI(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 o = PARAM_WORD();										\
	INT32 data = RFIELD##F(R##REG(R##SRCREG)+o);				\
	WFIELD##F(*rd,data);										\
	*rd+=FW_INC(F);												\
	COUNT_CYCLES(5);											\
}
static void move0_no_ni_a (void) { MOVE_NO_NI(0,A); }
static void move0_no_ni_b (void) { MOVE_NO_NI(0,B); }
static void move1_no_ni_a (void) { MOVE_NO_NI(1,A); }
static void move1_no_ni_b (void) { MOVE_NO_NI(1,B); }

#define MOVE_NO_NO(F,R)	       		       			    		\
{				 												\
	INT32 o1 = PARAM_WORD();									\
	INT32 o2 = PARAM_WORD();									\
	INT32 data = RFIELD##F(R##REG(R##SRCREG)+o1);				\
	WFIELD##F(R##REG(R##DSTREG)+o2,data);						\
	COUNT_CYCLES(5);											\
}
static void move0_no_no_a (void) { MOVE_NO_NO(0,A); }
static void move0_no_no_b (void) { MOVE_NO_NO(0,B); }
static void move1_no_no_a (void) { MOVE_NO_NO(1,A); }
static void move1_no_no_b (void) { MOVE_NO_NO(1,B); }

#define MOVE_RA(F,R)	       		       			    		\
{							  									\
	WFIELD##F(PARAM_LONG(),R##REG(R##DSTREG));					\
	COUNT_CYCLES(3);											\
}
static void move0_ra_a (void) { MOVE_RA(0,A); }
static void move0_ra_b (void) { MOVE_RA(0,B); }
static void move1_ra_a (void) { MOVE_RA(1,A); }
static void move1_ra_b (void) { MOVE_RA(1,B); }

#define MOVE_AR(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	*rd = RFIELD##F(PARAM_LONG());								\
	SET_NZ(*rd);												\
	CLR_V;														\
	COUNT_CYCLES(5);											\
}
static void move0_ar_a (void) { MOVE_AR(0,A); }
static void move0_ar_b (void) { MOVE_AR(0,B); }
static void move1_ar_a (void) { MOVE_AR(1,A); }
static void move1_ar_b (void) { MOVE_AR(1,B); }

#define MOVE_A_NI(F,R)	       		       			    		\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
    WFIELD##F(*rd,RFIELD##F(PARAM_LONG()));						\
    *rd+=FW_INC(F);												\
	COUNT_CYCLES(5);											\
}
static void move0_a_ni_a (void) { MOVE_A_NI(0,A); }
static void move0_a_ni_b (void) { MOVE_A_NI(0,B); }
static void move1_a_ni_a (void) { MOVE_A_NI(1,A); }
static void move1_a_ni_b (void) { MOVE_A_NI(1,B); }

#define MOVE_AA(F)		       		       			    		\
{																\
	UINT32 bitaddrs=PARAM_LONG();								\
	WFIELD##F(PARAM_LONG(),RFIELD##F(bitaddrs));				\
	COUNT_CYCLES(7);											\
}
static void move0_aa (void) { MOVE_AA(0); }
static void move1_aa (void) { MOVE_AA(1); }


/* Program Control and Context Switching */
#define CALL(R)													\
{																\
	PUSH(PC);													\
	PC = R##REG(R##DSTREG);										\
	COUNT_CYCLES(3);											\
}
static void call_a (void) { CALL(A); }
static void call_b (void) { CALL(B); }

static void callr(void)
{
	PUSH(PC+0x10);
	PC += (PARAM_WORD_NO_INC()<<4)+0x10;
	COUNT_CYCLES(3);
}

static void calla(void)
{
	PUSH(PC+0x20);
	PC = PARAM_LONG_NO_INC();
	COUNT_CYCLES(4);
}

#define DSJ(R)													\
{																\
	if (--R##REG(R##DSTREG))									\
	{															\
		PC += (PARAM_WORD_NO_INC()<<4)+0x10;					\
		COUNT_CYCLES(3);										\
	}															\
	else														\
	{															\
		SKIP_WORD;												\
		COUNT_CYCLES(2);										\
	}															\
}
static void dsj_a (void) { DSJ(A); }
static void dsj_b (void) { DSJ(B); }

#define DSJEQ(R)												\
{																\
	if (!NOTZ_FLAG)												\
	{															\
		if (--R##REG(R##DSTREG))								\
		{														\
			PC += (PARAM_WORD_NO_INC()<<4)+0x10;				\
			COUNT_CYCLES(3);									\
		}														\
		else													\
		{														\
			SKIP_WORD;											\
			COUNT_CYCLES(2);									\
		}														\
	}															\
	else														\
	{															\
		SKIP_WORD;												\
		COUNT_CYCLES(2);										\
	}															\
}
static void dsjeq_a (void) { DSJEQ(A); }
static void dsjeq_b (void) { DSJEQ(B); }

#define DSJNE(R)												\
{																\
	if (NOTZ_FLAG)												\
	{															\
		if (--R##REG(R##DSTREG))								\
		{														\
			PC += (PARAM_WORD_NO_INC()<<4)+0x10;				\
			COUNT_CYCLES(3);									\
		}														\
		else													\
		{														\
			SKIP_WORD;											\
			COUNT_CYCLES(2);									\
		}														\
	}															\
	else														\
	{															\
		SKIP_WORD;												\
		COUNT_CYCLES(2);										\
	}															\
}
static void dsjne_a (void) { DSJNE(A); }
static void dsjne_b (void) { DSJNE(B); }

#define DSJS(R)													\
{									   							\
	if (state.op & 0x0400)										\
	{															\
		if (--R##REG(R##DSTREG))								\
		{														\
			PC -= ((PARAM_K)<<4);								\
			COUNT_CYCLES(2);									\
		}														\
		else													\
			COUNT_CYCLES(3);									\
	}															\
	else														\
	{															\
		if (--R##REG(R##DSTREG))								\
		{														\
			PC += ((PARAM_K)<<4);								\
			COUNT_CYCLES(2);									\
		}														\
		else													\
			COUNT_CYCLES(3);									\
	}															\
}
static void dsjs_a (void) { DSJS(A); }
static void dsjs_b (void) { DSJS(B); }

static void emu(void)
{
	/* in RUN state, this instruction is a NOP */
	COUNT_CYCLES(6);
}

#define EXGPC(R)												\
{			  													\
	INT32 *rd = &R##REG(R##DSTREG);								\
	INT32 temppc = *rd;											\
	*rd = PC;													\
	PC = temppc;												\
	COUNT_CYCLES(2);											\
}
static void exgpc_a (void) { EXGPC(A); }
static void exgpc_b (void) { EXGPC(B); }

#define GETPC(R)												\
{																\
	R##REG(R##DSTREG) = PC;										\
	COUNT_CYCLES(1);											\
}
static void getpc_a (void) { GETPC(A); }
static void getpc_b (void) { GETPC(B); }

#define GETST(R)												\
{			  													\
	R##REG(R##DSTREG) = GET_ST();								\
	COUNT_CYCLES(1);											\
}
static void getst_a (void) { GETST(A); }
static void getst_b (void) { GETST(B); }

#define j_xx_8(TAKE)			  								\
{	   															\
	if (ADSTREG)												\
	{															\
		if (TAKE)												\
		{														\
			PC += (PARAM_REL8 << 4);							\
			COUNT_CYCLES(2);									\
		}														\
		else													\
			COUNT_CYCLES(1);									\
	}															\
	else														\
	{															\
		if (TAKE)												\
		{														\
			PC = PARAM_LONG_NO_INC();							\
			COUNT_CYCLES(3);									\
		}														\
		else													\
		{														\
			SKIP_LONG;											\
			COUNT_CYCLES(4);									\
		}														\
	}															\
}

#define j_xx_0(TAKE)											\
{																\
	if (ADSTREG)												\
	{															\
		if (TAKE)												\
		{														\
			PC += (PARAM_REL8 << 4);							\
			COUNT_CYCLES(2);									\
		}														\
		else													\
			COUNT_CYCLES(1);									\
	}															\
	else														\
	{															\
		if (TAKE)												\
		{														\
			PC += (PARAM_WORD_NO_INC()<<4)+0x10;				\
			COUNT_CYCLES(3);									\
		}														\
		else													\
		{														\
			SKIP_WORD;											\
			COUNT_CYCLES(2);									\
		}														\
	}															\
}

#define j_xx_x(TAKE)											\
{																\
	if (TAKE)													\
	{															\
		PC += (PARAM_REL8 << 4);								\
		COUNT_CYCLES(2);										\
	}															\
	else														\
		COUNT_CYCLES(1);										\
}

static void j_UC_0(void)
{
	j_xx_0(1);
}
static void j_UC_8(void)
{
	j_xx_8(1);
}
static void j_UC_x(void)
{
	j_xx_x(1);
}
static void j_P_0(void)
{
	j_xx_0(!N_FLAG && NOTZ_FLAG);
}
static void j_P_8(void)
{
	j_xx_8(!N_FLAG && NOTZ_FLAG);
}
static void j_P_x(void)
{
	j_xx_x(!N_FLAG && NOTZ_FLAG);
}
static void j_LS_0(void)
{
	j_xx_0(C_FLAG || !NOTZ_FLAG);
}
static void j_LS_8(void)
{
	j_xx_8(C_FLAG || !NOTZ_FLAG);
}
static void j_LS_x(void)
{
	j_xx_x(C_FLAG || !NOTZ_FLAG);
}
static void j_HI_0(void)
{
	j_xx_0(!C_FLAG && NOTZ_FLAG);
}
static void j_HI_8(void)
{
	j_xx_8(!C_FLAG && NOTZ_FLAG);
}
static void j_HI_x(void)
{
	j_xx_x(!C_FLAG && NOTZ_FLAG);
}
static void j_LT_0(void)
{
	j_xx_0((N_FLAG && !V_FLAG) || (!N_FLAG && V_FLAG));
}
static void j_LT_8(void)
{
	j_xx_8((N_FLAG && !V_FLAG) || (!N_FLAG && V_FLAG));
}
static void j_LT_x(void)
{
	j_xx_x((N_FLAG && !V_FLAG) || (!N_FLAG && V_FLAG));
}
static void j_GE_0(void)
{
	j_xx_0((N_FLAG && V_FLAG) || (!N_FLAG && !V_FLAG));
}
static void j_GE_8(void)
{
	j_xx_8((N_FLAG && V_FLAG) || (!N_FLAG && !V_FLAG));
}
static void j_GE_x(void)
{
	j_xx_x((N_FLAG && V_FLAG) || (!N_FLAG && !V_FLAG));
}
static void j_LE_0(void)
{
	j_xx_0((N_FLAG && !V_FLAG) || (!N_FLAG && V_FLAG) || !NOTZ_FLAG);
}
static void j_LE_8(void)
{
	j_xx_8((N_FLAG && !V_FLAG) || (!N_FLAG && V_FLAG) || !NOTZ_FLAG);
}
static void j_LE_x(void)
{
	j_xx_x((N_FLAG && !V_FLAG) || (!N_FLAG && V_FLAG) || !NOTZ_FLAG);
}
static void j_GT_0(void)
{
	j_xx_0((N_FLAG && V_FLAG && NOTZ_FLAG) || (!N_FLAG && !V_FLAG && NOTZ_FLAG));
}
static void j_GT_8(void)
{
	j_xx_8((N_FLAG && V_FLAG && NOTZ_FLAG) || (!N_FLAG && !V_FLAG && NOTZ_FLAG));
}
static void j_GT_x(void)
{
	j_xx_x((N_FLAG && V_FLAG && NOTZ_FLAG) || (!N_FLAG && !V_FLAG && NOTZ_FLAG));
}
static void j_C_0(void)
{
	j_xx_0(C_FLAG);
}
static void j_C_8(void)
{
	j_xx_8(C_FLAG);
}
static void j_C_x(void)
{
	j_xx_x(C_FLAG);
}
static void j_NC_0(void)
{
	j_xx_0(!C_FLAG);
}
static void j_NC_8(void)
{
	j_xx_8(!C_FLAG);
}
static void j_NC_x(void)
{
	j_xx_x(!C_FLAG);
}
static void j_EQ_0(void)
{
	j_xx_0(!NOTZ_FLAG);
}
static void j_EQ_8(void)
{
	j_xx_8(!NOTZ_FLAG);
}
static void j_EQ_x(void)
{
	j_xx_x(!NOTZ_FLAG);
}
static void j_NE_0(void)
{
	j_xx_0(NOTZ_FLAG);
}
static void j_NE_8(void)
{
	j_xx_8(NOTZ_FLAG);
}
static void j_NE_x(void)
{
	j_xx_x(NOTZ_FLAG);
}
static void j_V_0(void)
{
	j_xx_0(V_FLAG);
}
static void j_V_8(void)
{
	j_xx_8(V_FLAG);
}
static void j_V_x(void)
{
	j_xx_x(V_FLAG);
}
static void j_NV_0(void)
{
	j_xx_0(!V_FLAG);
}
static void j_NV_8(void)
{
	j_xx_8(!V_FLAG);
}
static void j_NV_x(void)
{
	j_xx_x(!V_FLAG);
}
static void j_N_0(void)
{
	j_xx_0(N_FLAG);
}
static void j_N_8(void)
{
	j_xx_8(N_FLAG);
}
static void j_N_x(void)
{
	j_xx_x(N_FLAG);
}
static void j_NN_0(void)
{
	j_xx_0(!N_FLAG);
}
static void j_NN_8(void)
{
	j_xx_8(!N_FLAG);
}
static void j_NN_x(void)
{
	j_xx_x(!N_FLAG);
}

#define JUMP(R)													\
{																\
	PC = R##REG(R##DSTREG);										\
	COUNT_CYCLES(2);											\
}
static void jump_a (void) { JUMP(A); }
static void jump_b (void) { JUMP(B); }

static void popst(void)
{
	SET_ST(POP());
	COUNT_CYCLES(8);
}

static void pushst(void)
{
	PUSH(GET_ST());
	COUNT_CYCLES(2);
}

#define PUTST(R)												\
{																\
	SET_ST(R##REG(R##DSTREG));									\
	COUNT_CYCLES(3);											\
}
static void putst_a (void) { PUTST(A); }
static void putst_b (void) { PUTST(B); }

static void reti(void)
{
	INT32 st = POP();
	PC = POP();
	SET_ST(st);
	COUNT_CYCLES(11);
}

static void rets(void)
{
	UINT32 offs;
	PC = POP();
	offs = PARAM_N;
	if (offs)
	{
		SP+=(offs<<4);
	}
	COUNT_CYCLES(7);
}

#define REV(R)													\
{																\
    R##REG(R##DSTREG) = 0x0008;									\
	COUNT_CYCLES(1);											\
}
static void rev_a (void) { REV(A); }
static void rev_b (void) { REV(B); }

static void trap(void)
{
	UINT32 t = PARAM_N;
	if (t)
	{
		PUSH(PC);
		PUSH(GET_ST());
	}
	RESET_ST();
	PC = RLONG(0xffffffe0-(t<<5));
	COUNT_CYCLES(16);
}
