/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.konami;

//cpu imports
import static arcadeflex.v036.cpu.konami.konami.*;
import static arcadeflex.v036.cpu.konami.konamtbl.*;
//memory imports
import static arcadeflex.v036.mame.memoryH.*;

public class konamops {

    /*TODO*///static void illegal( void )
/*TODO*///#else
/*TODO*///INLINE void illegal( void )
/*TODO*///#endif
/*TODO*///{
/*TODO*///	logerror("KONAMI: illegal opcode at %04x\n",PC);
/*TODO*///}
/*TODO*///
/*TODO*///#if macintosh
/*TODO*///#pragma mark ____0x____
/*TODO*///#endif
/*TODO*///
    public static opcode neg_di = new opcode() {
        public void handler() {
            int r, t;
            t = DIRBYTE() & 0xFFFF;
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r);
        }
    };

    static opcode com_di = new opcode() {
        public void handler() {
            /*UINT8*/
            int t = DIRBYTE() & 0xFF;
            t = (~t) & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    public static opcode lsr_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            CLR_NZC();
            konami.cc |= t & CC_C;
            t = (t >>> 1) & 0xFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    /*TODO*////* $06 ROR direct -**-* */
/*TODO*///INLINE void ror_di( void )
/*TODO*///{
/*TODO*///	UINT8 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r= (CC & CC_C) << 7;
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t & CC_C);
/*TODO*///	r |= t>>1;
/*TODO*///	SET_NZ8(r);
/*TODO*///	WM(EAD,r);
/*TODO*///}
/*TODO*///
    public static opcode asr_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = ((t & 0x80) | (t >>> 1)) & 0xFF;
            SET_NZ8(t);
            WM(ea, t);
        }
    };

    public static opcode asl_di = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode rol_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = konami.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode dec_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            t = (t - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);
        }
    };

    public static opcode inc_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            t = (t + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(t);
            WM(ea, t);
        }
    };

    public static opcode tst_di = new opcode() {
        public void handler() {
            /*UINT8*/
            int t = DIRBYTE() & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
        }
    };
    /*TODO*///
/*TODO*////* $0E JMP direct ----- */
/*TODO*///INLINE void jmp_di( void )
/*TODO*///{
/*TODO*///    DIRECT;
/*TODO*///	PCD=EAD;
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
    public static opcode clr_di = new opcode() {
        public void handler() {
            DIRECT();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode nop = new opcode() {
        public void handler() {
        }
    };
    /*TODO*///
/*TODO*////* $13 SYNC inherent ----- */
/*TODO*///INLINE void sync( void )
/*TODO*///{
/*TODO*///	/* SYNC stops processing instructions until an interrupt request happens. */
/*TODO*///	/* This doesn't require the corresponding interrupt to be enabled: if it */
/*TODO*///	/* is disabled, execution continues with the next instruction. */
/*TODO*///	konami.int_state |= KONAMI_SYNC;
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///	/* if KONAMI_SYNC has not been cleared by CHECK_IRQ_LINES,
/*TODO*///	 * stop execution until the interrupt lines change. */
/*TODO*///	if( (konami.int_state & KONAMI_SYNC) && konami_ICount > 0 )
/*TODO*///		konami_ICount = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* $14 ILLEGAL */
/*TODO*///
/*TODO*////* $15 ILLEGAL */
/*TODO*///
/*TODO*////* $16 LBRA relative ----- */
/*TODO*///INLINE void lbra( void )
/*TODO*///{
/*TODO*///	IMMWORD(ea);
/*TODO*///	PC += EA;
/*TODO*///	change_pc(PCD);
/*TODO*///
/*TODO*///	/* EHC 980508 speed up busy loop */
/*TODO*///	if( EA == 0xfffd && konami_ICount > 0 )
/*TODO*///		konami_ICount = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* $17 LBSR relative ----- */
/*TODO*///INLINE void lbsr( void )
/*TODO*///{
/*TODO*///	IMMWORD(ea);
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PC += EA;
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
/*TODO*////* $18 ILLEGAL */
/*TODO*///
/*TODO*///#if 1
/*TODO*////* $19 DAA inherent (A) -**0* */
/*TODO*///INLINE void daa( void )
/*TODO*///{
/*TODO*///	UINT8 msn, lsn;
/*TODO*///	UINT16 t, cf = 0;
/*TODO*///	msn = A & 0xf0; lsn = A & 0x0f;
/*TODO*///	if( lsn>0x09 || CC & CC_H) cf |= 0x06;
/*TODO*///	if( msn>0x80 && lsn>0x09 ) cf |= 0x60;
/*TODO*///	if( msn>0x90 || CC & CC_C) cf |= 0x60;
/*TODO*///	t = cf + A;
/*TODO*///	CLR_NZV; /* keep carry from previous operation */
/*TODO*///	SET_NZ8((UINT8)t); SET_C8(t);
/*TODO*///	A = t;
/*TODO*///}
/*TODO*///#else
/*TODO*////* $19 DAA inherent (A) -**0* */
/*TODO*///INLINE void daa( void )
/*TODO*///{
/*TODO*///	UINT16 t;
/*TODO*///	t = A;
/*TODO*///	if (CC & CC_H) t+=0x06;
/*TODO*///	if ((t&0x0f)>9) t+=0x06;		/* ASG -- this code is broken! $66+$99=$FF -> DAA should = $65, we get $05! */
/*TODO*///	if (CC & CC_C) t+=0x60;
/*TODO*///	if ((t&0xf0)>0x90) t+=0x60;
/*TODO*///	if (t&0x100) SEC;
/*TODO*///	A = t;
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*////* $1A ORCC immediate ##### */
/*TODO*///INLINE void orcc( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	CC |= t;
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///}
/*TODO*///
/*TODO*////* $1B ILLEGAL */
/*TODO*///
/*TODO*////* $1C ANDCC immediate ##### */
/*TODO*///INLINE void andcc( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	CC &= t;
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///}
/*TODO*///
/*TODO*////* $1D SEX inherent -**0- */
/*TODO*///INLINE void sex( void )
/*TODO*///{
/*TODO*///	UINT16 t;
/*TODO*///	t = SIGNED(B);
/*TODO*///	D = t;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(t);
/*TODO*///}
/*TODO*///
/*TODO*////* $1E EXG inherent ----- */
/*TODO*///INLINE void exg( void )
/*TODO*///{
/*TODO*///	UINT16 t1 = 0, t2 = 0;
/*TODO*///	UINT8 tb;
/*TODO*///
/*TODO*///	IMMBYTE(tb);
/*TODO*///
/*TODO*///	GETREG( t1, tb >> 4 );
/*TODO*///	GETREG( t2, tb & 0x0f );
/*TODO*///
/*TODO*///	SETREG( t2, tb >> 4 );
/*TODO*///	SETREG( t1, tb & 0x0f );
/*TODO*///}
/*TODO*///
/*TODO*////* $1F TFR inherent ----- */
/*TODO*///INLINE void tfr( void )
/*TODO*///{
/*TODO*///	UINT8 tb;
/*TODO*///	UINT16 t = 0;
/*TODO*///
/*TODO*///	IMMBYTE(tb);
/*TODO*///
/*TODO*///	GETREG( t, tb & 0x0f );
/*TODO*///	SETREG( t, ( tb >> 4 ) & 0x07 );
/*TODO*///}
/*TODO*///
/*TODO*///#if macintosh
/*TODO*///#pragma mark ____2x____
/*TODO*///#endif
/*TODO*///
/*TODO*////* $20 BRA relative ----- */
/*TODO*///INLINE void bra( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	PC += SIGNED(t);
/*TODO*///	change_pc(PCD);
/*TODO*///	/* JB 970823 - speed up busy loops */
/*TODO*///	if( t == 0xfe && konami_ICount > 0 )
/*TODO*///		konami_ICount = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* $21 BRN relative ----- */
/*TODO*///INLINE void brn( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///}
/*TODO*///
/*TODO*////* $1021 LBRN relative ----- */
/*TODO*///INLINE void lbrn( void )
/*TODO*///{
/*TODO*///	IMMWORD(ea);
/*TODO*///}
/*TODO*///
    public static opcode bhi = new opcode() {
        public void handler() {
            BRANCH((konami.cc & (CC_Z | CC_C)) == 0);
        }
    };

    public static opcode lbhi = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & (CC_Z | CC_C)) == 0);
        }
    };

    public static opcode bls = new opcode() {
        public void handler() {
            BRANCH((konami.cc & (CC_Z | CC_C)) != 0);
        }
    };

    public static opcode lbls = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & (CC_Z | CC_C)) != 0);
        }
    };

    public static opcode bcc = new opcode() {
        public void handler() {
            BRANCH((konami.cc & CC_C) == 0);
        }
    };

    public static opcode lbcc = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & CC_C) == 0);
        }
    };

    public static opcode bcs = new opcode() {
        public void handler() {
            BRANCH((konami.cc & CC_C) != 0);
        }
    };

    public static opcode lbcs = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & CC_C) != 0);
        }
    };

    public static opcode bne = new opcode() {
        public void handler() {
            BRANCH((konami.cc & CC_Z) == 0);
        }
    };

    public static opcode lbne = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & CC_Z) == 0);
        }
    };

    public static opcode beq = new opcode() {
        public void handler() {
            BRANCH((konami.cc & CC_Z) != 0);
        }
    };

    public static opcode lbeq = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & CC_Z) != 0);
        }
    };

    /*TODO*////* $28 BVC relative ----- */
