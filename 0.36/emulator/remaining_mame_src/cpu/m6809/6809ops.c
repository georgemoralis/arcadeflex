/*

HNZVC

? = undefined
* = affected
- = unaffected
0 = cleared
1 = set
# = CCr directly affected by instruction
@ = special - carry set if bit 7 is set

*/

#ifdef NEW
static void illegal( void )
#else
INLINE void illegal( void )
#endif
{
	if( errorlog )
		fprintf(errorlog, "M6809: illegal opcode at %04x\n",PC);
}

#if macintosh
#pragma mark ____0x____
#endif

/* $00 NEG direct ?**** */
INLINE void neg_di( void )
{
	UINT16 r,t;
	DIRBYTE(t);
	r = -t;
	CLR_NZVC;
	SET_FLAGS8(0,t,r);
	WM(EAD,r);
}

/* $01 ILLEGAL */

/* $02 ILLEGAL */

/* $03 COM direct -**01 */
INLINE void com_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	t = ~t;
	CLR_NZV;
	SET_NZ8(t);
	SEC;
	WM(EAD,t);
}

/* $04 LSR direct -0*-* */
INLINE void lsr_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	CLR_NZC;
	CC |= (t & CC_C);
	t >>= 1;
	SET_Z8(t);
	WM(EAD,t);
}

/* $05 ILLEGAL */

/* $06 ROR direct -**-* */
INLINE void ror_di( void )
{
	UINT8 t,r;
	DIRBYTE(t);
	r= (CC & CC_C) << 7;
	CLR_NZC;
	CC |= (t & CC_C);
	r |= t>>1;
	SET_NZ8(r);
	WM(EAD,r);
}

/* $07 ASR direct ?**-* */
INLINE void asr_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	CLR_NZC;
	CC |= (t & CC_C);
	t = (t & 0x80) | (t >> 1);
	SET_NZ8(t);
	WM(EAD,t);
}

/* $08 ASL direct ?**** */
INLINE void asl_di( void )
{
	UINT16 t,r;
	DIRBYTE(t);
	r = t << 1;
	CLR_NZVC;
	SET_FLAGS8(t,t,r);
	WM(EAD,r);
}

/* $09 ROL direct -**** */
INLINE void rol_di( void )
{
	UINT16 t,r;
	DIRBYTE(t);
	r = (CC & CC_C) | (t << 1);
	CLR_NZVC;
	SET_FLAGS8(t,t,r);
	WM(EAD,r);
}

/* $0A DEC direct -***- */
INLINE void dec_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	--t;
	CLR_NZV;
	SET_FLAGS8D(t);
	WM(EAD,t);
}

/* $0B ILLEGAL */

/* $OC INC direct -***- */
INLINE void inc_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	++t;
	CLR_NZV;
	SET_FLAGS8I(t);
	WM(EAD,t);
}

/* $OD TST direct -**0- */
INLINE void tst_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	CLR_NZV;
	SET_NZ8(t);
}

/* $0E JMP direct ----- */
INLINE void jmp_di( void )
{
	DIRECT;
	PCD = EAD;
	CHANGE_PC;
}

/* $0F CLR direct -0100 */
INLINE void clr_di( void )
{
	DIRECT;
	WM(EAD,0);
	CLR_NZVC;
	SEZ;
}

#if macintosh
#pragma mark ____1x____
#endif

/* $10 FLAG */

/* $11 FLAG */

/* $12 NOP inherent ----- */
INLINE void nop( void )
{
	;
}

/* $13 SYNC inherent ----- */
INLINE void sync( void )
{
	/* SYNC stops processing instructions until an interrupt request happens. */
	/* This doesn't require the corresponding interrupt to be enabled: if it */
	/* is disabled, execution continues with the next instruction. */
	m6809.int_state |= M6809_SYNC;	 /* HJB 990227 */
	CHECK_IRQ_LINES;
	/* if M6809_SYNC has not been cleared by CHECK_IRQ_LINES,
	 * stop execution until the interrupt lines change. */
	if( m6809.int_state & M6809_SYNC )
		if (m6809_ICount > 0) m6809_ICount = 0;
}

/* $14 ILLEGAL */

/* $15 ILLEGAL */

/* $16 LBRA relative ----- */
INLINE void lbra( void )
{
	IMMWORD(ea);
	PC += EA;
	CHANGE_PC;

	if ( EA == 0xfffd )  /* EHC 980508 speed up busy loop */
		if ( m6809_ICount > 0)
			m6809_ICount = 0;
}

/* $17 LBSR relative ----- */
INLINE void lbsr( void )
{
	IMMWORD(ea);
	PUSHWORD(pPC);
	PC += EA;
	CHANGE_PC;
}

/* $18 ILLEGAL */

#if 1
/* $19 DAA inherent (A) -**0* */
INLINE void daa( void )
{
	UINT8 msn, lsn;
	UINT16 t, cf = 0;
	msn = A & 0xf0; lsn = A & 0x0f;
	if( lsn>0x09 || CC & CC_H) cf |= 0x06;
	if( msn>0x80 && lsn>0x09 ) cf |= 0x60;
	if( msn>0x90 || CC & CC_C) cf |= 0x60;
	t = cf + A;
	CLR_NZV; /* keep carry from previous operation */
	SET_NZ8((UINT8)t); SET_C8(t);
	A = t;
}
#else
/* $19 DAA inherent (A) -**0* */
INLINE void daa( void )
{
	UINT16 t;
	t = A;
	if (CC & CC_H) t+=0x06;
	if ((t&0x0f)>9) t+=0x06;		/* ASG -- this code is broken! $66+$99=$FF -> DAA should = $65, we get $05! */
	if (CC & CC_C) t+=0x60;
	if ((t&0xf0)>0x90) t+=0x60;
	if (t&0x100) SEC;
	A = t;
}
#endif

/* $1A ORCC immediate ##### */
INLINE void orcc( void )
{
	UINT8 t;
	IMMBYTE(t);
	CC |= t;
	CHECK_IRQ_LINES;	/* HJB 990116 */
}

/* $1B ILLEGAL */

/* $1C ANDCC immediate ##### */
INLINE void andcc( void )
{
	UINT8 t;
	IMMBYTE(t);
	CC &= t;
	CHECK_IRQ_LINES;	/* HJB 990116 */
}

/* $1D SEX inherent -**0- */
INLINE void sex( void )
{
	UINT16 t;
	t = SIGNED(B);
	D = t;
	CLR_NZV;
	SET_NZ16(t);
}

/* $1E EXG inherent ----- */
INLINE void exg( void )
{
	UINT16 t1,t2;
	UINT8 tb;

	IMMBYTE(tb);
	if( (tb^(tb>>4)) & 0x08 )	/* HJB 990225: mixed 8/16 bit case? */
	{
		/* transfer $ff to both registers */
		t1 = t2 = 0xff;
	}
	else
	{
		switch(tb>>4) {
			case  0: t1 = D;  break;
			case  1: t1 = X;  break;
			case  2: t1 = Y;  break;
			case  3: t1 = U;  break;
			case  4: t1 = S;  break;
			case  5: t1 = PC; break;
			case  8: t1 = A;  break;
			case  9: t1 = B;  break;
			case 10: t1 = CC; break;
			case 11: t1 = DP; break;
			default: t1 = 0xff;
		}
		switch(tb&15) {
			case  0: t2 = D;  break;
			case  1: t2 = X;  break;
			case  2: t2 = Y;  break;
			case  3: t2 = U;  break;
			case  4: t2 = S;  break;
			case  5: t2 = PC; break;
			case  8: t2 = A;  break;
			case  9: t2 = B;  break;
			case 10: t2 = CC; break;
			case 11: t2 = DP; break;
			default: t2 = 0xff;
        }
	}
	switch(tb>>4) {
		case  0: D = t2;  break;
		case  1: X = t2;  break;
		case  2: Y = t2;  break;
		case  3: U = t2;  break;
		case  4: S = t2;  break;
		case  5: PC = t2; CHANGE_PC; break;
		case  8: A = t2;  break;
		case  9: B = t2;  break;
		case 10: CC = t2; break;
		case 11: DP = t2; break;
	}
	switch(tb&15) {
		case  0: D = t1;  break;
		case  1: X = t1;  break;
		case  2: Y = t1;  break;
		case  3: U = t1;  break;
		case  4: S = t1;  break;
		case  5: PC = t1; CHANGE_PC; break;
		case  8: A = t1;  break;
		case  9: B = t1;  break;
		case 10: CC = t1; break;
		case 11: DP = t1; break;
	}
}

