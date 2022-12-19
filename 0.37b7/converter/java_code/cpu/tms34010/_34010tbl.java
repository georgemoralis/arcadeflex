/*** TMS34010: Portable TMS34010 emulator ***********************************

	Copyright (C) Alex Pasadyn/Zsolt Vasvari 1998

    Opcode Table

*****************************************************************************/

static 
/* Graphics Instructions */

static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static 
/* General Instructions */
static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static 

/* Move Instructions */
static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static 

/* Program Control and Context Switching */
static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static 

/* 34020 instructions */
static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static static 

/* Opcode Table */
static void (*opcode_table[65536 >> 4])(void) =
{
	/* 0x0000 0x0010 0x0020 0x0030 ... 0x00f0 */
	unimpl,		unimpl,		rev_a,		rev_b,		idle,		unimpl,		unimpl,		unimpl,
	mwait,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		blmove,
	/* 0x0100 */
	emu,		unimpl, 	exgpc_a,	exgpc_b,	getpc_a,	getpc_b,	jump_a,		jump_b,
	getst_a,	getst_b,	putst_a,	putst_b,	popst,		unimpl,		pushst,		unimpl,
	/* 0x0200 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		setcsp,		unimpl,		setcdp,
	rpix_a,		rpix_b,		exgps_a,	exgps_b,	getps_a,	getps_b,	unimpl,		setcmp,
	/* 0x0300 */
	nop,		unimpl,		clrc,		unimpl,		movb_aa,	unimpl,		dint,		unimpl,
	abs_a,		abs_b,		neg_a,		neg_b,		negb_a,		negb_b,		not_a,		not_b,
	/* 0x0400 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x0500 */
	sext0_a,	sext0_b,	zext0_a,	zext0_b,	setf0,		setf0,		setf0,		setf0,
	move0_ra_a,	move0_ra_b,	move0_ar_a,	move0_ar_b,	move0_aa,	unimpl,		movb_ra_a,	movb_ra_b,
	/* 0x0600 */
	cexec_l,	unimpl,		cmovgc_a,	cmovgc_b,	cmovgc_a_s,	cmovgc_b_s,	cmovcg_a,	cmovcg_b,
	cmovmc_f,	cmovmc_f,	cmovcm_f,	cmovcm_f,	cmovcm_b,	cmovcm_b,	cmovmc_f_va,cmovmc_f_vb,
	/* 0x0700 */
	sext1_a,	sext1_b,	zext1_a,	zext1_b,	setf1,		setf1,		setf1,		setf1,
	move1_ra_a,	move1_ra_b,	move1_ar_a,	move1_ar_b,	move1_aa,	unimpl,		movb_ar_a,	movb_ar_b,
	/* 0x0800 */
	trapl,		unimpl,		cmovmc_b,	cmovmc_b,	unimpl,		vblt_b_l,	retm,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		clip,
	/* 0x0900 */
	trap,		trap,		call_a,		call_b,		reti,		unimpl,		rets,		rets,
	mmtm_a,		mmtm_b,		mmfm_a,		mmfm_b,		movi_w_a,	movi_w_b,	movi_l_a,	movi_l_b,
	/* 0x0a00 */
	vlcol,		unimpl,		unimpl,		pfill_xy,	unimpl,		vfill_l,	cvmxyl_a,	cvmxyl_b,
	cvdxyl_a,	cvdxyl_b,	unimpl,		fpixeq,		unimpl,		fpixne,		unimpl,		unimpl,
	/* 0x0b00 */
	addi_w_a,	addi_w_b,	addi_l_a,	addi_l_b,	cmpi_w_a,	cmpi_w_b,	cmpi_l_a,	cmpi_l_b,
	andi_a,		andi_b,		ori_a,		ori_b,		xori_a,		xori_b,		subi_w_a,	subi_w_b,
	/* 0x0c00 */
	addxyi_a,	addxyi_b,	unimpl,		unimpl,		unimpl,		linit,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x0d00 */
	subi_l_a,	subi_l_b,	unimpl,		callr,		unimpl,		calla,		eint,		unimpl,
	dsj_a,		dsj_b,		dsjeq_a,	dsjeq_b,	dsjne_a,	dsjne_b,	setc,		unimpl,
	/* 0x0e00 */
	unimpl,		pixblt_l_m_l,unimpl,	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		tfill_xy,
	/* 0x0f00 */
	pixblt_l_l,	unimpl,		pixblt_l_xy,unimpl,		pixblt_xy_l,unimpl,		pixblt_xy_xy,unimpl,
	pixblt_b_l,	unimpl,		pixblt_b_xy,unimpl,		fill_l,		unimpl,		fill_xy,	unimpl,
	/* 0x1000 */
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	/* 0x1100 */
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	/* 0x1200 */
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	/* 0x1300 */
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,		addk_a,		addk_b,
	/* 0x1400 */
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	/* 0x1500 */
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	/* 0x1600 */
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	/* 0x1700 */
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,		subk_a,		subk_b,
	/* 0x1800 */
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	/* 0x1900 */
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	/* 0x1a00 */
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	/* 0x1b00 */
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,		movk_a,		movk_b,
	/* 0x1c00 */
	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	/* 0x1d00 */
	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	/* 0x1e00 */
	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	/* 0x1f00 */
	btst_k_a,	btst_k_b, 	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	btst_k_a,	btst_k_b, 	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,	btst_k_a,	btst_k_b,
	/* 0x2000 */
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	/* 0x2100 */
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	/* 0x2200 */
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	/* 0x2300 */
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,	sla_k_a,	sla_k_b,
	/* 0x2400 */
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	/* 0x2500 */
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	/* 0x2600 */
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	/* 0x2700 */
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,	sll_k_a,	sll_k_b,
	/* 0x2800 */
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	/* 0x2900 */
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	/* 0x2a00 */
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	/* 0x2b00 */
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,	sra_k_a,	sra_k_b,
	/* 0x2c00 */
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	/* 0x2d00 */
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	/* 0x2e00 */
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	/* 0x2f00 */
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,	srl_k_a,	srl_k_b,
	/* 0x3000 */
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	/* 0x3100 */
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	/* 0x3200 */
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	/* 0x3300 */
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,		rl_k_a,		rl_k_b,
	/* 0x3400 */
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	/* 0x3500 */
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	/* 0x3600 */
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	/* 0x3700 */
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,	cmp_k_a,	cmp_k_b,
	/* 0x3800 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3900 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3a00 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3b00 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3c00 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3d00 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3e00 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x3f00 */
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,		dsjs_a,		dsjs_b,
	/* 0x4000 */
	add_a,		add_b,		add_a,		add_b,		add_a,		add_b,		add_a,		add_b,
	add_a,		add_b,		add_a,		add_b,		add_a,		add_b,		add_a,		add_b,
	/* 0x4100 */
	add_a,		add_b,		add_a,		add_b,		add_a,		add_b,		add_a,		add_b,
	add_a,		add_b,		add_a,		add_b,		add_a,		add_b,		add_a,		add_b,
	/* 0x4200 */
	addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,
	addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,
	/* 0x4300 */
	addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,
	addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,		addc_a,		addc_b,
	/* 0x4400 */
	sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,
	sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,
	/* 0x4500 */
	sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,
	sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,		sub_a,		sub_b,
	/* 0x4600 */
	subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,
	subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,
	/* 0x4700 */
	subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,
	subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,		subb_a,		subb_b,
	/* 0x4800 */
	cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,
	cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,
	/* 0x4900 */
	cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,
	cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,		cmp_a,		cmp_b,
	/* 0x4a00 */
	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,
	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,
	/* 0x4b00 */
	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,
	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,	btst_r_a,	btst_r_b,
	/* 0x4c00 */
	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,
	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,
	/* 0x4d00 */
	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,
	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,	move_rr_a,	move_rr_b,
	/* 0x4e00 */
	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,
	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,
	/* 0x4f00 */
	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,
	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,	move_rr_ax,	move_rr_bx,
	/* 0x5000 */
	and_a,		and_b,		and_a,		and_b,		and_a,		and_b,		and_a,		and_b,
	and_a,		and_b,		and_a,		and_b,		and_a,		and_b,		and_a,		and_b,
	/* 0x5100 */
	and_a,		and_b,		and_a,		and_b,		and_a,		and_b,		and_a,		and_b,
	and_a,		and_b,		and_a,		and_b,		and_a,		and_b,		and_a,		and_b,
	/* 0x5200 */
	andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,
	andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,
	/* 0x5300 */
	andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,
	andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,		andn_a,		andn_b,
	/* 0x5400 */
	or_a,		or_b,		or_a,		or_b,		or_a,		or_b,		or_a,		or_b,
	or_a,		or_b,		or_a,		or_b,		or_a,		or_b,		or_a,		or_b,
	/* 0x5500 */
	or_a,		or_b,		or_a,		or_b,		or_a,		or_b,		or_a,		or_b,
	or_a,		or_b,		or_a,		or_b,		or_a,		or_b,		or_a,		or_b,
	/* 0x5600 */
	xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,
	xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,
	/* 0x5700 */
	xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,
	xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,		xor_a,		xor_b,
	/* 0x5800 */
	divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,
	divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,
	/* 0x5900 */
	divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,
	divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,		divs_a,		divs_b,
	/* 0x5a00 */
	divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,
	divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,
	/* 0x5b00 */
	divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,
	divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,		divu_a,		divu_b,
	/* 0x5c00 */
	mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,
	mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,
	/* 0x5d00 */
	mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,
	mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,		mpys_a,		mpys_b,
	/* 0x5e00 */
	mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,
	mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,
	/* 0x5f00 */
	mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,
	mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,		mpyu_a,		mpyu_b,
	/* 0x6000 */
	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,
	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,
	/* 0x6100 */
	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,
	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,	sla_r_a,	sla_r_b,
	/* 0x6200 */
	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,
	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,
	/* 0x6300 */
	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,
	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,	sll_r_a,	sll_r_b,
	/* 0x6400 */
	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,
	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,
	/* 0x6500 */
	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,
	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,	sra_r_a,	sra_r_b,
	/* 0x6600 */
	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,
	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,
	/* 0x6700 */
	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,
	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,	srl_r_a,	srl_r_b,
	/* 0x6800 */
	rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,
	rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,
	/* 0x6900 */
	rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,
	rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,		rl_r_a,		rl_r_b,
	/* 0x6a00 */
	lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,
	lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,
	/* 0x6b00 */
	lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,
	lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,		lmo_a,		lmo_b,
	/* 0x6c00 */
	mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,
	mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,
	/* 0x6d00 */
	mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,
	mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,		mods_a,		mods_b,
	/* 0x6e00 */
	modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,
	modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,
	/* 0x6f00 */
	modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,
	modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,		modu_a,		modu_b,
	/* 0x7000 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7100 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7200 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7300 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7400 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7500 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7600 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7700 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7800 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7900 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7a00 */
	rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,
	rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,
	/* 0x7b00 */
	rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,
	rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,		rmo_a,		rmo_b,
	/* 0x7c00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7d00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x7e00 */
	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,
	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,
	/* 0x7f00 */
	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,
	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,	swapf_a,	swapf_b,
	/* 0x8000 */
	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,
	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,
	/* 0x8100 */
	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,
	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,	move0_rn_a,	move0_rn_b,
	/* 0x8200 */
	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,
	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,
	/* 0x8300 */
	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,
	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,	move1_rn_a,	move1_rn_b,
	/* 0x8400 */
	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,
	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,
	/* 0x8500 */
	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,
	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,	move0_nr_a,	move0_nr_b,
	/* 0x8600 */
	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,
	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,
	/* 0x8700 */
	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,
	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,	move1_nr_a,	move1_nr_b,
	/* 0x8800 */
	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,
	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,
	/* 0x8900 */
	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,
	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,	move0_nn_a,	move0_nn_b,
	/* 0x8a00 */
	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,
	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,
	/* 0x8b00 */
	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,
	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,	move1_nn_a,	move1_nn_b,
	/* 0x8c00 */
	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,
	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,
	/* 0x8d00 */
	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,
	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,	movb_rn_a,	movb_rn_b,
	/* 0x8e00 */
	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,
	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,
	/* 0x8f00 */
	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,
	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,	movb_nr_a,	movb_nr_b,
	/* 0x9000 */
	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,
	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,
	/* 0x9100 */
	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,
	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,	move0_r_ni_a,	move0_r_ni_b,
	/* 0x9200 */
	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,
	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,
	/* 0x9300 */
	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,
	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,	move1_r_ni_a,	move1_r_ni_b,
	/* 0x9400 */
	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,
	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,
	/* 0x9500 */
	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,
	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,	move0_ni_r_a,	move0_ni_r_b,
	/* 0x9600 */
	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,
	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,
	/* 0x9700 */
	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,
	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,	move1_ni_r_a,	move1_ni_r_b,
	/* 0x9800 */
	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,
	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,
	/* 0x9900 */
	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,
	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,	move0_ni_ni_a,	move0_ni_ni_b,
	/* 0x9a00 */
	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,
	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,
	/* 0x9b00 */
	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,
	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,	move1_ni_ni_a,	move1_ni_ni_b,
	/* 0x9c00 */
	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,
	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,
	/* 0x9d00 */
	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,
	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,	movb_nn_a,	movb_nn_b,
	/* 0x9e00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0x9f00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xa000 */
	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,
	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,
	/* 0xa100 */
	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,
	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,	move0_r_dn_a,	move0_r_dn_b,
	/* 0xa200 */
	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,
	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,
	/* 0xa300 */
	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,
	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,	move1_r_dn_a,	move1_r_dn_b,
	/* 0xa400 */
	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,
	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,
	/* 0xa500 */
	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,
	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,	move0_dn_r_a,	move0_dn_r_b,
	/* 0xa600 */
	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,
	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,
	/* 0xa700 */
	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,
	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,	move1_dn_r_a,	move1_dn_r_b,
	/* 0xa800 */
	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,
	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,
	/* 0xa900 */
	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,
	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,	move0_dn_dn_a,	move0_dn_dn_b,
	/* 0xaa00 */
	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,
	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,
	/* 0xab00 */
	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,
	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,	move1_dn_dn_a,	move1_dn_dn_b,
	/* 0xac00 */
	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,
	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,
	/* 0xad00 */
	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,
	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,	movb_r_no_a,	movb_r_no_b,
	/* 0xae00 */
	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,
	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,
	/* 0xaf00 */
	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,
	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,	movb_no_r_a,	movb_no_r_b,
	/* 0xb000 */
	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,
	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,
	/* 0xb100 */
	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,
	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,	move0_r_no_a,	move0_r_no_b,
	/* 0xb200 */
	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,
	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,
	/* 0xb300 */
	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,
	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,	move1_r_no_a,	move1_r_no_b,
	/* 0xb400 */
	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,
	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,
	/* 0xb500 */
	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,
	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,	move0_no_r_a,	move0_no_r_b,
	/* 0xb600 */
	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,
	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,
	/* 0xb700 */
	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,
	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,	move1_no_r_a,	move1_no_r_b,
	/* 0xb800 */
	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,
	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,
	/* 0xb900 */
	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,
	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,	move0_no_no_a,	move0_no_no_b,
	/* 0xba00 */
	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,
	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,
	/* 0xbb00 */
	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,
	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,	move1_no_no_a,	move1_no_no_b,
	/* 0xbc00 */
	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,
	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,
	/* 0xbd00 */
	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,
	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,	movb_no_no_b,	movb_no_no_a,		movb_no_no_b,
	/* 0xbe00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xbf00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xc000 */
	j_UC_0,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,
	j_UC_8,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,		j_UC_x,
	/* 0xc100 */
	j_P_0,		j_P_x,		j_P_x,		j_P_x,		j_P_x,		j_P_x,		j_P_x,		j_P_x,
	j_P_8,		j_P_x,		j_P_x,		j_P_x,		j_P_x,		j_P_x,		j_P_x,		j_P_x,
	/* 0xc200 */
	j_LS_0,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,
	j_LS_8,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,		j_LS_x,
	/* 0xc300 */
	j_HI_0,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,
	j_HI_8,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,		j_HI_x,
	/* 0xc400 */
	j_LT_0,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,
	j_LT_8,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,		j_LT_x,
	/* 0xc500 */
	j_GE_0,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,
	j_GE_8,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,		j_GE_x,
	/* 0xc600 */
	j_LE_0,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,
	j_LE_8,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,		j_LE_x,
	/* 0xc700 */
	j_GT_0,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,
	j_GT_8,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,		j_GT_x,
	/* 0xc800 */
	j_C_0,		j_C_x,		j_C_x,		j_C_x,		j_C_x,		j_C_x,		j_C_x,		j_C_x,
	j_C_8,		j_C_x,		j_C_x,		j_C_x,		j_C_x,		j_C_x,		j_C_x,		j_C_x,
	/* 0xc900 */
	j_NC_0,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,
	j_NC_8,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,		j_NC_x,
	/* 0xca00 */
	j_EQ_0,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,
	j_EQ_8,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,		j_EQ_x,
	/* 0xcb00 */
	j_NE_0,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,
	j_NE_8,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,		j_NE_x,
	/* 0xcc00 */
	j_V_0,		j_V_x,		j_V_x,		j_V_x,		j_V_x,		j_V_x,		j_V_x,		j_V_x,
	j_V_8,		j_V_x,		j_V_x,		j_V_x,		j_V_x,		j_V_x,		j_V_x,		j_V_x,
	/* 0xcd00 */
	j_NV_0,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,
	j_NV_8,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,		j_NV_x,
	/* 0xce00 */
	j_N_0,		j_N_x,		j_N_x,		j_N_x,		j_N_x,		j_N_x,		j_N_x,		j_N_x,
	j_N_8,		j_N_x,		j_N_x,		j_N_x,		j_N_x,		j_N_x,		j_N_x,		j_N_x,
	/* 0xcf00 */
	j_NN_0,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,
	j_NN_8,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,		j_NN_x,
	/* 0xd000 */
	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,
	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,
	/* 0xd100 */
	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,
	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,	move0_no_ni_a,	move0_no_ni_b,
	/* 0xd200 */
	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,
	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,
	/* 0xd300 */
	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,
	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,	move1_no_ni_a,	move1_no_ni_b,
	/* 0xd400 */
	move0_a_ni_a,move0_a_ni_b,unimpl,	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xd500 */
	exgf0_a,	exgf0_b,	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xd600 */
	move1_a_ni_a,move1_a_ni_b,unimpl,	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xd700 */
	exgf1_a,	exgf1_b,	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xd800 */
	cexec_s,	cexec_s,	cexec_s,	cexec_s,	cexec_s,	cexec_s,	cexec_s,	cexec_s,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xd900 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xda00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xdb00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xdc00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xdd00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xde00 */
	unimpl,		fline,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		fline,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xdf00 */
	unimpl,		line,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		line,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xe000 */
	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,
	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,
	/* 0xe100 */
	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,
	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,	add_xy_a,	add_xy_b,
	/* 0xe200 */
	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,
	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,
	/* 0xe300 */
	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,
	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,	sub_xy_a,	sub_xy_b,
	/* 0xe400 */
	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,
	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,
	/* 0xe500 */
	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,
	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,	cmp_xy_a,	cmp_xy_b,
	/* 0xe600 */
	cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,
	cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,
	/* 0xe700 */
	cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,
	cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,		cpw_a,		cpw_b,
	/* 0xe800 */
	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,
	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,
	/* 0xe900 */
	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,
	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,	cvxyl_a,	cvxyl_b,
	/* 0xea00 */
	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,
	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,
	/* 0xeb00 */
	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,
	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,	cvsxyl_a,	cvsxyl_b,
	/* 0xec00 */
	movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,
	movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,
	/* 0xed00 */
	movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,
	movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,		movx_a,		movx_b,
	/* 0xee00 */
	movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,
	movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,
	/* 0xef00 */
	movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,
	movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,		movy_a,		movy_b,
	/* 0xf000 */
	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,
	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,
	/* 0xf100 */
	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,
	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,	pixt_rixy_a,	pixt_rixy_b,
	/* 0xf200 */
	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,
	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,
	/* 0xf300 */
	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,
	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,	pixt_ixyr_a,	pixt_ixyr_b,
	/* 0xf400 */
	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,
	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,
	/* 0xf500 */
	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,
	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,	pixt_ixyixy_a,	pixt_ixyixy_b,
	/* 0xf600 */
	drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,
	drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,
	/* 0xf700 */
	drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,
	drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,		drav_a,		drav_b,
	/* 0xf800 */
	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,
	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,
	/* 0xf900 */
	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,
	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,	pixt_ri_a,	pixt_ri_b,
	/* 0xfa00 */
	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,
	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,
	/* 0xfb00 */
	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,
	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,	pixt_ir_a,	pixt_ir_b,
	/* 0xfc00 */
	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,
	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,
	/* 0xfd00 */
	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,
	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,	pixt_ii_a,	pixt_ii_b,
	/* 0xfe00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	/* 0xff00 */
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,
	unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl,		unimpl
};