/*TODO*///INLINE void bvc( void )
/*TODO*///{
/*TODO*///	BRANCH( !(CC&CC_V) );
/*TODO*///}
/*TODO*///
/*TODO*////* $1028 LBVC relative ----- */
/*TODO*///INLINE void lbvc( void )
/*TODO*///{
/*TODO*///	LBRANCH( !(CC&CC_V) );
/*TODO*///}
/*TODO*///
/*TODO*////* $29 BVS relative ----- */
/*TODO*///INLINE void bvs( void )
/*TODO*///{
/*TODO*///	BRANCH( (CC&CC_V) );
/*TODO*///}
/*TODO*///
/*TODO*////* $1029 LBVS relative ----- */
/*TODO*///INLINE void lbvs( void )
/*TODO*///{
/*TODO*///	LBRANCH( (CC&CC_V) );
/*TODO*///}
/*TODO*///
    public static opcode bpl = new opcode() {
        public void handler() {
            BRANCH((konami.cc & CC_N) == 0);
        }
    };

    public static opcode lbpl = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & CC_N) == 0);
        }
    };

    public static opcode bmi = new opcode() {
        public void handler() {
            BRANCH((konami.cc & CC_N) != 0);
        }
    };

    public static opcode lbmi = new opcode() {
        public void handler() {
            LBRANCH((konami.cc & CC_N) != 0);
        }
    };

    public static opcode bge = new opcode() {
        public void handler() {
            BRANCH(NXORV() == 0);
        }
    };

    public static opcode lbge = new opcode() {
        public void handler() {
            LBRANCH(NXORV() == 0);
        }
    };

    public static opcode blt = new opcode() {
        public void handler() {
            BRANCH(NXORV() != 0);
        }
    };

    public static opcode lblt = new opcode() {
        public void handler() {
            LBRANCH(NXORV() != 0);
        }
    };

    public static opcode bgt = new opcode() {
        public void handler() {
            BRANCH(!((NXORV() != 0) || ((konami.cc & CC_Z) != 0)));
        }
    };

    public static opcode lbgt = new opcode() {
        public void handler() {
            LBRANCH(!((NXORV() != 0) || ((konami.cc & CC_Z) != 0)));
        }
    };

    public static opcode ble = new opcode() {
        public void handler() {
            BRANCH((NXORV() != 0 || (konami.cc & CC_Z) != 0));
        }
    };

    public static opcode lble = new opcode() {
        public void handler() {
            LBRANCH((NXORV() != 0 || (konami.cc & CC_Z) != 0));
        }
    };

    public static opcode leax = new opcode() {
        public void handler() {
            konami.x = ea & 0xFFFF;
            CLR_Z();
            SET_Z(konami.x);
        }
    };

    public static opcode leay = new opcode() {
        public void handler() {
            konami.y = ea & 0xFFFF;
            CLR_Z();
            SET_Z(konami.y);
        }
    };

    public static opcode leas = new opcode() {
        public void handler() {
            konami.s = ea & 0xFFFF;
            konami.int_state |= KONAMI_LDS;
        }
    };

    public static opcode leau = new opcode() {
        public void handler() {
            konami.u = ea & 0xFFFF;
        }
    };

    /*TODO*////* $34 PSHS inherent ----- */
/*TODO*///INLINE void pshs( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x80 ) { PUSHWORD(pPC); konami_ICount -= 2; }
/*TODO*///	if( t&0x40 ) { PUSHWORD(pU);  konami_ICount -= 2; }
/*TODO*///	if( t&0x20 ) { PUSHWORD(pY);  konami_ICount -= 2; }
/*TODO*///	if( t&0x10 ) { PUSHWORD(pX);  konami_ICount -= 2; }
/*TODO*///	if( t&0x08 ) { PUSHBYTE(DP);  konami_ICount -= 1; }
/*TODO*///	if( t&0x04 ) { PUSHBYTE(B);   konami_ICount -= 1; }
/*TODO*///	if( t&0x02 ) { PUSHBYTE(A);   konami_ICount -= 1; }
/*TODO*///	if( t&0x01 ) { PUSHBYTE(CC);  konami_ICount -= 1; }
/*TODO*///}
/*TODO*///
/*TODO*////* 35 PULS inherent ----- */
/*TODO*///INLINE void puls( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x01 ) { PULLBYTE(CC); konami_ICount -= 1; }
/*TODO*///	if( t&0x02 ) { PULLBYTE(A);  konami_ICount -= 1; }
/*TODO*///	if( t&0x04 ) { PULLBYTE(B);  konami_ICount -= 1; }
/*TODO*///	if( t&0x08 ) { PULLBYTE(DP); konami_ICount -= 1; }
/*TODO*///	if( t&0x10 ) { PULLWORD(XD); konami_ICount -= 2; }
/*TODO*///	if( t&0x20 ) { PULLWORD(YD); konami_ICount -= 2; }
/*TODO*///	if( t&0x40 ) { PULLWORD(UD); konami_ICount -= 2; }
/*TODO*///	if( t&0x80 ) { PULLWORD(PCD); change_pc(PCD); konami_ICount -= 2; }
/*TODO*///
/*TODO*///	/* check after all PULLs */
/*TODO*///	if( t&0x01 ) { CHECK_IRQ_LINES; }
/*TODO*///}
/*TODO*///
/*TODO*////* $36 PSHU inherent ----- */
/*TODO*///INLINE void pshu( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x80 ) { PSHUWORD(pPC); konami_ICount -= 2; }
/*TODO*///	if( t&0x40 ) { PSHUWORD(pS);  konami_ICount -= 2; }
/*TODO*///	if( t&0x20 ) { PSHUWORD(pY);  konami_ICount -= 2; }
/*TODO*///	if( t&0x10 ) { PSHUWORD(pX);  konami_ICount -= 2; }
/*TODO*///	if( t&0x08 ) { PSHUBYTE(DP);  konami_ICount -= 1; }
/*TODO*///	if( t&0x04 ) { PSHUBYTE(B);   konami_ICount -= 1; }
/*TODO*///	if( t&0x02 ) { PSHUBYTE(A);   konami_ICount -= 1; }
/*TODO*///	if( t&0x01 ) { PSHUBYTE(CC);  konami_ICount -= 1; }
/*TODO*///}
/*TODO*///
/*TODO*////* 37 PULU inherent ----- */
/*TODO*///INLINE void pulu( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	if( t&0x01 ) { PULUBYTE(CC); konami_ICount -= 1; }
/*TODO*///	if( t&0x02 ) { PULUBYTE(A);  konami_ICount -= 1; }
/*TODO*///	if( t&0x04 ) { PULUBYTE(B);  konami_ICount -= 1; }
/*TODO*///	if( t&0x08 ) { PULUBYTE(DP); konami_ICount -= 1; }
/*TODO*///	if( t&0x10 ) { PULUWORD(XD); konami_ICount -= 2; }
/*TODO*///	if( t&0x20 ) { PULUWORD(YD); konami_ICount -= 2; }
/*TODO*///	if( t&0x40 ) { PULUWORD(SD); konami_ICount -= 2; }
/*TODO*///	if( t&0x80 ) { PULUWORD(PCD); change_pc(PCD); konami_ICount -= 2; }
/*TODO*///
/*TODO*///	/* check after all PULLs */
/*TODO*///	if( t&0x01 ) { CHECK_IRQ_LINES; }
/*TODO*///}
/*TODO*///
/*TODO*////* $38 ILLEGAL */
/*TODO*///
/*TODO*////* $39 RTS inherent ----- */
/*TODO*///INLINE void rts( void )
/*TODO*///{
/*TODO*///	PULLWORD(PCD);
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
    public static opcode abx = new opcode() {
        public void handler() {
            konami.x = (konami.x + konami.b) & 0xFFFF;
        }
    };
    /*TODO*////* $3B RTI inherent ##### */