/* $1F TFR inherent ----- */
INLINE void tfr( void )
{
	UINT8 tb;
	UINT16 t;

	IMMBYTE(tb);
	if( (tb^(tb>>4)) & 0x08 )	/* HJB 990225: mixed 8/16 bit case? */
	{
		/* transfer $ff to register */
		t = 0xff;
    }
	else
	{
		switch(tb>>4) {
			case  0: t = D;  break;
			case  1: t = X;  break;
			case  2: t = Y;  break;
			case  3: t = U;  break;
			case  4: t = S;  break;
			case  5: t = PC; break;
			case  8: t = A;  break;
			case  9: t = B;  break;
			case 10: t = CC; break;
			case 11: t = DP; break;
			default: t = 0xff;
        }
	}
	switch(tb&15) {
		case  0: D = t;  break;
		case  1: X = t;  break;
		case  2: Y = t;  break;
		case  3: U = t;  break;
		case  4: S = t;  break;
		case  5: PC = t; CHANGE_PC; break;
		case  8: A = t;  break;
		case  9: B = t;  break;
		case 10: CC = t; break;
		case 11: DP = t; break;
    }
}

#if macintosh
#pragma mark ____2x____
#endif

/* $20 BRA relative ----- */
INLINE void bra( void )
{
	UINT8 t;
	IMMBYTE(t);
	PC += SIGNED(t);
    CHANGE_PC;
	/* JB 970823 - speed up busy loops */
	if( t == 0xfe )
		if( m6809_ICount > 0 ) m6809_ICount = 0;
}

/* $21 BRN relative ----- */
INLINE void brn( void )
{
	UINT8 t;
	IMMBYTE(t);
}

/* $1021 LBRN relative ----- */
INLINE void lbrn( void )
{
	IMMWORD(ea);
}

/* $22 BHI relative ----- */
INLINE void bhi( void )
{
	BRANCH( !(CC & (CC_Z|CC_C)) );
}

/* $1022 LBHI relative ----- */
INLINE void lbhi( void )
{
	LBRANCH( !(CC & (CC_Z|CC_C)) );
}

/* $23 BLS relative ----- */
INLINE void bls( void )
{
	BRANCH( (CC & (CC_Z|CC_C)) );
}

/* $1023 LBLS relative ----- */
INLINE void lbls( void )
{
	LBRANCH( (CC&(CC_Z|CC_C)) );
}

/* $24 BCC relative ----- */
INLINE void bcc( void )
{
	BRANCH( !(CC&CC_C) );
}

/* $1024 LBCC relative ----- */
INLINE void lbcc( void )
{
	LBRANCH( !(CC&CC_C) );
}

/* $25 BCS relative ----- */
INLINE void bcs( void )
{
	BRANCH( (CC&CC_C) );
}

/* $1025 LBCS relative ----- */
INLINE void lbcs( void )
{
	LBRANCH( (CC&CC_C) );
}

/* $26 BNE relative ----- */
INLINE void bne( void )
{
	BRANCH( !(CC&CC_Z) );
}

/* $1026 LBNE relative ----- */
INLINE void lbne( void )
{
	LBRANCH( !(CC&CC_Z) );
}

/* $27 BEQ relative ----- */
INLINE void beq( void )
{
	BRANCH( (CC&CC_Z) );
}

/* $1027 LBEQ relative ----- */
INLINE void lbeq( void )
{
	LBRANCH( (CC&CC_Z) );
}

/* $28 BVC relative ----- */
INLINE void bvc( void )
{
	BRANCH( !(CC&CC_V) );
}

/* $1028 LBVC relative ----- */
INLINE void lbvc( void )
{
	LBRANCH( !(CC&CC_V) );
}

/* $29 BVS relative ----- */
INLINE void bvs( void )
{
	BRANCH( (CC&CC_V) );
}

/* $1029 LBVS relative ----- */
INLINE void lbvs( void )
{
	LBRANCH( (CC&CC_V) );
}

/* $2A BPL relative ----- */
INLINE void bpl( void )
{
	BRANCH( !(CC&CC_N) );
}

/* $102A LBPL relative ----- */
INLINE void lbpl( void )
{
	LBRANCH( !(CC&CC_N) );
}

/* $2B BMI relative ----- */
INLINE void bmi( void )
{
	BRANCH( (CC&CC_N) );
}

/* $102B LBMI relative ----- */
INLINE void lbmi( void )
{
	LBRANCH( (CC&CC_N) );
}

/* $2C BGE relative ----- */
INLINE void bge( void )
{
	BRANCH( !NXORV );
}

/* $102C LBGE relative ----- */
INLINE void lbge( void )
{
	LBRANCH( !NXORV );
}

/* $2D BLT relative ----- */
INLINE void blt( void )
{
	BRANCH( NXORV );
}

/* $102D LBLT relative ----- */
INLINE void lblt( void )
{
	LBRANCH( NXORV );
}

/* $2E BGT relative ----- */
INLINE void bgt( void )
{
	BRANCH( !(NXORV || (CC&CC_Z)) );
}

/* $102E LBGT relative ----- */
INLINE void lbgt( void )
{
	LBRANCH( !(NXORV || (CC&CC_Z)) );
}

/* $2F BLE relative ----- */
INLINE void ble( void )
{
	BRANCH( (NXORV || (CC&CC_Z)) );
}

/* $102F LBLE relative ----- */
INLINE void lble( void )
{
	LBRANCH( (NXORV || (CC&CC_Z)) );
}

#if macintosh
#pragma mark ____3x____
#endif

/* $30 LEAX indexed --*-- */
INLINE void leax( void )
{
	fetch_effective_address();
    X = EA;
	CLR_Z;
	SET_Z(X);
}

/* $31 LEAY indexed --*-- */
INLINE void leay( void )
{
	fetch_effective_address();
    Y = EA;
	CLR_Z;
	SET_Z(Y);
}

/* $32 LEAS indexed ----- */
INLINE void leas( void )
{
	fetch_effective_address();
    S = EA;
	m6809.int_state |= M6809_LDS;
}

/* $33 LEAU indexed ----- */
INLINE void leau( void )
{
	fetch_effective_address();
    U = EA;
}

/* $34 PSHS inherent ----- */
INLINE void pshs( void )
{
	UINT8 t;
	IMMBYTE(t);
	if( t&0x80 ) { PUSHWORD(pPC); m6809_ICount -= 2; }
	if( t&0x40 ) { PUSHWORD(pU);  m6809_ICount -= 2; }
	if( t&0x20 ) { PUSHWORD(pY);  m6809_ICount -= 2; }
	if( t&0x10 ) { PUSHWORD(pX);  m6809_ICount -= 2; }
	if( t&0x08 ) { PUSHBYTE(DP);  m6809_ICount -= 1; }
	if( t&0x04 ) { PUSHBYTE(B);   m6809_ICount -= 1; }
	if( t&0x02 ) { PUSHBYTE(A);   m6809_ICount -= 1; }
	if( t&0x01 ) { PUSHBYTE(CC);  m6809_ICount -= 1; }
}

/* 35 PULS inherent ----- */
INLINE void puls( void )
{
	UINT8 t;
	IMMBYTE(t);
	if( t&0x01 ) { PULLBYTE(CC); m6809_ICount -= 1; }
	if( t&0x02 ) { PULLBYTE(A);  m6809_ICount -= 1; }
	if( t&0x04 ) { PULLBYTE(B);  m6809_ICount -= 1; }
	if( t&0x08 ) { PULLBYTE(DP); m6809_ICount -= 1; }
	if( t&0x10 ) { PULLWORD(XD); m6809_ICount -= 2; }
	if( t&0x20 ) { PULLWORD(YD); m6809_ICount -= 2; }
	if( t&0x40 ) { PULLWORD(UD); m6809_ICount -= 2; }
	if( t&0x80 ) { PULLWORD(PCD); CHANGE_PC; m6809_ICount -= 2; }

	/* HJB 990225: moved check after all PULLs */
	if( t&0x01 ) { CHECK_IRQ_LINES; }
}

