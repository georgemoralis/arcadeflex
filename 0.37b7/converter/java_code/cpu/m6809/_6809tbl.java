INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE INLINE 
INLINE INLINE 
#if (BIG_SWITCH==0)
static void (*m6809_main[0x100])(void) = {
	neg_di, illegal,illegal,com_di, lsr_di, illegal,ror_di, asr_di, 	/* 00 */
	asl_di, rol_di, dec_di, illegal,inc_di, tst_di, jmp_di, clr_di,
	pref10, pref11, nop,	sync,	illegal,illegal,lbra,	lbsr,		/* 10 */
	illegal,daa,	orcc,	illegal,andcc,	sex,	exg,	tfr,
	bra,	brn,	bhi,	bls,	bcc,	bcs,	bne,	beq,		/* 20 */
	bvc,	bvs,	bpl,	bmi,	bge,	blt,	bgt,	ble,
	leax,	leay,	leas,	leau,	pshs,	puls,	pshu,	pulu,		/* 30 */
	illegal,rts,	abx,	rti,	cwai,	mul,	illegal,swi,
	nega,	illegal,illegal,coma,	lsra,	illegal,rora,	asra,		/* 40 */
	asla,	rola,	deca,	illegal,inca,	tsta,	illegal,clra,
	negb,	illegal,illegal,comb,	lsrb,	illegal,rorb,	asrb,		/* 50 */
	aslb,	rolb,	decb,	illegal,incb,	tstb,	illegal,clrb,
	neg_ix, illegal,illegal,com_ix, lsr_ix, illegal,ror_ix, asr_ix, 	/* 60 */
	asl_ix, rol_ix, dec_ix, illegal,inc_ix, tst_ix, jmp_ix, clr_ix,
	neg_ex, illegal,illegal,com_ex, lsr_ex, illegal,ror_ex, asr_ex, 	/* 70 */
	asl_ex, rol_ex, dec_ex, illegal,inc_ex, tst_ex, jmp_ex, clr_ex,
	suba_im,cmpa_im,sbca_im,subd_im,anda_im,bita_im,lda_im, sta_im, 	/* 80 */
	eora_im,adca_im,ora_im, adda_im,cmpx_im,bsr,	ldx_im, stx_im,
	suba_di,cmpa_di,sbca_di,subd_di,anda_di,bita_di,lda_di, sta_di, 	/* 90 */
	eora_di,adca_di,ora_di, adda_di,cmpx_di,jsr_di, ldx_di, stx_di,
	suba_ix,cmpa_ix,sbca_ix,subd_ix,anda_ix,bita_ix,lda_ix, sta_ix, 	/* a0 */
	eora_ix,adca_ix,ora_ix, adda_ix,cmpx_ix,jsr_ix, ldx_ix, stx_ix,
	suba_ex,cmpa_ex,sbca_ex,subd_ex,anda_ex,bita_ex,lda_ex, sta_ex, 	/* b0 */
	eora_ex,adca_ex,ora_ex, adda_ex,cmpx_ex,jsr_ex, ldx_ex, stx_ex,
	subb_im,cmpb_im,sbcb_im,addd_im,andb_im,bitb_im,ldb_im, stb_im, 	/* c0 */
	eorb_im,adcb_im,orb_im, addb_im,ldd_im, std_im, ldu_im, stu_im,
	subb_di,cmpb_di,sbcb_di,addd_di,andb_di,bitb_di,ldb_di, stb_di, 	/* d0 */
	eorb_di,adcb_di,orb_di, addb_di,ldd_di, std_di, ldu_di, stu_di,
	subb_ix,cmpb_ix,sbcb_ix,addd_ix,andb_ix,bitb_ix,ldb_ix, stb_ix, 	/* e0 */
	eorb_ix,adcb_ix,orb_ix, addb_ix,ldd_ix, std_ix, ldu_ix, stu_ix,
	subb_ex,cmpb_ex,sbcb_ex,addd_ex,andb_ex,bitb_ex,ldb_ex, stb_ex, 	/* f0 */
	eorb_ex,adcb_ex,orb_ex, addb_ex,ldd_ex, std_ex, ldu_ex, stu_ex
};
#endif