/*TODO*///INLINE void rti( void )
/*TODO*///{
/*TODO*///	PULLBYTE(CC);
/*TODO*///	if( CC & CC_E ) /* entire state saved? */
/*TODO*///	{
/*TODO*///        konami_ICount -= 9;
/*TODO*///		PULLBYTE(A);
/*TODO*///		PULLBYTE(B);
/*TODO*///		PULLBYTE(DP);
/*TODO*///		PULLWORD(XD);
/*TODO*///		PULLWORD(YD);
/*TODO*///		PULLWORD(UD);
/*TODO*///	}
/*TODO*///	PULLWORD(PCD);
/*TODO*///	change_pc(PCD);
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///}
/*TODO*///
/*TODO*////* $3C CWAI inherent ----1 */
/*TODO*///INLINE void cwai( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	CC &= t;
/*TODO*///	/*
/*TODO*///     * CWAI stacks the entire machine state on the hardware stack,
/*TODO*///     * then waits for an interrupt; when the interrupt is taken
/*TODO*///     * later, the state is *not* saved again after CWAI.
/*TODO*///     */
/*TODO*///	CC |= CC_E; 		/* HJB 990225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///	PUSHBYTE(CC);
/*TODO*///	konami.int_state |= KONAMI_CWAI;
/*TODO*///	CHECK_IRQ_LINES;
/*TODO*///	if( (konami.int_state & KONAMI_CWAI) && konami_ICount > 0 )
/*TODO*///		konami_ICount = 0;
/*TODO*///}
/*TODO*///
/*TODO*////* $3D MUL inherent --*-@ */
/*TODO*///INLINE void mul( void )
/*TODO*///{
/*TODO*///	UINT16 t;
/*TODO*///	t = A * B;
/*TODO*///	CLR_ZC; SET_Z16(t); if(t&0x80) SEC;
/*TODO*///	D = t;
/*TODO*///}
/*TODO*///
/*TODO*////* $3E ILLEGAL */
/*TODO*///
/*TODO*////* $3F SWI (SWI2 SWI3) absolute indirect ----- */
/*TODO*///INLINE void swi( void )
/*TODO*///{
/*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///	PUSHBYTE(CC);
/*TODO*///	CC |= CC_IF | CC_II;	/* inhibit FIRQ and IRQ */
/*TODO*///	PCD=RM16(0xfffa);
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
/*TODO*////* $103F SWI2 absolute indirect ----- */
/*TODO*///INLINE void swi2( void )
/*TODO*///{
/*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///    PUSHBYTE(CC);
/*TODO*///	PCD=RM16(0xfff4);
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
/*TODO*////* $113F SWI3 absolute indirect ----- */
/*TODO*///INLINE void swi3( void )
/*TODO*///{
/*TODO*///	CC |= CC_E; 			/* HJB 980225: save entire state */
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PUSHWORD(pU);
/*TODO*///	PUSHWORD(pY);
/*TODO*///	PUSHWORD(pX);
/*TODO*///	PUSHBYTE(DP);
/*TODO*///	PUSHBYTE(B);
/*TODO*///	PUSHBYTE(A);
/*TODO*///    PUSHBYTE(CC);
/*TODO*///	PCD=RM16(0xfff2);
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
/*TODO*///#if macintosh
/*TODO*///#pragma mark ____4x____
/*TODO*///#endif
/*TODO*///
    public static opcode nega = new opcode() {
        public void handler() {
            int r;
            r = -konami.a & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, konami.a, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode coma = new opcode() {
        public void handler() {
            konami.a = ~konami.a & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
            SEC();
        }
    };
    public static opcode lsra = new opcode() {
        public void handler() {
            CLR_NZC();
            konami.cc |= (konami.a & 0x01);
            konami.a = (konami.a >>> 1) & 0xFF;
            SET_Z8(konami.a);
        }
    };

    public static opcode rora = new opcode() {
        public void handler() {
            int r;
            r = ((konami.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            konami.cc |= (konami.a & CC_C);
            r = (r | konami.a >>> 1) & 0xFF;
            SET_NZ8(r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode asra = new opcode() {
        public void handler() {
            CLR_NZC();
            konami.cc |= (konami.a & CC_C);
            konami.a = ((konami.a & 0x80) | (konami.a >>> 1)) & 0xFF;
            SET_NZ8(konami.a);

        }
    };

    public static opcode asla = new opcode() {
        public void handler() {
            int r = (konami.a << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, konami.a, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode rola = new opcode() {
        public void handler() {
            int t, r;
            t = konami.a & 0xFFFF;
            r = ((konami.cc & CC_C) | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode deca = new opcode() {
        public void handler() {
            konami.a = (konami.a - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(konami.a);
        }
    };

    public static opcode inca = new opcode() {
        public void handler() {
            konami.a = (konami.a + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(konami.a);
        }
    };

    public static opcode tsta = new opcode() {
        public void handler() {
            CLR_NZVC();
            SET_NZ8(konami.a);
        }
    };

    public static opcode clra = new opcode() {
        public void handler() {
            konami.a = 0;
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode negb = new opcode() {
        public void handler() {
            int r;
            r = -konami.b & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, konami.b, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode comb = new opcode() {
        public void handler() {
            konami.b = ~konami.b & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
            SEC();
        }
    };

    public static opcode lsrb = new opcode() {
        public void handler() {
            CLR_NZC();
            konami.cc |= (konami.b & CC_C);
            konami.b = (konami.b >>> 1) & 0xFF;
            SET_Z8(konami.b);
        }
    };

    public static opcode rorb = new opcode() {
        public void handler() {
            int r;
            r = ((konami.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            konami.cc |= (konami.b & CC_C);
            r = (r | konami.b >>> 1) & 0xFF;
            SET_NZ8(r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode asrb = new opcode() {
        public void handler() {
            CLR_NZC();
            konami.cc |= (konami.b & CC_C);
            konami.b = ((konami.b & 0x80) | (konami.b >>> 1)) & 0xFF;
            SET_NZ8(konami.b);
        }
    };

    public static opcode aslb = new opcode() {
        public void handler() {
            int r = (konami.b << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, konami.b, r);
            konami.b = r & 0xFF;

        }
    };

    public static opcode rolb = new opcode() {
        public void handler() {
            int t, r;
            t = konami.b & 0xFFFF;
            r = konami.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode decb = new opcode() {
        public void handler() {
            konami.b = (konami.b - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(konami.b);
        }
    };

    public static opcode incb = new opcode() {
        public void handler() {
            konami.b = (konami.b + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(konami.b);
        }
    };

    public static opcode tstb = new opcode() {
        public void handler() {
            CLR_NZVC();
            SET_NZ8(konami.b);
        }
    };

    public static opcode clrb = new opcode() {
        public void handler() {
            konami.b = 0;
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode neg_ix = new opcode() {
        public void handler() {
            int r, t;
            t = RM(ea) & 0xFFFF;
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r);
        }
    };

    public static opcode com_ix = new opcode() {
        public void handler() {
            int t;
            t = RM(ea) & 0xFF;
            t = ~t & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    public static opcode lsr_ix = new opcode() {
        public void handler() {
            int t;
            t = RM(ea) & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = (t >>> 1) & 0xFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    public static opcode ror_ix = new opcode() {
        public void handler() {
            int/*UINT8*/ t, r;
            t = RM(ea) & 0xFF;
            r = ((konami.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            r = (r | t >>> 1) & 0xFF;
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    public static opcode asr_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = ((t & 0x80) | (t >>> 1)) & 0xFF;
            SET_NZ8(t);
            WM(ea, t);
        }
    };

    public static opcode asl_ix = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = RM(ea) & 0xFFFF;
            r = (t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode rol_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = konami.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode dec_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            t = (t - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);

        }
    };

    public static opcode inc_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            t = (t + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(t);
            WM(ea, t);
        }
    };

    public static opcode tst_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            CLR_NZVC();
            SET_NZ8(t);
        }
    };

    public static opcode jmp_ix = new opcode() {
        public void handler() {
            konami.pc = ea & 0xFFFF;
            change_pc(konami.pc);
        }
    };

    public static opcode clr_ix = new opcode() {
        public void handler() {
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode neg_ex = new opcode() {
        public void handler() {
            int/*UINT16*/ r, t;
            t = EXTBYTE() & 0xFFFF;
            r = -t & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(0, t, r);
            WM(ea, r & 0xFF);
        }
    };

    public static opcode com_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            t = ~t & 0xFF;
            CLR_NZV();
            SET_NZ8(t);
            SEC();
            WM(ea, t);
        }
    };

    public static opcode lsr_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE() & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = (t >>> 1) & 0XFF;
            SET_Z8(t);
            WM(ea, t);
        }
    };

    public static opcode ror_ex = new opcode() {
        public void handler() {
            int/*UINT8*/ t, r;
            t = EXTBYTE() & 0xFF;
            r = ((konami.cc & CC_C) << 7) & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            r = (r | t >>> 1) & 0xFF;
            SET_NZ8(r);
            WM(ea, r);
        }
    };

    public static opcode asr_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE() & 0xFF;
            CLR_NZC();
            konami.cc |= (t & CC_C);
            t = ((t & 0x80) | (t >>> 1)) & 0xFF;
            SET_NZ8(t);
            WM(ea, t);
        }
    };

    public static opcode asl_ex = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode rol_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = konami.cc & CC_C;
            r = (r | t << 1) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(t, t, r);
            WM(ea, r);
        }
    };

    public static opcode dec_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            t = (t - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(t);
            WM(ea, t);
        }
    };

    public static opcode inc_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            t = (t + 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8I(t);
            WM(ea, t);
        }
    };
    public static opcode tst_ex = new opcode() {
        public void handler() {
            int t;
            t = EXTBYTE() & 0xFF;
            CLR_NZVC();
            SET_NZ8(t);
        }
    };

    public static opcode jmp_ex = new opcode() {
        public void handler() {
            EXTENDED();
            konami.pc = ea & 0xFFFF;
            change_pc(konami.pc);

        }
    };

    public static opcode clr_ex = new opcode() {
        public void handler() {
            EXTENDED();
            WM(ea, 0);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode suba_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode cmpa_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
        }
    };

    public static opcode sbca_im = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.a - t - (konami.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    /*TODO*////* $83 SUBD (CMPD CMPU) immediate -**** */
/*TODO*///INLINE void subd_im( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $1083 CMPD immediate -**** */
/*TODO*///INLINE void cmpd_im( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $1183 CMPU immediate -**** */
/*TODO*///INLINE void cmpu_im( void )
/*TODO*///{
/*TODO*///	UINT32 r, d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = U;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode anda_im = new opcode() {
        public void handler() {
            int t = IMMBYTE() & 0xFF;
            konami.a = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode bita_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE();
            r = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_im = new opcode() {
        public void handler() {
            konami.a = IMMBYTE() & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };
    /*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $87 STA immediate -**0- */
/*TODO*///INLINE void sta_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(A);
/*TODO*///	IMM8;
/*TODO*///	WM(EAD,A);
/*TODO*///}
/*TODO*///
    public static opcode eora_im = new opcode() {
        public void handler() {
            int t = IMMBYTE() & 0xFF;
            konami.a = (konami.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adca_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.a + t + (konami.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode ora_im = new opcode() {
        public void handler() {
            int t = IMMBYTE() & 0xFF;
            konami.a = (konami.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adda_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };
    /*TODO*///
/*TODO*////* $8C CMPX (CMPY CMPS) immediate -**** */
/*TODO*///INLINE void cmpx_im( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $108C CMPY immediate -**** */
/*TODO*///INLINE void cmpy_im( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $118C CMPS immediate -**** */
/*TODO*///INLINE void cmps_im( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $8D BSR ----- */
/*TODO*///INLINE void bsr( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///	IMMBYTE(t);
/*TODO*///	PUSHWORD(pPC);
/*TODO*///	PC += SIGNED(t);
/*TODO*///	change_pc(PCD);
/*TODO*///}
/*TODO*///
    public static opcode ldx_im = new opcode() {
        public void handler() {
            konami.x = IMMWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.x);
        }
    };

    public static opcode ldy_im = new opcode() {
        public void handler() {
            konami.y = IMMWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.y);
        }
    };

    /*TODO*////* is this a legal instruction? */
/*TODO*////* $8F STX (STY) immediate -**0- */
/*TODO*///INLINE void stx_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(X);
/*TODO*///	IMM16;
/*TODO*///	WM16(EAD,&pX);
/*TODO*///}
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $108F STY immediate -**0- */
/*TODO*///INLINE void sty_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(Y);
/*TODO*///	IMM16;
/*TODO*///	WM16(EAD,&pY);
/*TODO*///}
    public static opcode suba_di = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode cmpa_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
        }
    };

    public static opcode sbca_di = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.a - t - (konami.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    /*TODO*////* $93 SUBD (CMPD CMPU) direct -**** */
/*TODO*///INLINE void subd_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $1093 CMPD direct -**** */
/*TODO*///INLINE void cmpd_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $1193 CMPU direct -**** */
/*TODO*///INLINE void cmpu_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = U;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(U,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode anda_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            konami.a = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode bita_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFF;
            r = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_di = new opcode() {
        public void handler() {
            konami.a = DIRBYTE() & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode sta_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(konami.a);
            DIRECT();
            WM(ea, konami.a);
        }
    };

    public static opcode eora_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            konami.a = (konami.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adca_di = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.a + t + (konami.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode ora_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            konami.a = (konami.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adda_di = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };
    /*TODO*///
/*TODO*////* $9C CMPX (CMPY CMPS) direct -**** */
/*TODO*///INLINE void cmpx_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $109C CMPY direct -**** */
/*TODO*///INLINE void cmpy_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $119C CMPS direct -**** */
/*TODO*///INLINE void cmps_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode jsr_di = new opcode() {
        public void handler() {
            DIRECT();
            PUSHWORD(konami.pc);
            konami.pc = ea & 0xFFFF;
            change_pc(konami.pc);
        }
    };

    public static opcode ldx_di = new opcode() {
        public void handler() {
            konami.x = DIRWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.x);
        }
    };

    public static opcode ldy_di = new opcode() {
        public void handler() {
            konami.y = DIRWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.y);
        }
    };

    public static opcode stx_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.x);
            DIRECT();
            WM16(ea, konami.x);
        }
    };

    public static opcode sty_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.y);
            DIRECT();
            WM16(ea, konami.y);
        }
    };

    public static opcode suba_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode cmpa_ix = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
        }
    };

    /*TODO*////* $a2 SBCA indexed ?**** */
/*TODO*///INLINE void sbca_ix( void )
/*TODO*///{
/*TODO*///	UINT16	  t,r;
/*TODO*///	t = RM(EAD);
/*TODO*///	r = A - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	A = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $a3 SUBD (CMPD CMPU) indexed -**** */
/*TODO*///INLINE void subd_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $10a3 CMPD indexed -**** */
/*TODO*///INLINE void cmpd_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $11a3 CMPU indexed -**** */
/*TODO*///INLINE void cmpu_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	r = U - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(U,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode anda_ix = new opcode() {
        public void handler() {
            int t;
            t = RM(ea) & 0xFF;
            konami.a = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode bita_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            int r = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode lda_ix = new opcode() {
        public void handler() {
            konami.a = RM(ea) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode sta_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(konami.a);
            WM(ea, konami.a);
        }
    };

    public static opcode eora_ix = new opcode() {
        public void handler() {
            /*UINT8*/
            int t;
            t = RM(ea);
            konami.a = (konami.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adca_ix = new opcode() {
        public void handler() {
            int/*UINT16*/ t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.a + t + (konami.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode ora_ix = new opcode() {
        public void handler() {
            int t;
            t = RM(ea) & 0xFF;
            konami.a = (konami.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adda_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    /*TODO*////* $aC CMPX (CMPY CMPS) indexed -**** */
/*TODO*///INLINE void cmpx_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $10aC CMPY indexed -**** */
/*TODO*///INLINE void cmpy_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $11aC CMPS indexed -**** */
/*TODO*///INLINE void cmps_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode jsr_ix = new opcode() {
        public void handler() {
            PUSHWORD(konami.pc);
            konami.pc = ea & 0xFFFF;
            change_pc(konami.pc);
        }
    };

    public static opcode ldx_ix = new opcode() {
        public void handler() {
            konami.x = RM16(ea) & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.x);
        }
    };

    public static opcode ldy_ix = new opcode() {
        public void handler() {
            konami.y = RM16(ea) & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.y);
        }
    };

    public static opcode stx_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.x);
            WM16(ea, konami.x);
        }
    };

    public static opcode sty_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.y);
            WM16(ea, konami.y);
        }
    };

    public static opcode suba_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    public static opcode cmpa_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.a - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
        }
    };

    public static opcode sbca_ex = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.a - t - (konami.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    /*TODO*////* $b3 SUBD (CMPD CMPU) extended -**** */
/*TODO*///INLINE void subd_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $10b3 CMPD extended -**** */
/*TODO*///INLINE void cmpd_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $11b3 CMPU extended -**** */
/*TODO*///INLINE void cmpu_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = U;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode anda_ex = new opcode() {
        public void handler() {
            /*UINT8*/
            int t = EXTBYTE() & 0xFF;
            konami.a = (konami.a & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    /*TODO*////* $b5 BITA extended -**0- */
/*TODO*///INLINE void bita_ex( void )
/*TODO*///{
/*TODO*///	UINT8 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A & t;
/*TODO*///	CLR_NZV; SET_NZ8(r);
/*TODO*///}
/*TODO*///
    public static opcode lda_ex = new opcode() {
        public void handler() {
            konami.a = EXTBYTE() & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };
    public static opcode sta_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(konami.a);
            EXTENDED();
            WM(ea, konami.a);
        }
    };

    public static opcode eora_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE() & 0xFF;
            konami.a = (konami.a ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    /*TODO*////* $b9 ADCA extended ***** */
/*TODO*///INLINE void adca_ex( void )
/*TODO*///{
/*TODO*///	UINT16 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = A + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(A,t,r);
/*TODO*///	SET_H(A,t,r);
/*TODO*///	A = r;
/*TODO*///}
/*TODO*///
    public static opcode ora_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE() & 0xFF;
            konami.a = (konami.a | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.a);
        }
    };

    public static opcode adda_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.a + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.a, t, r);
            SET_H(konami.a, t, r);
            konami.a = r & 0xFF;
        }
    };

    /*TODO*////* $bC CMPX (CMPY CMPS) extended -**** */
/*TODO*///INLINE void cmpx_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = X;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $10bC CMPY extended -**** */
/*TODO*///INLINE void cmpy_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = Y;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
/*TODO*////* $11bC CMPS extended -**** */
/*TODO*///INLINE void cmps_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = S;
/*TODO*///	r = d - b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///}
/*TODO*///
    public static opcode jsr_ex = new opcode() {
        public void handler() {
            EXTENDED();
            PUSHWORD(konami.pc);
            konami.pc = ea & 0xFFFF;
            change_pc(konami.pc);
        }
    };

    public static opcode ldx_ex = new opcode() {
        public void handler() {
            konami.x = EXTWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.x);
        }
    };

    public static opcode ldy_ex = new opcode() {
        public void handler() {
            konami.y = EXTWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.y);
        }
    };

    public static opcode stx_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.x);
            EXTENDED();
            WM16(ea, konami.x);
        }
    };

    public static opcode sty_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.y);
            EXTENDED();
            WM16(ea, konami.y);
        }
    };

    public static opcode subb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode cmpb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
        }
    };

    public static opcode sbcb_im = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.b - t - (konami.cc & CC_C)) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    /*TODO*////* $c3 ADDD immediate -**** */
/*TODO*///INLINE void addd_im( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	IMMWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
    public static opcode andb_im = new opcode() {
        public void handler() {
            int t = IMMBYTE() & 0xFF;
            konami.b = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode bitb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFF;
            r = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_im = new opcode() {
        public void handler() {
            konami.b = IMMBYTE() & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    /*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $c7 STB immediate -**0- */
/*TODO*///INLINE void stb_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ8(B);
/*TODO*///	IMM8;
/*TODO*///	WM(EAD,B);
/*TODO*///}
/*TODO*///
    public static opcode eorb_im = new opcode() {
        public void handler() {
            int t = IMMBYTE() & 0xFF;
            konami.b = (konami.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode adcb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.b + t + (konami.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.b, t, r);
            SET_H(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode orb_im = new opcode() {
        public void handler() {
            int t = IMMBYTE() & 0xFF;
            konami.b = (konami.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode addb_im = new opcode() {
        public void handler() {
            int t, r;
            t = IMMBYTE() & 0xFFFF;
            r = (konami.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.b, t, r);
            SET_H(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode ldd_im = new opcode() {
        public void handler() {
            int tmp = IMMWORD() & 0xFFFF;
            setDreg(tmp);
            CLR_NZV();
            SET_NZ16(getDreg());
        }
    };
    /*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $cD STD immediate -**0- */
/*TODO*///INLINE void std_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(D);
/*TODO*///    IMM16;
/*TODO*///	WM16(EAD,&pD);
/*TODO*///}
/*TODO*///
    public static opcode ldu_im = new opcode() {
        public void handler() {
            konami.u = IMMWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.u);
        }
    };

    public static opcode lds_im = new opcode() {
        public void handler() {
            konami.s = IMMWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.s);
            konami.int_state |= KONAMI_LDS;
        }
    };

    /*TODO*////* is this a legal instruction? */
/*TODO*////* $cF STU (STS) immediate -**0- */
/*TODO*///INLINE void stu_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(U);
/*TODO*///    IMM16;
/*TODO*///	WM16(EAD,&pU);
/*TODO*///}
/*TODO*///
/*TODO*////* is this a legal instruction? */
/*TODO*////* $10cF STS immediate -**0- */
/*TODO*///INLINE void sts_im( void )
/*TODO*///{
/*TODO*///	CLR_NZV;
/*TODO*///	SET_NZ16(S);
/*TODO*///    IMM16;
/*TODO*///	WM16(EAD,&pS);
/*TODO*///}
    public static opcode subb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode cmpb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
        }
    };
    /*TODO*////* $d2 SBCB direct ?**** */
/*TODO*///INLINE void sbcb_di( void )
/*TODO*///{
/*TODO*///	UINT16	  t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $d3 ADDD direct -**** */
/*TODO*///INLINE void addd_di( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	DIRWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
    public static opcode andb_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            konami.b = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    static opcode bitb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFF;
            r = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_di = new opcode() {
        public void handler() {
            konami.b = DIRBYTE() & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode stb_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(konami.b);
            DIRECT();
            WM(ea, konami.b);
        }
    };

    public static opcode eorb_di = new opcode() {
        public void handler() {
            int/*UINT8*/ t;
            t = DIRBYTE() & 0xFF;
            konami.b = (konami.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    /*TODO*///
/*TODO*////* $d9 ADCB direct ***** */
/*TODO*///INLINE void adcb_di( void )
/*TODO*///{
/*TODO*///	UINT16 t,r;
/*TODO*///	DIRBYTE(t);
/*TODO*///	r = B + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///
    public static opcode orb_di = new opcode() {
        public void handler() {
            int t = DIRBYTE() & 0xFF;
            konami.b = (konami.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode addb_di = new opcode() {
        public void handler() {
            int t, r;
            t = DIRBYTE() & 0xFFFF;
            r = (konami.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.b, t, r);
            SET_H(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode ldd_di = new opcode() {
        public void handler() {
            int temp = DIRWORD();
            setDreg(temp);
            CLR_NZV();
            SET_NZ16(getDreg());
        }
    };

    public static opcode std_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(getDreg());
            DIRECT();
            WM16(ea, getDreg());
        }
    };

    public static opcode ldu_di = new opcode() {
        public void handler() {
            konami.u = DIRWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.u);
        }
    };

    public static opcode lds_di = new opcode() {
        public void handler() {
            konami.s = DIRWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.s);
            konami.int_state |= KONAMI_LDS;
        }
    };

    public static opcode stu_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.u);
            DIRECT();
            WM16(ea, konami.u);
        }
    };

    public static opcode sts_di = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.s);
            DIRECT();
            WM16(ea, konami.s);
        }
    };

    public static opcode subb_ix = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode cmpb_ix = new opcode() {
        public void handler() {
            /*UINT16*/
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
        }
    };
    /*TODO*////* $e2 SBCB indexed ?**** */
/*TODO*///INLINE void sbcb_ix( void )
/*TODO*///{
/*TODO*///	UINT16	  t,r;
/*TODO*///	t = RM(EAD);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $e3 ADDD indexed -**** */
/*TODO*///INLINE void addd_ix( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	b.d=RM16(EAD);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
    public static opcode andb_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            konami.b = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode bitb_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM(ea) & 0xFF;
            r = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_ix = new opcode() {
        public void handler() {
            konami.b = RM(ea) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode stb_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(konami.b);
            WM(ea, konami.b);
        }
    };

    public static opcode eorb_ix = new opcode() {
        public void handler() {
            int t = RM(ea) & 0xFF;
            konami.b = (konami.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode adcb_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.b + t + (konami.cc & CC_C)) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.b, t, r);
            SET_H(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode orb_ix = new opcode() {
        public void handler() {
            int t;
            t = RM(ea) & 0xFF;
            konami.b = (konami.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode addb_ix = new opcode() {
        public void handler() {
            int t, r;
            t = RM(ea) & 0xFFFF;
            r = (konami.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.b, t, r);
            SET_H(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode ldd_ix = new opcode() {
        public void handler() {
            int temp = RM16(ea) & 0xFFFF;
            setDreg(temp);
            CLR_NZV();
            SET_NZ16(getDreg());
        }
    };

    public static opcode std_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(getDreg());
            WM16(ea, getDreg());
        }
    };

    public static opcode ldu_ix = new opcode() {
        public void handler() {
            konami.u = RM16(ea) & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.u);
        }
    };

    public static opcode lds_ix = new opcode() {
        public void handler() {
            konami.s = RM(ea) & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.s);
            konami.int_state |= KONAMI_LDS;
        }
    };

    public static opcode stu_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.u);
            WM16(ea, konami.u);
        }
    };
    public static opcode sts_ix = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.s);
            WM16(ea, konami.s);
        }
    };

    public static opcode subb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode cmpb_ex = new opcode() {
        public void handler()//todo recheck
        {
            /*UINT16*/
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.b - t) & 0xFFFF;
            CLR_NZVC();
            SET_FLAGS8(konami.b, t, r);
        }
    };

    /*TODO*////* $f2 SBCB extended ?**** */
/*TODO*///INLINE void sbcb_ex( void )
/*TODO*///{
/*TODO*///	UINT16	  t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B - t - (CC & CC_C);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///
/*TODO*////* $f3 ADDD extended -**** */
/*TODO*///INLINE void addd_ex( void )
/*TODO*///{
/*TODO*///	UINT32 r,d;
/*TODO*///	PAIR b;
/*TODO*///	EXTWORD(b);
/*TODO*///	d = D;
/*TODO*///	r = d + b.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(d,b.d,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
    public static opcode andb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            konami.b = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode bitb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE();
            r = (konami.b & t) & 0xFF;
            CLR_NZV();
            SET_NZ8(r);
        }
    };

    public static opcode ldb_ex = new opcode() {
        public void handler() {
            konami.b = EXTBYTE();
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode stb_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ8(konami.b);
            EXTENDED();
            WM(ea, konami.b);
        }
    };

    public static opcode eorb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE();
            konami.b = (konami.b ^ t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };
    /*TODO*///
/*TODO*////* $f9 ADCB extended ***** */
/*TODO*///INLINE void adcb_ex( void )
/*TODO*///{
/*TODO*///	UINT16 t,r;
/*TODO*///	EXTBYTE(t);
/*TODO*///	r = B + t + (CC & CC_C);
/*TODO*///	CLR_HNZVC;
/*TODO*///	SET_FLAGS8(B,t,r);
/*TODO*///	SET_H(B,t,r);
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///

    public static opcode orb_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE() & 0xFF;
            konami.b = (konami.b | t) & 0xFF;
            CLR_NZV();
            SET_NZ8(konami.b);
        }
    };

    public static opcode addb_ex = new opcode() {
        public void handler() {
            int t, r;
            t = EXTBYTE() & 0xFFFF;
            r = (konami.b + t) & 0xFFFF;
            CLR_HNZVC();
            SET_FLAGS8(konami.b, t, r);
            SET_H(konami.b, t, r);
            konami.b = r & 0xFF;
        }
    };

    public static opcode ldd_ex = new opcode() {
        public void handler() {
            setDreg(EXTWORD());
            CLR_NZV();
            SET_NZ16(getDreg());
        }
    };

    public static opcode std_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(getDreg());
            EXTENDED();
            WM16(ea, getDreg());
        }
    };

    public static opcode ldu_ex = new opcode() {
        public void handler() {
            konami.u = EXTWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.u);
        }
    };

    public static opcode lds_ex = new opcode() {
        public void handler() {
            konami.s = EXTWORD() & 0xFFFF;
            CLR_NZV();
            SET_NZ16(konami.s);
            konami.int_state |= KONAMI_LDS;
        }
    };
    public static opcode stu_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.u);
            EXTENDED();
            WM16(ea, konami.u);
        }
    };

    public static opcode sts_ex = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(konami.s);
            EXTENDED();
            WM16(ea, konami.s);
        }
    };

    public static opcode setline_im = new opcode() {
        public void handler() {
            //UINT8 t;
            int t = IMMBYTE() & 0xFF;

            if (konami_cpu_setlines_callback != null) {
                konami_cpu_setlines_callback.handler(t);
            }
        }
    };

    public static opcode setline_ix = new opcode() {
        public void handler() {
            //UINT8 t;
            int t = RM(ea) & 0xFF;

            if (konami_cpu_setlines_callback != null) {
                konami_cpu_setlines_callback.handler(t);
            }
        }
    };

    public static opcode setline_di = new opcode() {
        public void handler() {
            //UINT8 t;
            int t = DIRBYTE() & 0xFF;

            if (konami_cpu_setlines_callback != null) {
                konami_cpu_setlines_callback.handler(t);
            }
        }
    };

    public static opcode setline_ex = new opcode() {
        public void handler() {
            int t = EXTBYTE() & 0xFF;
            if (konami_cpu_setlines_callback != null) {
                konami_cpu_setlines_callback.handler(t);
            }
        }
    };

    public static opcode bmove = new opcode() {
        public void handler() {
            /*UINT8*/
            int t;
            while (konami.u != 0) {
                t = RM(konami.y) & 0xFF;
                WM(konami.x, t);
                konami.y = (konami.y + 1) & 0xFFFF;//Y++;
                konami.x = (konami.x + 1) & 0xFFFF;//X++;
                konami.u = (konami.u - 1) & 0xFFFF;//U--;
                konami_ICount[0] -= 2;
            }
        }
    };

    public static opcode move = new opcode() {
        public void handler() {
            int t = RM(konami.y) & 0xFF;
            WM(konami.x, t);
            konami.y = (konami.y + 1) & 0xFFFF;//Y++;
            konami.x = (konami.x + 1) & 0xFFFF;//X++;
            konami.u = (konami.u - 1) & 0xFFFF;//U--;
        }
    };

    public static opcode clrd = new opcode() {
        public void handler() {
            setDreg(0);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode clrw_ix = new opcode() {
        public void handler() {
            int t;//PAIR t;
            t = 0;
            WM16(ea, t);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode clrw_di = new opcode() {
        public void handler() {
            /*PAIR*/
            int t;
            t = 0;
            DIRECT();
            WM16(ea, t);
            CLR_NZVC();
            SEZ();
        }
    };

    public static opcode clrw_ex = new opcode() {
        public void handler() {
            int t;//PAIR t;
            t = 0;
            EXTENDED();
            WM16(ea, t);
            CLR_NZVC();
            SEZ();
        }
    };

    /*TODO*////* LSRD immediate -0*-* */
/*TODO*///INLINE void lsrd( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	IMMBYTE( t );
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D >>= 1;
/*TODO*///		SET_Z16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* RORD immediate -**-* */
/*TODO*///INLINE void rord( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	IMMBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = (CC & CC_C) << 15;
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		r |= D >> 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASRD immediate ?**-* */
/*TODO*///INLINE void asrd( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	IMMBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D = (D & 0x8000) | (D >> 1);
/*TODO*///		SET_NZ16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASLD immediate ?**** */
/*TODO*///INLINE void asld( void )
/*TODO*///{
/*TODO*///	UINT32	r;
/*TODO*///	UINT8	t;
/*TODO*///
/*TODO*///	IMMBYTE( t );
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = D << 1;
/*TODO*///		CLR_NZVC;
/*TODO*///		SET_FLAGS16(D,D,r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ROLD immediate -**-* */
/*TODO*///INLINE void rold( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	IMMBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		if ( D & 0x8000 ) SEC;
/*TODO*///		r = CC & CC_C;
/*TODO*///		r |= D << 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
    public static opcode decbjnz = new opcode() {
        public void handler() {
            konami.b = (konami.b - 1) & 0xFF;
            CLR_NZV();
            SET_FLAGS8D(konami.b);
            BRANCH((konami.cc & CC_Z) == 0);
        }
    };

    public static opcode decxjnz = new opcode() {
        public void handler() {
            konami.x = (konami.x - 1) & 0xFFFF;//--X;
            CLR_NZV();
            SET_NZ16(konami.x);
            /* should affect V as well? */

            BRANCH((konami.cc & CC_Z) == 0);
        }
    };

    /*TODO*///INLINE void bset( void )
/*TODO*///{
/*TODO*///	UINT8	t;
/*TODO*///
/*TODO*///	while( U != 0 ) {
/*TODO*///		t = A;
/*TODO*///		WM(XD,t);
/*TODO*///		X++;
/*TODO*///		U--;
/*TODO*///		konami_ICount -= 2;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void bset2( void )
/*TODO*///{
/*TODO*///	while( U != 0 ) {
/*TODO*///		WM16(XD,&pD);
/*TODO*///		X += 2;
/*TODO*///		U--;
/*TODO*///		konami_ICount -= 3;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* LMUL inherent --*-@ */
/*TODO*///INLINE void lmul( void )
/*TODO*///{
/*TODO*///	UINT32 t;
/*TODO*///	t = X * Y;
/*TODO*///	X = (t >> 16);
/*TODO*///	Y = (t & 0xffff);
/*TODO*///	CLR_ZC; SET_Z(t); if( t & 0x8000 ) SEC;
/*TODO*///}
/*TODO*///
/*TODO*////* DIVX inherent --*-@ */
/*TODO*///INLINE void divx( void )
/*TODO*///{
/*TODO*///	UINT16 t;
/*TODO*///	UINT8 r;
/*TODO*///	if ( B != 0 )
/*TODO*///	{
/*TODO*///		t = X / B;
/*TODO*///		r = X % B;
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* ?? */
/*TODO*///		t = 0;
/*TODO*///		r = 0;
/*TODO*///	}
/*TODO*///	CLR_ZC; SET_Z16(t); if ( t & 0x80 ) SEC;
/*TODO*///	X = t;
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///
/*TODO*////* INCD inherent -***- */
/*TODO*///INLINE void incd( void )
/*TODO*///{
/*TODO*///	UINT32 r;
/*TODO*///	r = D + 1;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS16(D,D,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* INCW direct -***- */
/*TODO*///INLINE void incw_di( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	DIRWORD(t);
/*TODO*///	r = t;
/*TODO*///	++r.d;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS16(t.d, t.d, r.d);;
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* INCW indexed -***- */
/*TODO*///INLINE void incw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	r = t;
/*TODO*///	++r.d;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS16(t.d, t.d, r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* INCW extended -***- */
/*TODO*///INLINE void incw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t, r;
/*TODO*///	EXTWORD(t);
/*TODO*///	r = t;
/*TODO*///	++r.d;
/*TODO*///	CLR_NZV; SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* DECD inherent -***- */
/*TODO*///INLINE void decd( void )
/*TODO*///{
/*TODO*///	UINT32 r;
/*TODO*///	r = D - 1;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS16(D,D,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* DECW direct -***- */
/*TODO*///INLINE void decw_di( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	DIRWORD(t);
/*TODO*///	r = t;
/*TODO*///	--r.d;
/*TODO*///	CLR_NZV;
/*TODO*///	SET_FLAGS16(t.d, t.d, r.d);;
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* DECW indexed -***- */
/*TODO*///INLINE void decw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t, r;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	r = t;
/*TODO*///	--r.d;
/*TODO*///	CLR_NZV; SET_FLAGS16(t.d, t.d, r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* DECW extended -***- */
/*TODO*///INLINE void decw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t, r;
/*TODO*///	EXTWORD(t);
/*TODO*///	r = t;
/*TODO*///	--r.d;
/*TODO*///	CLR_NZV; SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
    public static opcode tstd = new opcode() {
        public void handler() {
            CLR_NZV();
            SET_NZ16(getDreg());
        }
    };

    /*TODO*////* TSTW direct -**0- */
/*TODO*///INLINE void tstw_di( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	CLR_NZV;
/*TODO*///	DIRWORD(t);
/*TODO*///	SET_NZ16(t.d);
/*TODO*///}
/*TODO*///
/*TODO*////* TSTW indexed -**0- */
/*TODO*///INLINE void tstw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	CLR_NZV;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	SET_NZ16(t.d);
/*TODO*///}
/*TODO*///
/*TODO*////* TSTW extended -**0- */
/*TODO*///INLINE void tstw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	CLR_NZV;
/*TODO*///	EXTWORD(t);
/*TODO*///	SET_NZ16(t.d);
/*TODO*///}
/*TODO*///
/*TODO*////* LSRW direct -0*-* */
/*TODO*///INLINE void lsrw_di( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	DIRWORD(t);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	t.d >>= 1;
/*TODO*///	SET_Z16(t.d);
/*TODO*///	WM16(EAD,&t);
/*TODO*///}
/*TODO*///
/*TODO*////* LSRW indexed -0*-* */
/*TODO*///INLINE void lsrw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	t.d >>= 1;
/*TODO*///	SET_Z16(t.d);
/*TODO*///	WM16(EAD,&t);
/*TODO*///}
/*TODO*///
/*TODO*////* LSRW extended -0*-* */
/*TODO*///INLINE void lsrw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	EXTWORD(t);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	t.d >>= 1;
/*TODO*///	SET_Z16(t.d);
/*TODO*///	WM16(EAD,&t);
/*TODO*///}
/*TODO*///
/*TODO*////* RORW direct -**-* */
/*TODO*///INLINE void rorw_di( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	DIRWORD(t);
/*TODO*///	r.d = (CC & CC_C) << 15;
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	r.d |= t.d>>1;
/*TODO*///	SET_NZ16(r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* RORW indexed -**-* */
/*TODO*///INLINE void rorw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	r.d = (CC & CC_C) << 15;
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	r.d |= t.d>>1;
/*TODO*///	SET_NZ16(r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* RORW extended -**-* */
/*TODO*///INLINE void rorw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	EXTWORD(t);
/*TODO*///	r.d = (CC & CC_C) << 15;
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	r.d |= t.d>>1;
/*TODO*///	SET_NZ16(r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ASRW direct ?**-* */
/*TODO*///INLINE void asrw_di( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	DIRWORD(t);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
/*TODO*///	SET_NZ16(t.d);
/*TODO*///	WM16(EAD,&t);
/*TODO*///}
/*TODO*///
/*TODO*////* ASRW indexed ?**-* */
/*TODO*///INLINE void asrw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
/*TODO*///	SET_NZ16(t.d);
/*TODO*///	WM16(EAD,&t);
/*TODO*///}
/*TODO*///
/*TODO*////* ASRW extended ?**-* */
/*TODO*///INLINE void asrw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t;
/*TODO*///	EXTWORD(t);
/*TODO*///	CLR_NZC;
/*TODO*///	CC |= (t.d & CC_C);
/*TODO*///	t.d = (t.d & 0x8000) | (t.d >> 1);
/*TODO*///	SET_NZ16(t.d);
/*TODO*///	WM16(EAD,&t);
/*TODO*///}
/*TODO*///
/*TODO*////* ASLW direct ?**** */
/*TODO*///INLINE void aslw_di( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	DIRWORD(t);
/*TODO*///	r.d = t.d << 1;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ASLW indexed ?**** */
/*TODO*///INLINE void aslw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	r.d = t.d << 1;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ASLW extended ?**** */
/*TODO*///INLINE void aslw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	EXTWORD(t);
/*TODO*///	r.d = t.d << 1;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ROLW direct -**** */
/*TODO*///INLINE void rolw_di( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	DIRWORD(t);
/*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ROLW indexed -**** */
/*TODO*///INLINE void rolw_ix( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ROLW extended -**** */
/*TODO*///INLINE void rolw_ex( void )
/*TODO*///{
/*TODO*///	PAIR t,r;
/*TODO*///	EXTWORD(t);
/*TODO*///	r.d = (CC & CC_C) | (t.d << 1);
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(t.d,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* NEGD inherent ?**** */
/*TODO*///INLINE void negd( void )
/*TODO*///{
/*TODO*///	UINT32 r;
/*TODO*///	r = -D;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(0,D,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* NEGW direct ?**** */
/*TODO*///INLINE void negw_di( void )
/*TODO*///{
/*TODO*///	PAIR r,t;
/*TODO*///	DIRWORD(t);
/*TODO*///	r.d = -t.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(0,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* NEGW indexed ?**** */
/*TODO*///INLINE void negw_ix( void )
/*TODO*///{
/*TODO*///	PAIR r,t;
/*TODO*///	t.d=RM16(EAD);
/*TODO*///	r.d = -t.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(0,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* NEGW extended ?**** */
/*TODO*///INLINE void negw_ex( void )
/*TODO*///{
/*TODO*///	PAIR r,t;
/*TODO*///	EXTWORD(t);
/*TODO*///	r.d = -t.d;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(0,t.d,r.d);
/*TODO*///	WM16(EAD,&r);
/*TODO*///}
/*TODO*///
/*TODO*////* ABSA inherent ?**** */
/*TODO*///INLINE void absa( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	if (A & 0x80)
/*TODO*///		r = -A;
/*TODO*///	else
/*TODO*///		r = A;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(0,A,r);
/*TODO*///	A = r;
/*TODO*///}
/*TODO*///
/*TODO*////* ABSB inherent ?**** */
/*TODO*///INLINE void absb( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	if (B & 0x80)
/*TODO*///		r = -B;
/*TODO*///	else
/*TODO*///		r = B;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS8(0,B,r);
/*TODO*///	B = r;
/*TODO*///}
/*TODO*///
/*TODO*////* ABSD inherent ?**** */
/*TODO*///INLINE void absd( void )
/*TODO*///{
/*TODO*///	UINT32 r;
/*TODO*///	if (D & 0x8000)
/*TODO*///		r = -D;
/*TODO*///	else
/*TODO*///		r = D;
/*TODO*///	CLR_NZVC;
/*TODO*///	SET_FLAGS16(0,D,r);
/*TODO*///	D = r;
/*TODO*///}
/*TODO*///
/*TODO*////* LSRD direct -0*-* */
/*TODO*///INLINE void lsrd_di( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	DIRBYTE( t );
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D >>= 1;
/*TODO*///		SET_Z16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* RORD direct -**-* */
/*TODO*///INLINE void rord_di( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	DIRBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = (CC & CC_C) << 15;
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		r |= D >> 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASRD direct ?**-* */
/*TODO*///INLINE void asrd_di( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	DIRBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D = (D & 0x8000) | (D >> 1);
/*TODO*///		SET_NZ16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASLD direct ?**** */
/*TODO*///INLINE void asld_di( void )
/*TODO*///{
/*TODO*///	UINT32	r;
/*TODO*///	UINT8	t;
/*TODO*///
/*TODO*///	DIRBYTE( t );
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = D << 1;
/*TODO*///		CLR_NZVC;
/*TODO*///		SET_FLAGS16(D,D,r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ROLD direct -**-* */
/*TODO*///INLINE void rold_di( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	DIRBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		if ( D & 0x8000 ) SEC;
/*TODO*///		r = CC & CC_C;
/*TODO*///		r |= D << 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* LSRD indexed -0*-* */
/*TODO*///INLINE void lsrd_ix( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	t=RM(EA);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D >>= 1;
/*TODO*///		SET_Z16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* RORD indexed -**-* */
/*TODO*///INLINE void rord_ix( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	t=RM(EA);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = (CC & CC_C) << 15;
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		r |= D >> 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASRD indexed ?**-* */
/*TODO*///INLINE void asrd_ix( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	t=RM(EA);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D = (D & 0x8000) | (D >> 1);
/*TODO*///		SET_NZ16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASLD indexed ?**** */
/*TODO*///INLINE void asld_ix( void )
/*TODO*///{
/*TODO*///	UINT32	r;
/*TODO*///	UINT8	t;
/*TODO*///
/*TODO*///	t=RM(EA);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = D << 1;
/*TODO*///		CLR_NZVC;
/*TODO*///		SET_FLAGS16(D,D,r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ROLD indexed -**-* */
/*TODO*///INLINE void rold_ix( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	t=RM(EA);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		if ( D & 0x8000 ) SEC;
/*TODO*///		r = CC & CC_C;
/*TODO*///		r |= D << 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* LSRD extended -0*-* */
/*TODO*///INLINE void lsrd_ex( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	EXTBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D >>= 1;
/*TODO*///		SET_Z16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* RORD extended -**-* */
/*TODO*///INLINE void rord_ex( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	EXTBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = (CC & CC_C) << 15;
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		r |= D >> 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASRD extended ?**-* */
/*TODO*///INLINE void asrd_ex( void )
/*TODO*///{
/*TODO*///	UINT8 t;
/*TODO*///
/*TODO*///	EXTBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		CC |= (D & CC_C);
/*TODO*///		D = (D & 0x8000) | (D >> 1);
/*TODO*///		SET_NZ16(D);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ASLD extended ?**** */
/*TODO*///INLINE void asld_ex( void )
/*TODO*///{
/*TODO*///	UINT32	r;
/*TODO*///	UINT8	t;
/*TODO*///
/*TODO*///	EXTBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		r = D << 1;
/*TODO*///		CLR_NZVC;
/*TODO*///		SET_FLAGS16(D,D,r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////* ROLD extended -**-* */
/*TODO*///INLINE void rold_ex( void )
/*TODO*///{
/*TODO*///	UINT16 r;
/*TODO*///	UINT8  t;
/*TODO*///
/*TODO*///	EXTBYTE(t);
/*TODO*///
/*TODO*///	while ( t-- ) {
/*TODO*///		CLR_NZC;
/*TODO*///		if ( D & 0x8000 ) SEC;
/*TODO*///		r = CC & CC_C;
/*TODO*///		r |= D << 1;
/*TODO*///		SET_NZ16(r);
/*TODO*///		D = r;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void opcode2( void )
/*TODO*///{
/*TODO*///	UINT8 ireg2 = ROP_ARG(PCD);
/*TODO*///	PC++;
/*TODO*///
/*TODO*///	switch ( ireg2 ) {
/*TODO*/////	case 0x00: EA=0; break; /* auto increment */
/*TODO*/////	case 0x01: EA=0; break; /* double auto increment */
/*TODO*/////	case 0x02: EA=0; break; /* auto decrement */
/*TODO*/////	case 0x03: EA=0; break; /* double auto decrement */
/*TODO*/////	case 0x04: EA=0; break; /* postbyte offs */
/*TODO*/////	case 0x05: EA=0; break; /* postword offs */
/*TODO*/////	case 0x06: EA=0; break; /* normal */
/*TODO*///	case 0x07:
/*TODO*///		EAD=0;
/*TODO*///		(*konami_extended[konami.ireg])();
/*TODO*///        konami_ICount -= 2;
/*TODO*///		return;
/*TODO*/////	case 0x08: EA=0; break; /* indirect - auto increment */
/*TODO*/////	case 0x09: EA=0; break; /* indirect - double auto increment */
/*TODO*/////	case 0x0a: EA=0; break; /* indirect - auto decrement */
/*TODO*/////	case 0x0b: EA=0; break; /* indirect - double auto decrement */
/*TODO*/////	case 0x0c: EA=0; break; /* indirect - postbyte offs */
/*TODO*/////	case 0x0d: EA=0; break; /* indirect - postword offs */
/*TODO*/////	case 0x0e: EA=0; break; /* indirect - normal */
/*TODO*///	case 0x0f:				/* indirect - extended */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0x10: EA=0; break; /* auto increment */
/*TODO*/////	case 0x11: EA=0; break; /* double auto increment */
/*TODO*/////	case 0x12: EA=0; break; /* auto decrement */
/*TODO*/////	case 0x13: EA=0; break; /* double auto decrement */
/*TODO*/////	case 0x14: EA=0; break; /* postbyte offs */
/*TODO*/////	case 0x15: EA=0; break; /* postword offs */
/*TODO*/////	case 0x16: EA=0; break; /* normal */
/*TODO*/////	case 0x17: EA=0; break; /* extended */
/*TODO*/////	case 0x18: EA=0; break; /* indirect - auto increment */
/*TODO*/////	case 0x19: EA=0; break; /* indirect - double auto increment */
/*TODO*/////	case 0x1a: EA=0; break; /* indirect - auto decrement */
/*TODO*/////	case 0x1b: EA=0; break; /* indirect - double auto decrement */
/*TODO*/////	case 0x1c: EA=0; break; /* indirect - postbyte offs */
/*TODO*/////	case 0x1d: EA=0; break; /* indirect - postword offs */
/*TODO*/////	case 0x1e: EA=0; break; /* indirect - normal */
/*TODO*/////	case 0x1f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*////* base X */
/*TODO*///    case 0x20:              /* auto increment */
/*TODO*///		EA=X;
/*TODO*///		X++;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x21:				/* double auto increment */
/*TODO*///		EA=X;
/*TODO*///		X+=2;
/*TODO*///        konami_ICount-=3;
/*TODO*///        break;
/*TODO*///	case 0x22:				/* auto decrement */
/*TODO*///		X--;
/*TODO*///		EA=X;
/*TODO*///        konami_ICount-=2;
/*TODO*///        break;
/*TODO*///	case 0x23:				/* double auto decrement */
/*TODO*///		X-=2;
/*TODO*///		EA=X;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x24:				/* postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=X+SIGNED(EA);
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x25:				/* postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=X;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x26:				/* normal */
/*TODO*///		EA=X;
/*TODO*///		break;
/*TODO*/////	case 0x27: EA=0; break; /* extended */
/*TODO*///	case 0x28:				/* indirect - auto increment */
/*TODO*///		EA=X;
/*TODO*///		X++;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x29:				/* indirect - double auto increment */
/*TODO*///		EA=X;
/*TODO*///		X+=2;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x2a:				/* indirect - auto decrement */
/*TODO*///		X--;
/*TODO*///		EA=X;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x2b:				/* indirect - double auto decrement */
/*TODO*///		X-=2;
/*TODO*///		EA=X;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x2c:				/* indirect - postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=X+SIGNED(EA);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x2d:				/* indirect - postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=X;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0x2e:				/* indirect - normal */
/*TODO*///		EA=X;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*/////	case 0x2f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*////* base Y */
/*TODO*///    case 0x30:              /* auto increment */
/*TODO*///		EA=Y;
/*TODO*///		Y++;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x31:				/* double auto increment */
/*TODO*///		EA=Y;
/*TODO*///		Y+=2;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x32:				/* auto decrement */
/*TODO*///		Y--;
/*TODO*///		EA=Y;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x33:				/* double auto decrement */
/*TODO*///		Y-=2;
/*TODO*///		EA=Y;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x34:				/* postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=Y+SIGNED(EA);
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x35:				/* postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=Y;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x36:				/* normal */
/*TODO*///		EA=Y;
/*TODO*///		break;
/*TODO*/////	case 0x37: EA=0; break; /* extended */
/*TODO*///	case 0x38:				/* indirect - auto increment */
/*TODO*///		EA=Y;
/*TODO*///		Y++;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x39:				/* indirect - double auto increment */
/*TODO*///		EA=Y;
/*TODO*///		Y+=2;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x3a:				/* indirect - auto decrement */
/*TODO*///		Y--;
/*TODO*///		EA=Y;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x3b:				/* indirect - double auto decrement */
/*TODO*///		Y-=2;
/*TODO*///		EA=Y;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x3c:				/* indirect - postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=Y+SIGNED(EA);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x3d:				/* indirect - postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=Y;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0x3e:				/* indirect - normal */
/*TODO*///		EA=Y;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*/////	case 0x3f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*/////  case 0x40: EA=0; break; /* auto increment */
/*TODO*/////	case 0x41: EA=0; break; /* double auto increment */
/*TODO*/////	case 0x42: EA=0; break; /* auto decrement */
/*TODO*/////	case 0x43: EA=0; break; /* double auto decrement */
/*TODO*/////	case 0x44: EA=0; break; /* postbyte offs */
/*TODO*/////	case 0x45: EA=0; break; /* postword offs */
/*TODO*/////	case 0x46: EA=0; break; /* normal */
/*TODO*/////	case 0x47: EA=0; break; /* extended */
/*TODO*/////	case 0x48: EA=0; break; /* indirect - auto increment */
/*TODO*/////	case 0x49: EA=0; break; /* indirect - double auto increment */
/*TODO*/////	case 0x4a: EA=0; break; /* indirect - auto decrement */
/*TODO*/////	case 0x4b: EA=0; break; /* indirect - double auto decrement */
/*TODO*/////	case 0x4c: EA=0; break; /* indirect - postbyte offs */
/*TODO*/////	case 0x4d: EA=0; break; /* indirect - postword offs */
/*TODO*/////	case 0x4e: EA=0; break; /* indirect - normal */
/*TODO*/////	case 0x4f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*////* base U */
/*TODO*///    case 0x50:              /* auto increment */
/*TODO*///		EA=U;
/*TODO*///		U++;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x51:				/* double auto increment */
/*TODO*///		EA=U;
/*TODO*///		U+=2;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x52:				/* auto decrement */
/*TODO*///		U--;
/*TODO*///		EA=U;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x53:				/* double auto decrement */
/*TODO*///		U-=2;
/*TODO*///		EA=U;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x54:				/* postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=U+SIGNED(EA);
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x55:				/* postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=U;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x56:				/* normal */
/*TODO*///		EA=U;
/*TODO*///		break;
/*TODO*/////	case 0x57: EA=0; break; /* extended */
/*TODO*///	case 0x58:				/* indirect - auto increment */
/*TODO*///		EA=U;
/*TODO*///		U++;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x59:				/* indirect - double auto increment */
/*TODO*///		EA=U;
/*TODO*///		U+=2;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x5a:				/* indirect - auto decrement */
/*TODO*///		U--;
/*TODO*///		EA=U;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x5b:				/* indirect - double auto decrement */
/*TODO*///		U-=2;
/*TODO*///		EA=U;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x5c:				/* indirect - postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=U+SIGNED(EA);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x5d:				/* indirect - postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=U;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0x5e:				/* indirect - normal */
/*TODO*///		EA=U;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*/////	case 0x5f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*////* base S */
/*TODO*///    case 0x60:              /* auto increment */
/*TODO*///		EAD=SD;
/*TODO*///		S++;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x61:				/* double auto increment */
/*TODO*///		EAD=SD;
/*TODO*///		S+=2;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x62:				/* auto decrement */
/*TODO*///		S--;
/*TODO*///		EAD=SD;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x63:				/* double auto decrement */
/*TODO*///		S-=2;
/*TODO*///		EAD=SD;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x64:				/* postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=S+SIGNED(EA);
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x65:				/* postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=S;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x66:				/* normal */
/*TODO*///		EAD=SD;
/*TODO*///		break;
/*TODO*/////	case 0x67: EA=0; break; /* extended */
/*TODO*///	case 0x68:				/* indirect - auto increment */
/*TODO*///		EAD=SD;
/*TODO*///		S++;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x69:				/* indirect - double auto increment */
/*TODO*///		EAD=SD;
/*TODO*///		S+=2;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x6a:				/* indirect - auto decrement */
/*TODO*///		S--;
/*TODO*///		EAD=SD;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x6b:				/* indirect - double auto decrement */
/*TODO*///		S-=2;
/*TODO*///		EAD=SD;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x6c:				/* indirect - postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=S+SIGNED(EA);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x6d:				/* indirect - postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=S;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0x6e:				/* indirect - normal */
/*TODO*///		EAD=SD;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*/////	case 0x6f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*////* base PC */
/*TODO*///    case 0x70:              /* auto increment */
/*TODO*///		EAD=PCD;
/*TODO*///		PC++;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x71:				/* double auto increment */
/*TODO*///		EAD=PCD;
/*TODO*///		PC+=2;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x72:				/* auto decrement */
/*TODO*///		PC--;
/*TODO*///		EAD=PCD;
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x73:				/* double auto decrement */
/*TODO*///		PC-=2;
/*TODO*///		EAD=PCD;
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*///	case 0x74:				/* postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=PC-1+SIGNED(EA);
/*TODO*///        konami_ICount-=2;
/*TODO*///		break;
/*TODO*///	case 0x75:				/* postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=PC-2;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x76:				/* normal */
/*TODO*///		EAD=PCD;
/*TODO*///		break;
/*TODO*/////	case 0x77: EA=0; break; /* extended */
/*TODO*///	case 0x78:				/* indirect - auto increment */
/*TODO*///		EAD=PCD;
/*TODO*///		PC++;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x79:				/* indirect - double auto increment */
/*TODO*///		EAD=PCD;
/*TODO*///		PC+=2;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x7a:				/* indirect - auto decrement */
/*TODO*///		PC--;
/*TODO*///		EAD=PCD;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=5;
/*TODO*///		break;
/*TODO*///	case 0x7b:				/* indirect - double auto decrement */
/*TODO*///		PC-=2;
/*TODO*///		EAD=PCD;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=6;
/*TODO*///		break;
/*TODO*///	case 0x7c:				/* indirect - postbyte offs */
/*TODO*///		IMMBYTE(EA);
/*TODO*///		EA=PC-1+SIGNED(EA);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0x7d:				/* indirect - postword offs */
/*TODO*///		IMMWORD(ea);
/*TODO*///		EA+=PC-2;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0x7e:				/* indirect - normal */
/*TODO*///		EAD=PCD;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=3;
/*TODO*///		break;
/*TODO*/////	case 0x7f: EA=0; break; /* indirect - extended */
/*TODO*///
/*TODO*/////  case 0x80: EA=0; break; /* register a */
/*TODO*/////	case 0x81: EA=0; break; /* register b */
/*TODO*/////	case 0x82: EA=0; break; /* ???? */
/*TODO*/////	case 0x83: EA=0; break; /* ???? */
/*TODO*/////	case 0x84: EA=0; break; /* ???? */
/*TODO*/////	case 0x85: EA=0; break; /* ???? */
/*TODO*/////	case 0x86: EA=0; break; /* ???? */
/*TODO*/////	case 0x87: EA=0; break; /* register d */
/*TODO*/////	case 0x88: EA=0; break; /* indirect - register a */
/*TODO*/////	case 0x89: EA=0; break; /* indirect - register b */
/*TODO*/////	case 0x8a: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x8b: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x8c: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x8d: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x8e: EA=0; break; /* indirect - register d */
/*TODO*/////	case 0x8f: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x90: EA=0; break; /* register a */
/*TODO*/////	case 0x91: EA=0; break; /* register b */
/*TODO*/////	case 0x92: EA=0; break; /* ???? */
/*TODO*/////	case 0x93: EA=0; break; /* ???? */
/*TODO*/////	case 0x94: EA=0; break; /* ???? */
/*TODO*/////	case 0x95: EA=0; break; /* ???? */
/*TODO*/////	case 0x96: EA=0; break; /* ???? */
/*TODO*/////	case 0x97: EA=0; break; /* register d */
/*TODO*/////	case 0x98: EA=0; break; /* indirect - register a */
/*TODO*/////	case 0x99: EA=0; break; /* indirect - register b */
/*TODO*/////	case 0x9a: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x9b: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x9c: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x9d: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0x9e: EA=0; break; /* indirect - register d */
/*TODO*/////	case 0x9f: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xa0:				/* register a */
/*TODO*///		EA=X+SIGNED(A);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*///	case 0xa1:				/* register b */
/*TODO*///		EA=X+SIGNED(B);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*/////	case 0xa2: EA=0; break; /* ???? */
/*TODO*/////	case 0xa3: EA=0; break; /* ???? */
/*TODO*/////	case 0xa4: EA=0; break; /* ???? */
/*TODO*/////	case 0xa5: EA=0; break; /* ???? */
/*TODO*/////	case 0xa6: EA=0; break; /* ???? */
/*TODO*///	case 0xa7:				/* register d */
/*TODO*///		EA=X+D;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xa8:				/* indirect - register a */
/*TODO*///		EA=X+SIGNED(A);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xa9:				/* indirect - register b */
/*TODO*///		EA=X+SIGNED(B);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0xaa: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xab: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xac: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xad: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xae: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xaf:				/* indirect - register d */
/*TODO*///		EA=X+D;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0xb0:				/* register a */
/*TODO*///		EA=Y+SIGNED(A);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*///	case 0xb1:				/* register b */
/*TODO*///		EA=Y+SIGNED(B);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*/////	case 0xb2: EA=0; break; /* ???? */
/*TODO*/////	case 0xb3: EA=0; break; /* ???? */
/*TODO*/////	case 0xb4: EA=0; break; /* ???? */
/*TODO*/////	case 0xb5: EA=0; break; /* ???? */
/*TODO*/////	case 0xb6: EA=0; break; /* ???? */
/*TODO*///	case 0xb7:				/* register d */
/*TODO*///		EA=Y+D;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xb8:				/* indirect - register a */
/*TODO*///		EA=Y+SIGNED(A);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xb9:				/* indirect - register b */
/*TODO*///		EA=Y+SIGNED(B);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0xba: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xbb: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xbc: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xbd: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xbe: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xbf:				/* indirect - register d */
/*TODO*///		EA=Y+D;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*/////	case 0xc0: EA=0; break; /* register a */
/*TODO*/////	case 0xc1: EA=0; break; /* register b */
/*TODO*/////	case 0xc2: EA=0; break; /* ???? */
/*TODO*/////	case 0xc3: EA=0; break; /* ???? */
/*TODO*///	case 0xc4:
/*TODO*///		EAD=0;
/*TODO*///		(*konami_direct[konami.ireg])();
/*TODO*///        konami_ICount -= 1;
/*TODO*///		return;
/*TODO*/////	case 0xc5: EA=0; break; /* ???? */
/*TODO*/////	case 0xc6: EA=0; break; /* ???? */
/*TODO*/////	case 0xc7: EA=0; break; /* register d */
/*TODO*/////	case 0xc8: EA=0; break; /* indirect - register a */
/*TODO*/////	case 0xc9: EA=0; break; /* indirect - register b */
/*TODO*/////	case 0xca: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xcb: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xcc:				/* indirect - direct */
/*TODO*///		DIRWORD(ea);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0xcd: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xce: EA=0; break; /* indirect - register d */
/*TODO*/////	case 0xcf: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xd0:				/* register a */
/*TODO*///		EA=U+SIGNED(A);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*///	case 0xd1:				/* register b */
/*TODO*///		EA=U+SIGNED(B);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*/////	case 0xd2: EA=0; break; /* ???? */
/*TODO*/////	case 0xd3: EA=0; break; /* ???? */
/*TODO*/////	case 0xd4: EA=0; break; /* ???? */
/*TODO*/////	case 0xd5: EA=0; break; /* ???? */
/*TODO*/////	case 0xd6: EA=0; break; /* ???? */
/*TODO*///	case 0xd7:				/* register d */
/*TODO*///		EA=U+D;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xd8:				/* indirect - register a */
/*TODO*///		EA=U+SIGNED(A);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xd9:				/* indirect - register b */
/*TODO*///		EA=U+SIGNED(B);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0xda: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xdb: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xdc: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xdd: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xde: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xdf:				/* indirect - register d */
/*TODO*///		EA=U+D;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///        break;
/*TODO*///	case 0xe0:				/* register a */
/*TODO*///		EA=S+SIGNED(A);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*///	case 0xe1:				/* register b */
/*TODO*///		EA=S+SIGNED(B);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*/////	case 0xe2: EA=0; break; /* ???? */
/*TODO*/////	case 0xe3: EA=0; break; /* ???? */
/*TODO*/////	case 0xe4: EA=0; break; /* ???? */
/*TODO*/////	case 0xe5: EA=0; break; /* ???? */
/*TODO*/////	case 0xe6: EA=0; break; /* ???? */
/*TODO*///	case 0xe7:				/* register d */
/*TODO*///		EA=S+D;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xe8:				/* indirect - register a */
/*TODO*///		EA=S+SIGNED(A);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xe9:				/* indirect - register b */
/*TODO*///		EA=S+SIGNED(B);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0xea: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xeb: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xec: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xed: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xee: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xef:				/* indirect - register d */
/*TODO*///		EA=S+D;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	case 0xf0:				/* register a */
/*TODO*///		EA=PC+SIGNED(A);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*///	case 0xf1:				/* register b */
/*TODO*///		EA=PC+SIGNED(B);
/*TODO*///        konami_ICount-=1;
/*TODO*///		break;
/*TODO*/////	case 0xf2: EA=0; break; /* ???? */
/*TODO*/////	case 0xf3: EA=0; break; /* ???? */
/*TODO*/////	case 0xf4: EA=0; break; /* ???? */
/*TODO*/////	case 0xf5: EA=0; break; /* ???? */
/*TODO*/////	case 0xf6: EA=0; break; /* ???? */
/*TODO*///	case 0xf7:				/* register d */
/*TODO*///		EA=PC+D;
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xf8:				/* indirect - register a */
/*TODO*///		EA=PC+SIGNED(A);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*///	case 0xf9:				/* indirect - register b */
/*TODO*///		EA=PC+SIGNED(B);
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=4;
/*TODO*///		break;
/*TODO*/////	case 0xfa: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xfb: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xfc: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xfd: EA=0; break; /* indirect - ???? */
/*TODO*/////	case 0xfe: EA=0; break; /* indirect - ???? */
/*TODO*///	case 0xff:				/* indirect - register d */
/*TODO*///		EA=PC+D;
/*TODO*///		EA=RM16(EAD);
/*TODO*///        konami_ICount-=7;
/*TODO*///		break;
/*TODO*///	default:
/*TODO*///		logerror("KONAMI: Unknown/Invalid postbyte at PC = %04x\n", PC -1 );
/*TODO*///        EAD = 0;
/*TODO*///	}
/*TODO*///	(*konami_indexed[konami.ireg])();
/*TODO*///}
/*TODO*/// 
}