/* $36 PSHU inherent ----- */
INLINE void pshu( void )
{
	UINT8 t;
	IMMBYTE(t);
	if( t&0x80 ) { PSHUWORD(pPC); m6809_ICount -= 2; }
	if( t&0x40 ) { PSHUWORD(pS);  m6809_ICount -= 2; }
	if( t&0x20 ) { PSHUWORD(pY);  m6809_ICount -= 2; }
	if( t&0x10 ) { PSHUWORD(pX);  m6809_ICount -= 2; }
	if( t&0x08 ) { PSHUBYTE(DP);  m6809_ICount -= 1; }
	if( t&0x04 ) { PSHUBYTE(B);   m6809_ICount -= 1; }
	if( t&0x02 ) { PSHUBYTE(A);   m6809_ICount -= 1; }
	if( t&0x01 ) { PSHUBYTE(CC);  m6809_ICount -= 1; }
}

/* 37 PULU inherent ----- */
INLINE void pulu( void )
{
	UINT8 t;
	IMMBYTE(t);
	if( t&0x01 ) { PULUBYTE(CC); m6809_ICount -= 1; }
	if( t&0x02 ) { PULUBYTE(A);  m6809_ICount -= 1; }
	if( t&0x04 ) { PULUBYTE(B);  m6809_ICount -= 1; }
	if( t&0x08 ) { PULUBYTE(DP); m6809_ICount -= 1; }
	if( t&0x10 ) { PULUWORD(XD); m6809_ICount -= 2; }
	if( t&0x20 ) { PULUWORD(YD); m6809_ICount -= 2; }
	if( t&0x40 ) { PULUWORD(SD); m6809_ICount -= 2; }
	if( t&0x80 ) { PULUWORD(PCD); CHANGE_PC; m6809_ICount -= 2; }

	/* HJB 990225: moved check after all PULLs */
	if( t&0x01 ) { CHECK_IRQ_LINES; }
}

/* $38 ILLEGAL */

/* $39 RTS inherent ----- */
INLINE void rts( void )
{
	PULLWORD(PCD);
	CHANGE_PC;
}

/* $3A ABX inherent ----- */
INLINE void abx( void )
{
	X += B;
}

/* $3B RTI inherent ##### */
INLINE void rti( void )
{
	UINT8 t;
	PULLBYTE(CC);
	t = CC & CC_E;		/* HJB 990225: entire state saved? */
	if(t)
	{
        m6809_ICount -= 9;
		PULLBYTE(A);
		PULLBYTE(B);
		PULLBYTE(DP);
		PULLWORD(XD);
		PULLWORD(YD);
		PULLWORD(UD);
	}
	PULLWORD(PCD);
	CHANGE_PC;
	CHECK_IRQ_LINES;	/* HJB 990116 */
}

/* $3C CWAI inherent ----1 */
INLINE void cwai( void )
{
	UINT8 t;
	IMMBYTE(t);
	CC &= t;
	/*
     * CWAI stacks the entire machine state on the hardware stack,
     * then waits for an interrupt; when the interrupt is taken
     * later, the state is *not* saved again after CWAI.
     */
	CC |= CC_E; 		/* HJB 990225: save entire state */
	PUSHWORD(pPC);
	PUSHWORD(pU);
	PUSHWORD(pY);
	PUSHWORD(pX);
	PUSHBYTE(DP);
	PUSHBYTE(B);
	PUSHBYTE(A);
	PUSHBYTE(CC);
	m6809.int_state |= M6809_CWAI;	 /* HJB 990228 */
    CHECK_IRQ_LINES;    /* HJB 990116 */
	if( m6809.int_state & M6809_CWAI )
		if( m6809_ICount > 0 )
			m6809_ICount = 0;
}

/* $3D MUL inherent --*-@ */
INLINE void mul( void )
{
	UINT16 t;
	t = A * B;
	CLR_ZC; SET_Z16(t); if(t&0x80) SEC;
	D = t;
}

/* $3E ILLEGAL */

/* $3F SWI (SWI2 SWI3) absolute indirect ----- */
INLINE void swi( void )
{
	CC |= CC_E; 			/* HJB 980225: save entire state */
	PUSHWORD(pPC);
	PUSHWORD(pU);
	PUSHWORD(pY);
	PUSHWORD(pX);
	PUSHBYTE(DP);
	PUSHBYTE(B);
	PUSHBYTE(A);
	PUSHBYTE(CC);
	CC |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
	PCD=RM16(0xfffa);
	CHANGE_PC;
}

/* $103F SWI2 absolute indirect ----- */
INLINE void swi2( void )
{
	CC |= CC_E; 			/* HJB 980225: save entire state */
	PUSHWORD(pPC);
	PUSHWORD(pU);
	PUSHWORD(pY);
	PUSHWORD(pX);
	PUSHBYTE(DP);
	PUSHBYTE(B);
	PUSHBYTE(A);
    PUSHBYTE(CC);
	PCD = RM16(0xfff4);
	CHANGE_PC;
}

/* $113F SWI3 absolute indirect ----- */
INLINE void swi3( void )
{
	CC |= CC_E; 			/* HJB 980225: save entire state */
	PUSHWORD(pPC);
	PUSHWORD(pU);
	PUSHWORD(pY);
	PUSHWORD(pX);
	PUSHBYTE(DP);
	PUSHBYTE(B);
	PUSHBYTE(A);
    PUSHBYTE(CC);
	PCD = RM16(0xfff2);
	CHANGE_PC;
}

#if macintosh
#pragma mark ____4x____
#endif

/* $40 NEGA inherent ?**** */
INLINE void nega( void )
{
	UINT16 r;
	r = -A;
	CLR_NZVC;
	SET_FLAGS8(0,A,r);
	A = r;
}

/* $41 ILLEGAL */

/* $42 ILLEGAL */

/* $43 COMA inherent -**01 */
INLINE void coma( void )
{
	A = ~A;
	CLR_NZV;
	SET_NZ8(A);
	SEC;
}

/* $44 LSRA inherent -0*-* */
INLINE void lsra( void )
{
	CLR_NZC;
	CC |= (A & CC_C);
	A >>= 1;
	SET_Z8(A);
}

/* $45 ILLEGAL */

/* $46 RORA inherent -**-* */
INLINE void rora( void )
{
	UINT8 r;
	r = (CC & CC_C) << 7;
	CLR_NZC;
	CC |= (A & CC_C);
	r |= A >> 1;
	SET_NZ8(r);
	A = r;
}

/* $47 ASRA inherent ?**-* */
INLINE void asra( void )
{
	CLR_NZC;
	CC |= (A & CC_C);
	A = (A & 0x80) | (A >> 1);
	SET_NZ8(A);
}

/* $48 ASLA inherent ?**** */
INLINE void asla( void )
{
	UINT16 r;
	r = A << 1;
	CLR_NZVC;
	SET_FLAGS8(A,A,r);
	A = r;
}

/* $49 ROLA inherent -**** */
INLINE void rola( void )
{
	UINT16 t,r;
	t = A;
	r = (CC & CC_C) | (t<<1);
	CLR_NZVC; SET_FLAGS8(t,t,r);
	A = r;
}

/* $4A DECA inherent -***- */
INLINE void deca( void )
{
	--A;
	CLR_NZV;
	SET_FLAGS8D(A);
}

/* $4B ILLEGAL */

/* $4C INCA inherent -***- */
INLINE void inca( void )
{
	++A;
	CLR_NZV;
	SET_FLAGS8I(A);
}

/* $4D TSTA inherent -**0- */
INLINE void tsta( void )
{
	CLR_NZV;
	SET_NZ8(A);
}

