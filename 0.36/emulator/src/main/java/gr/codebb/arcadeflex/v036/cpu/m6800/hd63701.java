package gr.codebb.arcadeflex.v036.cpu.m6800;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static gr.codebb.arcadeflex.v036.cpu.m6800.m6800H.*;
import static gr.codebb.arcadeflex.v036.cpu.m6800.m6803.*;

public class hd63701 extends m6800 {

    public static final int HD63701_DDR1 = M6803_DDR1;
    public static final int HD63701_DDR2 = M6803_DDR2;

    public static final int HD63701_PORT1 = M6803_PORT1;
    public static final int HD63701_PORT2 = M6803_PORT2;

    public static ReadHandlerPtr hd63701_internal_registers_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return m6803_internal_registers_r.handler(offset);
        }
    };
    public static WriteHandlerPtr hd63701_internal_registers_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            m6803_internal_registers_w.handler(offset, data);
        }
    };

    public hd63701() {
        cpu_num = CPU_HD63701;
        num_irqs = 1;
        default_vector = 0;
        overclock = 1.0;
        no_int = M6800_INT_NONE;
        irq_int = M6800_INT_IRQ;
        nmi_int = M6800_INT_NMI;
        address_shift = 0;
        address_bits = 16;
        endianess = CPU_IS_BE;
        align_unit = 1;
        max_inst_len = 4;
        abits1 = ABITS1_16;
        abits2 = ABITS2_16;
        abitsmin = ABITS_MIN_16;
        icount = m6800_ICount;
        icount[0] = 50000;
    }

    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "HD63701";
            case CPU_INFO_FAMILY:
                return "Motorola 6800";
            case CPU_INFO_VERSION:
                return "1.1";
            case CPU_INFO_FILE:
                return "m6800.c";
            case CPU_INFO_CREDITS:
                return "The MAME team.";
        }
        throw new UnsupportedOperationException("Unsupported");
    }

    public void reset(Object param) {
        super.reset(param);
        m6800.insn = hd63701_insn;
        m6800.cycles = cycles_63701;
    }
    static int cycles_63701[]
            = {
                /* 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F */
                /*0*/0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
                /*1*/ 1, 1, 0, 0, 0, 0, 1, 1, 2, 2, 4, 1, 0, 0, 0, 0,
                /*2*/ 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
                /*3*/ 1, 1, 3, 3, 1, 1, 4, 4, 4, 5, 1, 10, 5, 7, 9, 12,
                /*4*/ 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1,
                /*5*/ 1, 0, 0, 1, 1, 0, 1, 1, 1, 1, 1, 0, 1, 1, 0, 1,
                /*6*/ 6, 7, 7, 6, 6, 7, 6, 6, 6, 6, 6, 5, 6, 4, 3, 5,
                /*7*/ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 4, 6, 4, 3, 5,
                /*8*/ 2, 2, 2, 3, 2, 2, 2, 0, 2, 2, 2, 2, 3, 5, 3, 0,
                /*9*/ 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 4, 5, 4, 4,
                /*A*/ 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
                /*B*/ 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 6, 5, 5,
                /*C*/ 2, 2, 2, 3, 2, 2, 2, 0, 2, 2, 2, 2, 3, 0, 3, 0,
                /*D*/ 3, 3, 3, 4, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4,
                /*E*/ 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
                /*F*/ 4, 4, 4, 5, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5
            };
    public opcode[] hd63701_insn = {
        trap, nop, trap, trap, lsrd, asld, tap, tpa,
        inx, dex, clv, sev, clc, sec, cli, sei,
        sba, cba, undoc1, undoc2, trap, trap, tab, tba,
        xgdx, daa, slp, aba, trap, trap, trap, trap,
        bra, brn, bhi, bls, bcc, bcs, bne, beq,
        bvc, bvs, bpl, bmi, bge, blt, bgt, ble,
        tsx, ins, pula, pulb, des, txs, psha, pshb,
        pulx, rts, abx, rti, pshx, mul, wai, swi,
        nega, trap, trap, coma, lsra, trap, rora, asra,
        asla, rola, deca, trap, inca, tsta, trap, clra,
        negb, trap, trap, comb, lsrb, trap, rorb, asrb,
        aslb, rolb, decb, trap, incb, tstb, trap, clrb,
        neg_ix, aim_ix, oim_ix, com_ix, lsr_ix, eim_ix, ror_ix, asr_ix,
        asl_ix, rol_ix, dec_ix, tim_ix, inc_ix, tst_ix, jmp_ix, clr_ix,
        neg_ex, aim_di, oim_di, com_ex, lsr_ex, eim_di, ror_ex, asr_ex,
        asl_ex, rol_ex, dec_ex, tim_di, inc_ex, tst_ex, jmp_ex, clr_ex,
        suba_im, cmpa_im, sbca_im, subd_im, anda_im, bita_im, lda_im, sta_im,
        eora_im, adca_im, ora_im, adda_im, cpx_im, bsr, lds_im, sts_im,
        suba_di, cmpa_di, sbca_di, subd_di, anda_di, bita_di, lda_di, sta_di,
        eora_di, adca_di, ora_di, adda_di, cpx_di, jsr_di, lds_di, sts_di,
        suba_ix, cmpa_ix, sbca_ix, subd_ix, anda_ix, bita_ix, lda_ix, sta_ix,
        eora_ix, adca_ix, ora_ix, adda_ix, cpx_ix, jsr_ix, lds_ix, sts_ix,
        suba_ex, cmpa_ex, sbca_ex, subd_ex, anda_ex, bita_ex, lda_ex, sta_ex,
        eora_ex, adca_ex, ora_ex, adda_ex, cpx_ex, jsr_ex, lds_ex, sts_ex,
        subb_im, cmpb_im, sbcb_im, addd_im, andb_im, bitb_im, ldb_im, stb_im,
        eorb_im, adcb_im, orb_im, addb_im, ldd_im, std_im, ldx_im, stx_im,
        subb_di, cmpb_di, sbcb_di, addd_di, andb_di, bitb_di, ldb_di, stb_di,
        eorb_di, adcb_di, orb_di, addb_di, ldd_di, std_di, ldx_di, stx_di,
        subb_ix, cmpb_ix, sbcb_ix, addd_ix, andb_ix, bitb_ix, ldb_ix, stb_ix,
        eorb_ix, adcb_ix, orb_ix, addb_ix, ldd_ix, std_ix, ldx_ix, stx_ix,
        subb_ex, cmpb_ex, sbcb_ex, addd_ex, andb_ex, bitb_ex, ldb_ex, stb_ex,
        eorb_ex, adcb_ex, orb_ex, addb_ex, ldd_ex, std_ex, ldx_ex, stx_ex
    };

    void hd63701_trap_pc() {
        TAKE_TRAP();
    }

    public int execute(int cycles) {
        int ireg;
         m6800_ICount[0] = cycles;
        CLEANUP_conters();
        INCREMENT_COUNTER(m6800.extra_cycles);
        m6800.extra_cycles = 0;

        if ((m6800.wai_state & (M6800_WAI | M6800_SLP))!=0) {
            EAT_CYCLES();
            INCREMENT_COUNTER(m6800.extra_cycles);
        m6800.extra_cycles = 0;

        return cycles - m6800_ICount[0];
        }

        do {
            m6800.ppc = m6800.pc;//pPPC = pPC;
            //CALL_MAME_DEBUG;
            ireg = M_RDOP(m6800.pc);
            m6800.pc = (m6800.pc + 1) & 0xFFFF;

            switch (ireg) {
                case 0x00:
                    trap.handler();
                    break;
                case 0x01:
                    nop.handler();
                    break;
                case 0x02:
                    trap.handler();
                    break;
                case 0x03:
                    trap.handler();
                    break;
                case 0x04:
                    lsrd.handler(); /* 6803 only */
                    break;
                case 0x05:
                    asld.handler(); /* 6803 only */
                    break;
                case 0x06:
                    tap.handler();
                    break;
                case 0x07:
                    tpa.handler();
                    break;
                case 0x08:
                    inx.handler();
                    break;
                case 0x09:
                    dex.handler();
                    break;
                case 0x0A:
                    CLV();
                    break;
                case 0x0B:
                    SEV();
                    break;
                case 0x0C:
                    CLC();
                    break;
                case 0x0D:
                    SEC();
                    break;
                case 0x0E:
                    cli.handler();
                    break;
                case 0x0F:
                    sei.handler();
                    break;
                case 0x10:
                    sba.handler();
                    break;
                case 0x11:
                    cba.handler();
                    break;
                case 0x12:
                    undoc1.handler();
                    break;
                case 0x13:
                    undoc2.handler();
                    break;
                case 0x14:
                    trap.handler();
                    break;
                case 0x15:
                    trap.handler();
                    break;
                case 0x16:
                    tab.handler();
                    break;
                case 0x17:
                    tba.handler();
                    break;
                case 0x18:
                    xgdx.handler(); /* HD63701YO only */
                    break;
                case 0x19:
                    daa.handler();
                    break;
                case 0x1a:
                    slp.handler();
                    break;
                case 0x1b:
                    aba.handler();
                    break;
                case 0x1c:
                    trap.handler();
                    break;
                case 0x1d:
                    trap.handler();
                    break;
                case 0x1e:
                    trap.handler();
                    break;
                case 0x1f:
                    trap.handler();
                    break;
                case 0x20:
                    bra.handler();
                    break;
                case 0x21:
                    brn.handler();
                    break;
                case 0x22:
                    bhi.handler();
                    break;
                case 0x23:
                    bls.handler();
                    break;
                case 0x24:
                    bcc.handler();
                    break;
                case 0x25:
                    bcs.handler();
                    break;
                case 0x26:
                    bne.handler();
                    break;
                case 0x27:
                    beq.handler();
                    break;
                case 0x28:
                    bvc.handler();
                    break;
                case 0x29:
                    bvs.handler();
                    break;
                case 0x2a:
                    bpl.handler();
                    break;
                case 0x2b:
                    bmi.handler();
                    break;
                case 0x2c:
                    bge.handler();
                    break;
                case 0x2d:
                    blt.handler();
                    break;
                case 0x2e:
                    bgt.handler();
                    break;
                case 0x2f:
                    ble.handler();
                    break;
                case 0x30:
                    tsx.handler();
                    break;
                case 0x31:
                    ins.handler();
                    break;
                case 0x32:
                    pula.handler();
                    break;
                case 0x33:
                    pulb.handler();
                    break;
                case 0x34:
                    des.handler();
                    break;
                case 0x35:
                    txs.handler();
                    break;
                case 0x36:
                    psha.handler();
                    break;
                case 0x37:
                    pshb.handler();
                    break;
                case 0x38:
                    pulx.handler(); /* 6803 only */ break;
                case 0x39:
                    rts.handler();
                    break;
                case 0x3a:
                    abx.handler(); /* 6803 only */ break;
                case 0x3b:
                    rti.handler();
                    break;
                case 0x3c:
                    pshx.handler(); /* 6803 only */ break;
                case 0x3d:
                    mul.handler(); /* 6803 only */ break;
                case 0x3e:
                    wai.handler();
                    break;
                case 0x3f:
                    swi.handler();
                    break;
                case 0x40:
                    nega.handler();
                    break;
                case 0x41:
                    trap.handler();
                    break;
                case 0x42:
                    trap.handler();
                    break;
                case 0x43:
                    coma.handler();
                    break;
                case 0x44:
                    lsra.handler();
                    break;
                case 0x45:
                    trap.handler();
                    break;
                case 0x46:
                    rora.handler();
                    break;
                case 0x47:
                    asra.handler();
                    break;
                case 0x48:
                    asla.handler();
                    break;
                case 0x49:
                    rola.handler();
                    break;
                case 0x4a:
                    deca.handler();
                    break;
                case 0x4b:
                    trap.handler();
                    break;
                case 0x4c:
                    inca.handler();
                    break;
                case 0x4d:
                    tsta.handler();
                    break;
                case 0x4e:
                    trap.handler();
                    break;
                case 0x4f:
                    clra.handler();
                    break;
                case 0x50:
                    negb.handler();
                    break;
                case 0x51:
                    trap.handler();
                    break;
                case 0x52:
                    trap.handler();
                    break;
                case 0x53:
                    comb.handler();
                    break;
                case 0x54:
                    lsrb.handler();
                    break;
                case 0x55:
                    trap.handler();
                    break;
                case 0x56:
                    rorb.handler();
                    break;
                case 0x57:
                    asrb.handler();
                    break;
                case 0x58:
                    aslb.handler();
                    break;
                case 0x59:
                    rolb.handler();
                    break;
                case 0x5a:
                    decb.handler();
                    break;
                case 0x5b:
                    trap.handler();
                    break;
                case 0x5c:
                    incb.handler();
                    break;
                case 0x5d:
                    tstb.handler();
                    break;
                case 0x5e:
                    trap.handler();
                    break;
                case 0x5f:
                    clrb.handler();
                    break;
                case 0x60:
                    neg_ix.handler();
                    break;
                case 0x61:
                    aim_ix.handler(); /* HD63701YO only */
                    break;
                case 0x62:
                    oim_ix.handler(); /* HD63701YO only */
                    break;
                case 0x63:
                    com_ix.handler();
                    break;
                case 0x64:
                    lsr_ix.handler();
                    break;
                case 0x65:
                    eim_ix.handler(); /* HD63701YO only */
                    break;
                case 0x66:
                    ror_ix.handler();
                    break;
                case 0x67:
                    asr_ix.handler();
                    break;
                case 0x68:
                    asl_ix.handler();
                    break;
                case 0x69:
                    rol_ix.handler();
                    break;
                case 0x6a:
                    dec_ix.handler();
                    break;
                case 0x6b:
                    tim_ix.handler(); /* HD63701YO only */
                    break;
                case 0x6c:
                    inc_ix.handler();
                    break;
                case 0x6d:
                    tst_ix.handler();
                    break;
                case 0x6e:
                    jmp_ix.handler();
                    break;
                case 0x6f:
                    clr_ix.handler();
                    break;
                case 0x70:
                    neg_ex.handler();
                    break;
                case 0x71:
                    aim_di.handler(); /* HD63701YO only */
                    break;
                case 0x72:
                    oim_di.handler(); /* HD63701YO only */
                    break;
                case 0x73:
                    com_ex.handler();
                    break;
                case 0x74:
                    lsr_ex.handler();
                    break;
                case 0x75:
                    eim_di.handler(); /* HD63701YO only */
                    break;
                case 0x76:
                    ror_ex.handler();
                    break;
                case 0x77:
                    asr_ex.handler();
                    break;
                case 0x78:
                    asl_ex.handler();
                    break;
                case 0x79:
                    rol_ex.handler();
                    break;
                case 0x7a:
                    dec_ex.handler();
                    break;
                case 0x7b:
                    tim_di.handler(); /* HD63701YO only */
                    break;
                case 0x7c:
                    inc_ex.handler();
                    break;
                case 0x7d:
                    tst_ex.handler();
                    break;
                case 0x7e:
                    jmp_ex.handler();
                    break;
                case 0x7f:
                    clr_ex.handler();
                    break;
                case 0x80:
                    suba_im.handler();
                    break;
                case 0x81:
                    cmpa_im.handler();
                    break;
                case 0x82:
                    sbca_im.handler();
                    break;
                case 0x83:
                    subd_im.handler(); /* 6803 only */ break;
                case 0x84:
                    anda_im.handler();
                    break;
                case 0x85:
                    bita_im.handler();
                    break;
                case 0x86:
                    lda_im.handler();
                    break;
                case 0x87:
                    sta_im.handler();
                    break;
                case 0x88:
                    eora_im.handler();
                    break;
                case 0x89:
                    adca_im.handler();
                    break;
                case 0x8a:
                    ora_im.handler();
                    break;
                case 0x8b:
                    adda_im.handler();
                    break;
                case 0x8c:
                    cpx_im.handler(); /* 6803 difference */ break;
                case 0x8d:
                    bsr.handler();
                    break;
                case 0x8e:
                    lds_im.handler();
                    break;
                case 0x8f:
                    sts_im.handler(); /* orthogonality */ break;
                case 0x90:
                    suba_di.handler();
                    break;
                case 0x91:
                    cmpa_di.handler();
                    break;
                case 0x92:
                    sbca_di.handler();
                    break;
                case 0x93:
                    subd_di.handler(); /* 6803 only */ break;
                case 0x94:
                    anda_di.handler();
                    break;
                case 0x95:
                    bita_di.handler();
                    break;
                case 0x96:
                    lda_di.handler();
                    break;
                case 0x97:
                    sta_di.handler();
                    break;
                case 0x98:
                    eora_di.handler();
                    break;
                case 0x99:
                    adca_di.handler();
                    break;
                case 0x9a:
                    ora_di.handler();
                    break;
                case 0x9b:
                    adda_di.handler();
                    break;
                case 0x9c:
                    cpx_di.handler(); /* 6803 difference */ break;
                case 0x9d:
                    jsr_di.handler();
                    break;
                case 0x9e:
                    lds_di.handler();
                    break;
                case 0x9f:
                    sts_di.handler();
                    break;
                case 0xa0:
                    suba_ix.handler();
                    break;
                case 0xa1:
                    cmpa_ix.handler();
                    break;
                case 0xa2:
                    sbca_ix.handler();
                    break;
                case 0xa3:
                    subd_ix.handler(); /* 6803 only */ break;
                case 0xa4:
                    anda_ix.handler();
                    break;
                case 0xa5:
                    bita_ix.handler();
                    break;
                case 0xa6:
                    lda_ix.handler();
                    break;
                case 0xa7:
                    sta_ix.handler();
                    break;
                case 0xa8:
                    eora_ix.handler();
                    break;
                case 0xa9:
                    adca_ix.handler();
                    break;
                case 0xaa:
                    ora_ix.handler();
                    break;
                case 0xab:
                    adda_ix.handler();
                    break;
                case 0xac:
                    cpx_ix.handler(); /* 6803 difference */ break;
                case 0xad:
                    jsr_ix.handler();
                    break;
                case 0xae:
                    lds_ix.handler();
                    break;
                case 0xaf:
                    sts_ix.handler();
                    break;
                case 0xb0:
                    suba_ex.handler();
                    break;
                case 0xb1:
                    cmpa_ex.handler();
                    break;
                case 0xb2:
                    sbca_ex.handler();
                    break;
                case 0xb3:
                    subd_ex.handler(); /* 6803 only */ break;
                case 0xb4:
                    anda_ex.handler();
                    break;
                case 0xb5:
                    bita_ex.handler();
                    break;
                case 0xb6:
                    lda_ex.handler();
                    break;
                case 0xb7:
                    sta_ex.handler();
                    break;
                case 0xb8:
                    eora_ex.handler();
                    break;
                case 0xb9:
                    adca_ex.handler();
                    break;
                case 0xba:
                    ora_ex.handler();
                    break;
                case 0xbb:
                    adda_ex.handler();
                    break;
                case 0xbc:
                    cpx_ex.handler(); /* 6803 difference */ break;
                case 0xbd:
                    jsr_ex.handler();
                    break;
                case 0xbe:
                    lds_ex.handler();
                    break;
                case 0xbf:
                    sts_ex.handler();
                    break;
                case 0xc0:
                    subb_im.handler();
                    break;
                case 0xc1:
                    cmpb_im.handler();
                    break;
                case 0xc2:
                    sbcb_im.handler();
                    break;
                case 0xc3:
                    addd_im.handler(); /* 6803 only */ break;
                case 0xc4:
                    andb_im.handler();
                    break;
                case 0xc5:
                    bitb_im.handler();
                    break;
                case 0xc6:
                    ldb_im.handler();
                    break;
                case 0xc7:
                    stb_im.handler();
                    break;
                case 0xc8:
                    eorb_im.handler();
                    break;
                case 0xc9:
                    adcb_im.handler();
                    break;
                case 0xca:
                    orb_im.handler();
                    break;
                case 0xcb:
                    addb_im.handler();
                    break;
                case 0xcc:
                    ldd_im.handler(); /* 6803 only */ break;
                case 0xcd:
                    std_im.handler(); /* 6803 only -- orthogonality */ break;
                case 0xce:
                    ldx_im.handler();
                    break;
                case 0xcf:
                    stx_im.handler();
                    break;
                case 0xd0:
                    subb_di.handler();
                    break;
                case 0xd1:
                    cmpb_di.handler();
                    break;
                case 0xd2:
                    sbcb_di.handler();
                    break;
                case 0xd3:
                    addd_di.handler(); /* 6803 only */ break;
                case 0xd4:
                    andb_di.handler();
                    break;
                case 0xd5:
                    bitb_di.handler();
                    break;
                case 0xd6:
                    ldb_di.handler();
                    break;
                case 0xd7:
                    stb_di.handler();
                    break;
                case 0xd8:
                    eorb_di.handler();
                    break;
                case 0xd9:
                    adcb_di.handler();
                    break;
                case 0xda:
                    orb_di.handler();
                    break;
                case 0xdb:
                    addb_di.handler();
                    break;
                case 0xdc:
                    ldd_di.handler(); /* 6803 only */ break;
                case 0xdd:
                    std_di.handler(); /* 6803 only */ break;
                case 0xde:
                    ldx_di.handler();
                    break;
                case 0xdf:
                    stx_di.handler();
                    break;
                case 0xe0:
                    subb_ix.handler();
                    break;
                case 0xe1:
                    cmpb_ix.handler();
                    break;
                case 0xe2:
                    sbcb_ix.handler();
                    break;
                case 0xe3:
                    addd_ix.handler(); /* 6803 only */ break;
                case 0xe4:
                    andb_ix.handler();
                    break;
                case 0xe5:
                    bitb_ix.handler();
                    break;
                case 0xe6:
                    ldb_ix.handler();
                    break;
                case 0xe7:
                    stb_ix.handler();
                    break;
                case 0xe8:
                    eorb_ix.handler();
                    break;
                case 0xe9:
                    adcb_ix.handler();
                    break;
                case 0xea:
                    orb_ix.handler();
                    break;
                case 0xeb:
                    addb_ix.handler();
                    break;
                case 0xec:
                    ldd_ix.handler(); /* 6803 only */ break;
                case 0xed:
                    std_ix.handler(); /* 6803 only */ break;
                case 0xee:
                    ldx_ix.handler();
                    break;
                case 0xef:
                    stx_ix.handler();
                    break;
                case 0xf0:
                    subb_ex.handler();
                    break;
                case 0xf1:
                    cmpb_ex.handler();
                    break;
                case 0xf2:
                    sbcb_ex.handler();
                    break;
                case 0xf3:
                    addd_ex.handler(); /* 6803 only */ break;
                case 0xf4:
                    andb_ex.handler();
                    break;
                case 0xf5:
                    bitb_ex.handler();
                    break;
                case 0xf6:
                    ldb_ex.handler();
                    break;
                case 0xf7:
                    stb_ex.handler();
                    break;
                case 0xf8:
                    eorb_ex.handler();
                    break;
                case 0xf9:
                    adcb_ex.handler();
                    break;
                case 0xfa:
                    orb_ex.handler();
                    break;
                case 0xfb:
                    addb_ex.handler();
                    break;
                case 0xfc:
                    ldd_ex.handler(); /* 6803 only */ break;
                case 0xfd:
                    std_ex.handler(); /* 6803 only */ break;
                case 0xfe:
                    ldx_ex.handler();
                    break;
                case 0xff:
                    stx_ex.handler();
                    break;
            }
            INCREMENT_COUNTER(cycles_63701[ireg]);
        } while (m6800_ICount[0] > 0);

        INCREMENT_COUNTER(m6800.extra_cycles);
        m6800.extra_cycles = 0;

        return cycles - m6800_ICount[0];
    }
}