/* $4E ILLEGAL */

/* $4F CLRA inherent -0100 */
INLINE void clra( void )
{
	A = 0;
	CLR_NZVC; SEZ;
}

#if macintosh
#pragma mark ____5x____
#endif

/* $50 NEGB inherent ?**** */
INLINE void negb( void )
{
	UINT16 r;
	r = -B;
	CLR_NZVC;
	SET_FLAGS8(0,B,r);
	B = r;
}

/* $51 ILLEGAL */

/* $52 ILLEGAL */

/* $53 COMB inherent -**01 */
INLINE void comb( void )
{
	B = ~B;
	CLR_NZV;
	SET_NZ8(B);
	SEC;
}

/* $54 LSRB inherent -0*-* */
INLINE void lsrb( void )
{
	CLR_NZC;
	CC |= (B & CC_C);
	B >>= 1;
	SET_Z8(B);
}

/* $55 ILLEGAL */

/* $56 RORB inherent -**-* */
INLINE void rorb( void )
{
	UINT8 r;
	r = (CC & CC_C) << 7;
	CLR_NZC;
	CC |= (B & CC_C);
	r |= B >> 1;
	SET_NZ8(r);
	B = r;
}

/* $57 ASRB inherent ?**-* */
INLINE void asrb( void )
{
	CLR_NZC;
	CC |= (B & CC_C);
	B= (B & 0x80) | (B >> 1);
	SET_NZ8(B);
}

/* $58 ASLB inherent ?**** */
INLINE void aslb( void )
{
	UINT16 r;
	r = B << 1;
	CLR_NZVC;
	SET_FLAGS8(B,B,r);
	B = r;
}

/* $59 ROLB inherent -**** */
INLINE void rolb( void )
{
	UINT16 t,r;
	t = B;
	r = CC & CC_C;
	r |= t << 1;
	CLR_NZVC;
	SET_FLAGS8(t,t,r);
	B = r;
}

/* $5A DECB inherent -***- */
INLINE void decb( void )
{
	--B;
	CLR_NZV;
	SET_FLAGS8D(B);
}

/* $5B ILLEGAL */

/* $5C INCB inherent -***- */
INLINE void incb( void )
{
	++B;
	CLR_NZV;
	SET_FLAGS8I(B);
}

/* $5D TSTB inherent -**0- */
INLINE void tstb( void )
{
	CLR_NZV;
	SET_NZ8(B);
}

/* $5E ILLEGAL */

/* $5F CLRB inherent -0100 */
INLINE void clrb( void )
{
	B = 0;
	CLR_NZVC; SEZ;
}

#if macintosh
#pragma mark ____6x____
#endif

/* $60 NEG indexed ?**** */
INLINE void neg_ix( void )
{
	UINT16 r,t;
	fetch_effective_address();
	t = RM(EAD);
	r=-t;
	CLR_NZVC;
	SET_FLAGS8(0,t,r);
	WM(EAD,r);
}

/* $61 ILLEGAL */

/* $62 ILLEGAL */

/* $63 COM indexed -**01 */
INLINE void com_ix( void )
{
	UINT8 t;
	fetch_effective_address();
	t = ~RM(EAD);
	CLR_NZV;
	SET_NZ8(t);
	SEC;
	WM(EAD,t);
}

/* $64 LSR indexed -0*-* */
INLINE void lsr_ix( void )
{
	UINT8 t;
	fetch_effective_address();
	t=RM(EAD);
	CLR_NZC;
	CC |= (t & CC_C);
	t>>=1; SET_Z8(t);
	WM(EAD,t);
}

/* $65 ILLEGAL */

/* $66 ROR indexed -**-* */
INLINE void ror_ix( void )
{
	UINT8 t,r;
	fetch_effective_address();
	t=RM(EAD);
	r = (CC & CC_C) << 7;
	CLR_NZC;
	CC |= (t & CC_C);
	r |= t>>1; SET_NZ8(r);
	WM(EAD,r);
}

/* $67 ASR indexed ?**-* */
INLINE void asr_ix( void )
{
	UINT8 t;
	fetch_effective_address();
	t=RM(EAD);
	CLR_NZC;
	CC |= (t & CC_C);
	t=(t&0x80)|(t>>1);
	SET_NZ8(t);
	WM(EAD,t);
}

/* $68 ASL indexed ?**** */
INLINE void asl_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t=RM(EAD);
	r = t << 1;
	CLR_NZVC;
	SET_FLAGS8(t,t,r);
	WM(EAD,r);
}

/* $69 ROL indexed -**** */
INLINE void rol_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t=RM(EAD);
	r = CC & CC_C;
	r |= t << 1;
	CLR_NZVC;
	SET_FLAGS8(t,t,r);
	WM(EAD,r);
}

/* $6A DEC indexed -***- */
INLINE void dec_ix( void )
{
	UINT8 t;
	fetch_effective_address();
	t = RM(EAD) - 1;
	CLR_NZV; SET_FLAGS8D(t);
	WM(EAD,t);
}

/* $6B ILLEGAL */

/* $6C INC indexed -***- */
INLINE void inc_ix( void )
{
	UINT8 t;
	fetch_effective_address();
	t = RM(EAD) + 1;
	CLR_NZV; SET_FLAGS8I(t);
	WM(EAD,t);
}

/* $6D TST indexed -**0- */
INLINE void tst_ix( void )
{
	UINT8 t;
	fetch_effective_address();
	t = RM(EAD);
	CLR_NZV;
	SET_NZ8(t);
}

/* $6E JMP indexed ----- */
INLINE void jmp_ix( void )
{
	fetch_effective_address();
	PCD = EAD;
	CHANGE_PC;
}

/* $6F CLR indexed -0100 */
INLINE void clr_ix( void )
{
	fetch_effective_address();
    WM(EAD,0);
	CLR_NZVC; SEZ;
}

#if macintosh
#pragma mark ____7x____
#endif

/* $70 NEG extended ?**** */
INLINE void neg_ex( void )
{
	UINT16 r,t;
	EXTBYTE(t); r=-t;
	CLR_NZVC; SET_FLAGS8(0,t,r);
	WM(EAD,r);
}

/* $71 ILLEGAL */

/* $72 ILLEGAL */

/* $73 COM extended -**01 */
INLINE void com_ex( void )
{
	UINT8 t;
	EXTBYTE(t); t = ~t;
	CLR_NZV; SET_NZ8(t); SEC;
	WM(EAD,t);
}

/* $74 LSR extended -0*-* */
INLINE void lsr_ex( void )
{
	UINT8 t;
	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
	t>>=1; SET_Z8(t);
	WM(EAD,t);
}

/* $75 ILLEGAL */

/* $76 ROR extended -**-* */
INLINE void ror_ex( void )
{
	UINT8 t,r;
	EXTBYTE(t); r=(CC & CC_C) << 7;
	CLR_NZC; CC |= (t & CC_C);
	r |= t>>1; SET_NZ8(r);
	WM(EAD,r);
}

/* $77 ASR extended ?**-* */
INLINE void asr_ex( void )
{
	UINT8 t;
	EXTBYTE(t); CLR_NZC; CC |= (t & CC_C);
	t=(t&0x80)|(t>>1);
	SET_NZ8(t);
	WM(EAD,t);
}

/* $78 ASL extended ?**** */
INLINE void asl_ex( void )
{
	UINT16 t,r;
	EXTBYTE(t); r=t<<1;
	CLR_NZVC; SET_FLAGS8(t,t,r);
	WM(EAD,r);
}

/* $79 ROL extended -**** */
INLINE void rol_ex( void )
{
	UINT16 t,r;
	EXTBYTE(t); r = (CC & CC_C) | (t << 1);
	CLR_NZVC; SET_FLAGS8(t,t,r);
	WM(EAD,r);
}

/* $7A DEC extended -***- */
INLINE void dec_ex( void )
{
	UINT8 t;
	EXTBYTE(t); --t;
	CLR_NZV; SET_FLAGS8D(t);
	WM(EAD,t);
}

/* $7B ILLEGAL */

/* $7C INC extended -***- */
INLINE void inc_ex( void )
{
	UINT8 t;
	EXTBYTE(t); ++t;
	CLR_NZV; SET_FLAGS8I(t);
	WM(EAD,t);
}

/* $7D TST extended -**0- */
INLINE void tst_ex( void )
{
	UINT8 t;
	EXTBYTE(t); CLR_NZV; SET_NZ8(t);
}

/* $7E JMP extended ----- */
INLINE void jmp_ex( void )
{
	EXTENDED;
	PCD = EAD;
	CHANGE_PC;
}

/* $7F CLR extended -0100 */
INLINE void clr_ex( void )
{
	EXTENDED;
	WM(EAD,0);
	CLR_NZVC; SEZ;
}


#if macintosh
#pragma mark ____8x____
#endif

/* $80 SUBA immediate ?**** */
INLINE void suba_im( void )
{
	UINT16 t,r;
	IMMBYTE(t);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $81 CMPA immediate ?**** */
INLINE void cmpa_im( void )
{
	UINT16	  t,r;
	IMMBYTE(t);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
}

/* $82 SBCA immediate ?**** */
INLINE void sbca_im( void )
{
	UINT16	  t,r;
	IMMBYTE(t);
	r = A - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $83 SUBD (CMPD CMPU) immediate -**** */
INLINE void subd_im( void )
{
	UINT32 r,d;
	PAIR b;
	IMMWORD(b);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $1083 CMPD immediate -**** */
INLINE void cmpd_im( void )
{
	UINT32 r,d;
	PAIR b;
	IMMWORD(b);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $1183 CMPU immediate -**** */
INLINE void cmpu_im( void )
{
	UINT32 r, d;
	PAIR b;
	IMMWORD(b);
	d = U;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $84 ANDA immediate -**0- */
INLINE void anda_im( void )
{
	UINT8 t;
	IMMBYTE(t);
	A &= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $85 BITA immediate -**0- */
INLINE void bita_im( void )
{
	UINT8 t,r;
	IMMBYTE(t);
	r = A & t;
	CLR_NZV;
	SET_NZ8(r);
}

/* $86 LDA immediate -**0- */
INLINE void lda_im( void )
{
	IMMBYTE(A);
	CLR_NZV;
	SET_NZ8(A);
}

/* is this a legal instruction? */
/* $87 STA immediate -**0- */
INLINE void sta_im( void )
{
	CLR_NZV;
	SET_NZ8(A);
	IMM8;
	WM(EAD,A);
}

/* $88 EORA immediate -**0- */
INLINE void eora_im( void )
{
	UINT8 t;
	IMMBYTE(t);
	A ^= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $89 ADCA immediate ***** */
INLINE void adca_im( void )
{
	UINT16 t,r;
	IMMBYTE(t);
	r = A + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $8A ORA immediate -**0- */
INLINE void ora_im( void )
{
	UINT8 t;
	IMMBYTE(t);
	A |= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $8B ADDA immediate ***** */
INLINE void adda_im( void )
{
	UINT16 t,r;
	IMMBYTE(t);
	r = A + t;
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $8C CMPX (CMPY CMPS) immediate -**** */
INLINE void cmpx_im( void )
{
	UINT32 r,d;
	PAIR b;
	IMMWORD(b);
	d = X;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $108C CMPY immediate -**** */
INLINE void cmpy_im( void )
{
	UINT32 r,d;
	PAIR b;
	IMMWORD(b);
	d = Y;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $118C CMPS immediate -**** */
INLINE void cmps_im( void )
{
	UINT32 r,d;
	PAIR b;
	IMMWORD(b);
	d = S;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $8D BSR ----- */
INLINE void bsr( void )
{
	UINT8 t;
	IMMBYTE(t);
	PUSHWORD(pPC);
	PC += SIGNED(t);
	CHANGE_PC;
}

/* $8E LDX (LDY) immediate -**0- */
INLINE void ldx_im( void )
{
	IMMWORD(pX);
	CLR_NZV;
	SET_NZ16(X);
}

/* $108E LDY immediate -**0- */
INLINE void ldy_im( void )
{
	IMMWORD(pY);
	CLR_NZV;
	SET_NZ16(Y);
}

/* is this a legal instruction? */
/* $8F STX (STY) immediate -**0- */
INLINE void stx_im( void )
{
	CLR_NZV;
	SET_NZ16(X);
	IMM16;
	WM16(EAD,&pX);
}

/* is this a legal instruction? */
/* $108F STY immediate -**0- */
INLINE void sty_im( void )
{
	CLR_NZV;
	SET_NZ16(Y);
	IMM16;
	WM16(EAD,&pY);
}

#if macintosh
#pragma mark ____9x____
#endif

/* $90 SUBA direct ?**** */
INLINE void suba_di( void )
{
	UINT16	  t,r;
	DIRBYTE(t);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $91 CMPA direct ?**** */
INLINE void cmpa_di( void )
{
	UINT16	  t,r;
	DIRBYTE(t);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
}

/* $92 SBCA direct ?**** */
INLINE void sbca_di( void )
{
	UINT16	  t,r;
	DIRBYTE(t);
	r = A - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $93 SUBD (CMPD CMPU) direct -**** */
INLINE void subd_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $1093 CMPD direct -**** */
INLINE void cmpd_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $1193 CMPU direct -**** */
INLINE void cmpu_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = U;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(U,b.d,r);
}

/* $94 ANDA direct -**0- */
INLINE void anda_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	A &= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $95 BITA direct -**0- */
INLINE void bita_di( void )
{
	UINT8 t,r;
	DIRBYTE(t);
	r = A & t;
	CLR_NZV;
	SET_NZ8(r);
}

/* $96 LDA direct -**0- */
INLINE void lda_di( void )
{
	DIRBYTE(A);
	CLR_NZV;
	SET_NZ8(A);
}

/* $97 STA direct -**0- */
INLINE void sta_di( void )
{
	CLR_NZV;
	SET_NZ8(A);
	DIRECT;
	WM(EAD,A);
}

/* $98 EORA direct -**0- */
INLINE void eora_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	A ^= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $99 ADCA direct ***** */
INLINE void adca_di( void )
{
	UINT16 t,r;
	DIRBYTE(t);
	r = A + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $9A ORA direct -**0- */
INLINE void ora_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	A |= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $9B ADDA direct ***** */
INLINE void adda_di( void )
{
	UINT16 t,r;
	DIRBYTE(t);
	r = A + t;
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $9C CMPX (CMPY CMPS) direct -**** */
INLINE void cmpx_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = X;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $109C CMPY direct -**** */
INLINE void cmpy_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = Y;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $119C CMPS direct -**** */
INLINE void cmps_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = S;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $9D JSR direct ----- */
INLINE void jsr_di( void )
{
	DIRECT;
	PUSHWORD(pPC);
	PCD = EAD;
	CHANGE_PC;
}

/* $9E LDX (LDY) direct -**0- */
INLINE void ldx_di( void )
{
	DIRWORD(pX);
	CLR_NZV;
	SET_NZ16(X);
}

/* $109E LDY direct -**0- */
INLINE void ldy_di( void )
{
	DIRWORD(pY);
	CLR_NZV;
	SET_NZ16(Y);
}

/* $9F STX (STY) direct -**0- */
INLINE void stx_di( void )
{
	CLR_NZV;
	SET_NZ16(X);
	DIRECT;
	WM16(EAD,&pX);
}

/* $109F STY direct -**0- */
INLINE void sty_di( void )
{
	CLR_NZV;
	SET_NZ16(Y);
	DIRECT;
	WM16(EAD,&pY);
}

#if macintosh
#pragma mark ____Ax____
#endif


/* $a0 SUBA indexed ?**** */
INLINE void suba_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $a1 CMPA indexed ?**** */
INLINE void cmpa_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
}

/* $a2 SBCA indexed ?**** */
INLINE void sbca_ix( void )
{
	UINT16	  t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = A - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $a3 SUBD (CMPD CMPU) indexed -**** */
INLINE void subd_ix( void )
{
	UINT32 r,d;
	PAIR b;
	fetch_effective_address();
    b.d=RM16(EAD);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $10a3 CMPD indexed -**** */
INLINE void cmpd_ix( void )
{
	UINT32 r,d;
	PAIR b;
	fetch_effective_address();
    b.d=RM16(EAD);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $11a3 CMPU indexed -**** */
INLINE void cmpu_ix( void )
{
	UINT32 r;
	PAIR b;
	fetch_effective_address();
    b.d=RM16(EAD);
	r = U - b.d;
	CLR_NZVC;
	SET_FLAGS16(U,b.d,r);
}

/* $a4 ANDA indexed -**0- */
INLINE void anda_ix( void )
{
	fetch_effective_address();
	A &= RM(EAD);
	CLR_NZV;
	SET_NZ8(A);
}

/* $a5 BITA indexed -**0- */
INLINE void bita_ix( void )
{
	UINT8 r;
	fetch_effective_address();
	r = A & RM(EAD);
	CLR_NZV;
	SET_NZ8(r);
}

/* $a6 LDA indexed -**0- */
INLINE void lda_ix( void )
{
	fetch_effective_address();
	A = RM(EAD);
	CLR_NZV;
	SET_NZ8(A);
}

/* $a7 STA indexed -**0- */
INLINE void sta_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ8(A);
	WM(EAD,A);
}

/* $a8 EORA indexed -**0- */
INLINE void eora_ix( void )
{
	fetch_effective_address();
	A ^= RM(EAD);
	CLR_NZV;
	SET_NZ8(A);
}

/* $a9 ADCA indexed ***** */
INLINE void adca_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = A + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $aA ORA indexed -**0- */
INLINE void ora_ix( void )
{
	fetch_effective_address();
	A |= RM(EAD);
	CLR_NZV;
	SET_NZ8(A);
}

/* $aB ADDA indexed ***** */
INLINE void adda_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = A + t;
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $aC CMPX (CMPY CMPS) indexed -**** */
INLINE void cmpx_ix( void )
{
	UINT32 r,d;
	PAIR b;
	fetch_effective_address();
    b.d=RM16(EAD);
	d = X;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $10aC CMPY indexed -**** */
INLINE void cmpy_ix( void )
{
	UINT32 r,d;
	PAIR b;
	fetch_effective_address();
    b.d=RM16(EAD);
	d = Y;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $11aC CMPS indexed -**** */
INLINE void cmps_ix( void )
{
	UINT32 r,d;
	PAIR b;
	fetch_effective_address();
    b.d=RM16(EAD);
	d = S;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $aD JSR indexed ----- */
INLINE void jsr_ix( void )
{
	fetch_effective_address();
    PUSHWORD(pPC);
	PCD = EAD;
	CHANGE_PC;
}

/* $aE LDX (LDY) indexed -**0- */
INLINE void ldx_ix( void )
{
	fetch_effective_address();
    X=RM16(EAD);
	CLR_NZV;
	SET_NZ16(X);
}

/* $10aE LDY indexed -**0- */
INLINE void ldy_ix( void )
{
	fetch_effective_address();
    Y=RM16(EAD);
	CLR_NZV;
	SET_NZ16(Y);
}

/* $aF STX (STY) indexed -**0- */
INLINE void stx_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ16(X);
	WM16(EAD,&pX);
}

/* $10aF STY indexed -**0- */
INLINE void sty_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ16(Y);
	WM16(EAD,&pY);
}

#if macintosh
#pragma mark ____Bx____
#endif

/* $b0 SUBA extended ?**** */
INLINE void suba_ex( void )
{
	UINT16	  t,r;
	EXTBYTE(t);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $b1 CMPA extended ?**** */
INLINE void cmpa_ex( void )
{
	UINT16	  t,r;
	EXTBYTE(t);
	r = A - t;
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
}

/* $b2 SBCA extended ?**** */
INLINE void sbca_ex( void )
{
	UINT16	  t,r;
	EXTBYTE(t);
	r = A - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(A,t,r);
	A = r;
}

/* $b3 SUBD (CMPD CMPU) extended -**** */
INLINE void subd_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $10b3 CMPD extended -**** */
INLINE void cmpd_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = D;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $11b3 CMPU extended -**** */
INLINE void cmpu_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = U;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $b4 ANDA extended -**0- */
INLINE void anda_ex( void )
{
	UINT8 t;
	EXTBYTE(t);
	A &= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $b5 BITA extended -**0- */
INLINE void bita_ex( void )
{
	UINT8 t,r;
	EXTBYTE(t);
	r = A & t;
	CLR_NZV; SET_NZ8(r);
}

/* $b6 LDA extended -**0- */
INLINE void lda_ex( void )
{
	EXTBYTE(A);
	CLR_NZV;
	SET_NZ8(A);
}

/* $b7 STA extended -**0- */
INLINE void sta_ex( void )
{
	CLR_NZV;
	SET_NZ8(A);
	EXTENDED;
	WM(EAD,A);
}

/* $b8 EORA extended -**0- */
INLINE void eora_ex( void )
{
	UINT8 t;
	EXTBYTE(t);
	A ^= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $b9 ADCA extended ***** */
INLINE void adca_ex( void )
{
	UINT16 t,r;
	EXTBYTE(t);
	r = A + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $bA ORA extended -**0- */
INLINE void ora_ex( void )
{
	UINT8 t;
	EXTBYTE(t);
	A |= t;
	CLR_NZV;
	SET_NZ8(A);
}

/* $bB ADDA extended ***** */
INLINE void adda_ex( void )
{
	UINT16 t,r;
	EXTBYTE(t);
	r = A + t;
	CLR_HNZVC;
	SET_FLAGS8(A,t,r);
	SET_H(A,t,r);
	A = r;
}

/* $bC CMPX (CMPY CMPS) extended -**** */
INLINE void cmpx_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = X;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $10bC CMPY extended -**** */
INLINE void cmpy_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = Y;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $11bC CMPS extended -**** */
INLINE void cmps_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = S;
	r = d - b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
}

/* $bD JSR extended ----- */
INLINE void jsr_ex( void )
{
	EXTENDED;
	PUSHWORD(pPC);
	PCD = EAD;
	CHANGE_PC;
}

/* $bE LDX (LDY) extended -**0- */
INLINE void ldx_ex( void )
{
	EXTWORD(pX);
	CLR_NZV;
	SET_NZ16(X);
}

/* $10bE LDY extended -**0- */
INLINE void ldy_ex( void )
{
	EXTWORD(pY);
	CLR_NZV;
	SET_NZ16(Y);
}

/* $bF STX (STY) extended -**0- */
INLINE void stx_ex( void )
{
	CLR_NZV;
	SET_NZ16(X);
	EXTENDED;
	WM16(EAD,&pX);
}

/* $10bF STY extended -**0- */
INLINE void sty_ex( void )
{
	CLR_NZV;
	SET_NZ16(Y);
	EXTENDED;
	WM16(EAD,&pY);
}


#if macintosh
#pragma mark ____Cx____
#endif

/* $c0 SUBB immediate ?**** */
INLINE void subb_im( void )
{
	UINT16	  t,r;
	IMMBYTE(t);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $c1 CMPB immediate ?**** */
INLINE void cmpb_im( void )
{
	UINT16	  t,r;
	IMMBYTE(t);
	r = B - t;
	CLR_NZVC; SET_FLAGS8(B,t,r);
}

/* $c2 SBCB immediate ?**** */
INLINE void sbcb_im( void )
{
	UINT16	  t,r;
	IMMBYTE(t);
	r = B - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $c3 ADDD immediate -**** */
INLINE void addd_im( void )
{
	UINT32 r,d;
	PAIR b;
	IMMWORD(b);
	d = D;
	r = d + b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $c4 ANDB immediate -**0- */
INLINE void andb_im( void )
{
	UINT8 t;
	IMMBYTE(t);
	B &= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $c5 BITB immediate -**0- */
INLINE void bitb_im( void )
{
	UINT8 t,r;
	IMMBYTE(t);
	r = B & t;
	CLR_NZV;
	SET_NZ8(r);
}

/* $c6 LDB immediate -**0- */
INLINE void ldb_im( void )
{
	IMMBYTE(B);
	CLR_NZV;
	SET_NZ8(B);
}

/* is this a legal instruction? */
/* $c7 STB immediate -**0- */
INLINE void stb_im( void )
{
	CLR_NZV;
	SET_NZ8(B);
	IMM8;
	WM(EAD,B);
}

/* $c8 EORB immediate -**0- */
INLINE void eorb_im( void )
{
	UINT8 t;
	IMMBYTE(t);
	B ^= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $c9 ADCB immediate ***** */
INLINE void adcb_im( void )
{
	UINT16 t,r;
	IMMBYTE(t);
	r = B + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $cA ORB immediate -**0- */
INLINE void orb_im( void )
{
	UINT8 t;
	IMMBYTE(t);
	B |= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $cB ADDB immediate ***** */
INLINE void addb_im( void )
{
	UINT16 t,r;
	IMMBYTE(t);
	r = B + t;
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $cC LDD immediate -**0- */
INLINE void ldd_im( void )
{
	IMMWORD(pD);
	CLR_NZV;
	SET_NZ16(D);
}

/* is this a legal instruction? */
/* $cD STD immediate -**0- */
INLINE void std_im( void )
{
	CLR_NZV;
	SET_NZ16(D);
    IMM16;
	WM16(EAD,&pD);
}

/* $cE LDU (LDS) immediate -**0- */
INLINE void ldu_im( void )
{
	IMMWORD(pU);
	CLR_NZV;
	SET_NZ16(U);
}

/* $10cE LDS immediate -**0- */
INLINE void lds_im( void )
{
	IMMWORD(pS);
	CLR_NZV;
	SET_NZ16(S);
	m6809.int_state |= M6809_LDS;
}

/* is this a legal instruction? */
/* $cF STU (STS) immediate -**0- */
INLINE void stu_im( void )
{
	CLR_NZV;
	SET_NZ16(U);
    IMM16;
	WM16(EAD,&pU);
}

/* is this a legal instruction? */
/* $10cF STS immediate -**0- */
INLINE void sts_im( void )
{
	CLR_NZV;
	SET_NZ16(S);
    IMM16;
	WM16(EAD,&pS);
}


#if macintosh
#pragma mark ____Dx____
#endif

/* $d0 SUBB direct ?**** */
INLINE void subb_di( void )
{
	UINT16	  t,r;
	DIRBYTE(t);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $d1 CMPB direct ?**** */
INLINE void cmpb_di( void )
{
	UINT16	  t,r;
	DIRBYTE(t);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
}

/* $d2 SBCB direct ?**** */
INLINE void sbcb_di( void )
{
	UINT16	  t,r;
	DIRBYTE(t);
	r = B - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $d3 ADDD direct -**** */
INLINE void addd_di( void )
{
	UINT32 r,d;
	PAIR b;
	DIRWORD(b);
	d = D;
	r = d + b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $d4 ANDB direct -**0- */
INLINE void andb_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	B &= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $d5 BITB direct -**0- */
INLINE void bitb_di( void )
{
	UINT8 t,r;
	DIRBYTE(t);
	r = B & t;
	CLR_NZV;
	SET_NZ8(r);
}

/* $d6 LDB direct -**0- */
INLINE void ldb_di( void )
{
	DIRBYTE(B);
	CLR_NZV;
	SET_NZ8(B);
}

/* $d7 STB direct -**0- */
INLINE void stb_di( void )
{
	CLR_NZV;
	SET_NZ8(B);
	DIRECT;
	WM(EAD,B);
}

/* $d8 EORB direct -**0- */
INLINE void eorb_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	B ^= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $d9 ADCB direct ***** */
INLINE void adcb_di( void )
{
	UINT16 t,r;
	DIRBYTE(t);
	r = B + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $dA ORB direct -**0- */
INLINE void orb_di( void )
{
	UINT8 t;
	DIRBYTE(t);
	B |= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $dB ADDB direct ***** */
INLINE void addb_di( void )
{
	UINT16 t,r;
	DIRBYTE(t);
	r = B + t;
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $dC LDD direct -**0- */
INLINE void ldd_di( void )
{
	DIRWORD(pD);
	CLR_NZV;
	SET_NZ16(D);
}

/* $dD STD direct -**0- */
INLINE void std_di( void )
{
	CLR_NZV;
	SET_NZ16(D);
    DIRECT;
	WM16(EAD,&pD);
}

/* $dE LDU (LDS) direct -**0- */
INLINE void ldu_di( void )
{
	DIRWORD(pU);
	CLR_NZV;
	SET_NZ16(U);
}

/* $10dE LDS direct -**0- */
INLINE void lds_di( void )
{
	DIRWORD(pS);
	CLR_NZV;
	SET_NZ16(S);
	m6809.int_state |= M6809_LDS;
}

/* $dF STU (STS) direct -**0- */
INLINE void stu_di( void )
{
	CLR_NZV;
	SET_NZ16(U);
	DIRECT;
	WM16(EAD,&pU);
}

/* $10dF STS direct -**0- */
INLINE void sts_di( void )
{
	CLR_NZV;
	SET_NZ16(S);
	DIRECT;
	WM16(EAD,&pS);
}

#if macintosh
#pragma mark ____Ex____
#endif


/* $e0 SUBB indexed ?**** */
INLINE void subb_ix( void )
{
	UINT16	  t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $e1 CMPB indexed ?**** */
INLINE void cmpb_ix( void )
{
	UINT16	  t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
}

/* $e2 SBCB indexed ?**** */
INLINE void sbcb_ix( void )
{
	UINT16	  t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = B - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $e3 ADDD indexed -**** */
INLINE void addd_ix( void )
{
	UINT32 r,d;
    PAIR b;
    fetch_effective_address();
	b.d=RM16(EAD);
	d = D;
	r = d + b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $e4 ANDB indexed -**0- */
INLINE void andb_ix( void )
{
	fetch_effective_address();
	B &= RM(EAD);
	CLR_NZV;
	SET_NZ8(B);
}

/* $e5 BITB indexed -**0- */
INLINE void bitb_ix( void )
{
	UINT8 r;
	fetch_effective_address();
	r = B & RM(EAD);
	CLR_NZV;
	SET_NZ8(r);
}

/* $e6 LDB indexed -**0- */
INLINE void ldb_ix( void )
{
	fetch_effective_address();
	B = RM(EAD);
	CLR_NZV;
	SET_NZ8(B);
}

/* $e7 STB indexed -**0- */
INLINE void stb_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ8(B);
	WM(EAD,B);
}

/* $e8 EORB indexed -**0- */
INLINE void eorb_ix( void )
{
	fetch_effective_address();
	B ^= RM(EAD);
	CLR_NZV;
	SET_NZ8(B);
}

/* $e9 ADCB indexed ***** */
INLINE void adcb_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = B + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $eA ORB indexed -**0- */
INLINE void orb_ix( void )
{
	fetch_effective_address();
	B |= RM(EAD);
	CLR_NZV;
	SET_NZ8(B);
}

/* $eB ADDB indexed ***** */
INLINE void addb_ix( void )
{
	UINT16 t,r;
	fetch_effective_address();
	t = RM(EAD);
	r = B + t;
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $eC LDD indexed -**0- */
INLINE void ldd_ix( void )
{
	fetch_effective_address();
    D=RM16(EAD);
	CLR_NZV; SET_NZ16(D);
}

/* $eD STD indexed -**0- */
INLINE void std_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ16(D);
	WM16(EAD,&pD);
}

/* $eE LDU (LDS) indexed -**0- */
INLINE void ldu_ix( void )
{
	fetch_effective_address();
    U=RM16(EAD);
	CLR_NZV;
	SET_NZ16(U);
}

/* $10eE LDS indexed -**0- */
INLINE void lds_ix( void )
{
	fetch_effective_address();
    S=RM16(EAD);
	CLR_NZV;
	SET_NZ16(S);
	m6809.int_state |= M6809_LDS;
}

/* $eF STU (STS) indexed -**0- */
INLINE void stu_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ16(U);
	WM16(EAD,&pU);
}

/* $10eF STS indexed -**0- */
INLINE void sts_ix( void )
{
	fetch_effective_address();
    CLR_NZV;
	SET_NZ16(S);
	WM16(EAD,&pS);
}

#if macintosh
#pragma mark ____Fx____
#endif

/* $f0 SUBB extended ?**** */
INLINE void subb_ex( void )
{
	UINT16	  t,r;
	EXTBYTE(t);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $f1 CMPB extended ?**** */
INLINE void cmpb_ex( void )
{
	UINT16	  t,r;
	EXTBYTE(t);
	r = B - t;
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
}

/* $f2 SBCB extended ?**** */
INLINE void sbcb_ex( void )
{
	UINT16	  t,r;
	EXTBYTE(t);
	r = B - t - (CC & CC_C);
	CLR_NZVC;
	SET_FLAGS8(B,t,r);
	B = r;
}

/* $f3 ADDD extended -**** */
INLINE void addd_ex( void )
{
	UINT32 r,d;
	PAIR b;
	EXTWORD(b);
	d = D;
	r = d + b.d;
	CLR_NZVC;
	SET_FLAGS16(d,b.d,r);
	D = r;
}

/* $f4 ANDB extended -**0- */
INLINE void andb_ex( void )
{
	UINT8 t;
	EXTBYTE(t);
	B &= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $f5 BITB extended -**0- */
INLINE void bitb_ex( void )
{
	UINT8 t,r;
	EXTBYTE(t);
	r = B & t;
	CLR_NZV;
	SET_NZ8(r);
}

/* $f6 LDB extended -**0- */
INLINE void ldb_ex( void )
{
	EXTBYTE(B);
	CLR_NZV;
	SET_NZ8(B);
}

/* $f7 STB extended -**0- */
INLINE void stb_ex( void )
{
	CLR_NZV;
	SET_NZ8(B);
	EXTENDED;
	WM(EAD,B);
}

/* $f8 EORB extended -**0- */
INLINE void eorb_ex( void )
{
	UINT8 t;
	EXTBYTE(t);
	B ^= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $f9 ADCB extended ***** */
INLINE void adcb_ex( void )
{
	UINT16 t,r;
	EXTBYTE(t);
	r = B + t + (CC & CC_C);
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $fA ORB extended -**0- */
INLINE void orb_ex( void )
{
	UINT8 t;
	EXTBYTE(t);
	B |= t;
	CLR_NZV;
	SET_NZ8(B);
}

/* $fB ADDB extended ***** */
INLINE void addb_ex( void )
{
	UINT16 t,r;
	EXTBYTE(t);
	r = B + t;
	CLR_HNZVC;
	SET_FLAGS8(B,t,r);
	SET_H(B,t,r);
	B = r;
}

/* $fC LDD extended -**0- */
INLINE void ldd_ex( void )
{
	EXTWORD(pD);
	CLR_NZV;
	SET_NZ16(D);
}

/* $fD STD extended -**0- */
INLINE void std_ex( void )
{
	CLR_NZV;
	SET_NZ16(D);
    EXTENDED;
	WM16(EAD,&pD);
}

/* $fE LDU (LDS) extended -**0- */
INLINE void ldu_ex( void )
{
	EXTWORD(pU);
	CLR_NZV;
	SET_NZ16(U);
}

/* $10fE LDS extended -**0- */
INLINE void lds_ex( void )
{
	EXTWORD(pS);
	CLR_NZV;
	SET_NZ16(S);
	m6809.int_state |= M6809_LDS;
}

/* $fF STU (STS) extended -**0- */
INLINE void stu_ex( void )
{
	CLR_NZV;
	SET_NZ16(U);
	EXTENDED;
	WM16(EAD,&pU);
}

/* $10fF STS extended -**0- */
INLINE void sts_ex( void )
{
	CLR_NZV;
	SET_NZ16(S);
	EXTENDED;
	WM16(EAD,&pS);
}

/* $10xx opcodes */
INLINE void pref10( void )
{
	UINT8 ireg2 = ROP(PCD);
	PC++;
	switch( ireg2 )
	{
		case 0x21: lbrn();		m6809_ICount-=5;	break;
		case 0x22: lbhi();		m6809_ICount-=5;	break;
		case 0x23: lbls();		m6809_ICount-=5;	break;
		case 0x24: lbcc();		m6809_ICount-=5;	break;
		case 0x25: lbcs();		m6809_ICount-=5;	break;
		case 0x26: lbne();		m6809_ICount-=5;	break;
		case 0x27: lbeq();		m6809_ICount-=5;	break;
		case 0x28: lbvc();		m6809_ICount-=5;	break;
		case 0x29: lbvs();		m6809_ICount-=5;	break;
		case 0x2a: lbpl();		m6809_ICount-=5;	break;
		case 0x2b: lbmi();		m6809_ICount-=5;	break;
		case 0x2c: lbge();		m6809_ICount-=5;	break;
		case 0x2d: lblt();		m6809_ICount-=5;	break;
		case 0x2e: lbgt();		m6809_ICount-=5;	break;
		case 0x2f: lble();		m6809_ICount-=5;	break;

		case 0x3f: swi2();		m6809_ICount-=20;	break;

		case 0x83: cmpd_im();	m6809_ICount-=5;	break;
		case 0x8c: cmpy_im();	m6809_ICount-=5;	break;
		case 0x8e: ldy_im();	m6809_ICount-=4;	break;
		case 0x8f: sty_im();	m6809_ICount-=4;	break;

		case 0x93: cmpd_di();	m6809_ICount-=7;	break;
		case 0x9c: cmpy_di();	m6809_ICount-=7;	break;
		case 0x9e: ldy_di();	m6809_ICount-=6;	break;
		case 0x9f: sty_di();	m6809_ICount-=6;	break;

		case 0xa3: cmpd_ix();	m6809_ICount-=7;	break;
		case 0xac: cmpy_ix();	m6809_ICount-=7;	break;
		case 0xae: ldy_ix();	m6809_ICount-=6;	break;
		case 0xaf: sty_ix();	m6809_ICount-=6;	break;

		case 0xb3: cmpd_ex();	m6809_ICount-=8;	break;
		case 0xbc: cmpy_ex();	m6809_ICount-=8;	break;
		case 0xbe: ldy_ex();	m6809_ICount-=7;	break;
		case 0xbf: sty_ex();	m6809_ICount-=7;	break;

		case 0xce: lds_im();	m6809_ICount-=4;	break;
		case 0xcf: sts_im();	m6809_ICount-=4;	break;

		case 0xde: lds_di();	m6809_ICount-=4;	break;
		case 0xdf: sts_di();	m6809_ICount-=4;	break;

		case 0xee: lds_ix();	m6809_ICount-=6;	break;
		case 0xef: sts_ix();	m6809_ICount-=6;	break;

		case 0xfe: lds_ex();	m6809_ICount-=7;	break;
		case 0xff: sts_ex();	m6809_ICount-=7;	break;

		default:   illegal();						break;
	}
}

/* $11xx opcodes */
INLINE void pref11( void )
{
	UINT8 ireg2 = ROP(PCD);
	PC++;
	switch( ireg2 )
	{
		case 0x3f: swi3();		m6809_ICount-=20;	break;

		case 0x83: cmpu_im();	m6809_ICount-=5;	break;
		case 0x8c: cmps_im();	m6809_ICount-=5;	break;

		case 0x93: cmpu_di();	m6809_ICount-=7;	break;
		case 0x9c: cmps_di();	m6809_ICount-=7;	break;

		case 0xa3: cmpu_ix();	m6809_ICount-=7;	break;
		case 0xac: cmps_ix();	m6809_ICount-=7;	break;

		case 0xb3: cmpu_ex();	m6809_ICount-=8;	break;
		case 0xbc: cmps_ex();	m6809_ICount-=8;	break;

		default:   illegal();						break;
	}
}


