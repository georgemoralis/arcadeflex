/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.cpu.m6800;

//cpu imports
import static arcadeflex.v036.cpu.m6800.m6800ops.*;
import static arcadeflex.v036.cpu.m6800.m6800Η.*;
import static arcadeflex.v036.cpu.m6800.m6800tbl.*;
import static arcadeflex.v036.cpu.m6800.m6803.*;
//mame imports
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;

public class nsc8105 extends m6800 {

    public nsc8105() {
        cpu_num = CPU_NSC8105;
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
        m6800_ICount[0] = 50000;
    }

    public String cpu_info(Object context, int regnum) {
        switch (regnum) {
            case CPU_INFO_NAME:
                return "NSC8105";
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
        m6800.insn = nsc8105_insn;
        m6800.cycles = cycles_nsc8105;
    }

    public int execute(int cycles) {
        int ireg;
        m6800_ICount[0] = cycles;
        CLEANUP_conters();
        INCREMENT_COUNTER(m6800.extra_cycles);
        m6800.extra_cycles = 0;

        if ((m6800.wai_state & M6800_WAI) != 0) {
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
                    illegal.handler();
                    break;
                case 0x01:
                    illegal.handler();
                    break;
                case 0x02:
                    nop.handler();
                    break;
                case 0x03:
                    illegal.handler();
                    break;
                case 0x04:
                    illegal.handler();
                    break;
                case 0x05:
                    tap.handler();
                    break;
                case 0x06:
                    illegal.handler();
                    break;
                case 0x07:
                    tpa.handler();
                    break;
                case 0x08:
                    inx.handler();
                    break;
                case 0x09:
                    CLV();
                    break;
                case 0x0a:
                    dex.handler();
                    break;
                case 0x0b:
                    SEV();
                    break;
                case 0x0c:
                    CLC();
                    break;
                case 0x0d:
                    cli.handler();
                    break;
                case 0x0e:
                    SEC();
                    break;
                case 0x0f:
                    sei.handler();
                    break;
                case 0x10:
                    sba.handler();
                    break;
                case 0x11:
                    illegal.handler();
                    break;
                case 0x12:
                    cba.handler();
                    break;
                case 0x13:
                    illegal.handler();
                    break;
                case 0x14:
                    illegal.handler();
                    break;
                case 0x15:
                    tab.handler();
                    break;
                case 0x16:
                    illegal.handler();
                    break;
                case 0x17:
                    tba.handler();
                    break;
                case 0x18:
                    illegal.handler();
                    break;
                case 0x19:
                    illegal.handler();
                    break;
                case 0x1a:
                    daa.handler();
                    break;
                case 0x1b:
                    aba.handler();
                    break;
                case 0x1c:
                    illegal.handler();
                    break;
                case 0x1d:
                    illegal.handler();
                    break;
                case 0x1e:
                    illegal.handler();
                    break;
                case 0x1f:
                    illegal.handler();
                    break;
                case 0x20:
                    bra.handler();
                    break;
                case 0x21:
                    bhi.handler();
                    break;
                case 0x22:
                    brn.handler();
                    break;
                case 0x23:
                    bls.handler();
                    break;
                case 0x24:
                    bcc.handler();
                    break;
                case 0x25:
                    bne.handler();
                    break;
                case 0x26:
                    bcs.handler();
                    break;
                case 0x27:
                    beq.handler();
                    break;
                case 0x28:
                    bvc.handler();
                    break;
                case 0x29:
                    bpl.handler();
                    break;
                case 0x2a:
                    bvs.handler();
                    break;
                case 0x2b:
                    bmi.handler();
                    break;
                case 0x2c:
                    bge.handler();
                    break;
                case 0x2d:
                    bgt.handler();
                    break;
                case 0x2e:
                    blt.handler();
                    break;
                case 0x2f:
                    ble.handler();
                    break;
                case 0x30:
                    tsx.handler();
                    break;
                case 0x31:
                    pula.handler();
                    break;
                case 0x32:
                    ins.handler();
                    break;
                case 0x33:
                    pulb.handler();
                    break;
                case 0x34:
                    des.handler();
                    break;
                case 0x35:
                    psha.handler();
                    break;
                case 0x36:
                    txs.handler();
                    break;
                case 0x37:
                    pshb.handler();
                    break;
                case 0x38:
                    illegal.handler();
                    break;
                case 0x39:
                    illegal.handler();
                    break;
                case 0x3a:
                    rts.handler();
                    break;
                case 0x3b:
                    rti.handler();
                    break;
                case 0x3c:
                    illegal.handler();
                    break;
                case 0x3d:
                    wai.handler();
                    break;
                case 0x3e:
                    illegal.handler();
                    break;
                case 0x3f:
                    swi.handler();
                    break;
                case 0x40:
                    suba_im.handler();
                    break;
                case 0x41:
                    sbca_im.handler();
                    break;
                case 0x42:
                    cmpa_im.handler();
                    break;
                case 0x43:
                    illegal.handler();
                    break;
                case 0x44:
                    anda_im.handler();
                    break;
                case 0x45:
                    lda_im.handler();
                    break;
                case 0x46:
                    bita_im.handler();
                    break;
                case 0x47:
                    sta_im.handler();
                    break;
                case 0x48:
                    eora_im.handler();
                    break;
                case 0x49:
                    ora_im.handler();
                    break;
                case 0x4a:
                    adca_im.handler();
                    break;
                case 0x4b:
                    adda_im.handler();
                    break;
                case 0x4c:
                    cmpx_im.handler();
                    break;
                case 0x4d:
                    lds_im.handler();
                    break;
                case 0x4e:
                    bsr.handler();
                    break;
                case 0x4f:
                    sts_im.handler();
                    /* orthogonality */ break;
                case 0x50:
                    suba_di.handler();
                    break;
                case 0x51:
                    sbca_di.handler();
                    break;
                case 0x52:
                    cmpa_di.handler();
                    break;
                case 0x53:
                    illegal.handler();
                    break;
                case 0x54:
                    anda_di.handler();
                    break;
                case 0x55:
                    lda_di.handler();
                    break;
                case 0x56:
                    bita_di.handler();
                    break;
                case 0x57:
                    sta_di.handler();
                    break;
                case 0x58:
                    eora_di.handler();
                    break;
                case 0x59:
                    ora_di.handler();
                    break;
                case 0x5a:
                    adca_di.handler();
                    break;
                case 0x5b:
                    adda_di.handler();
                    break;
                case 0x5c:
                    cmpx_di.handler();
                    break;
                case 0x5d:
                    lds_di.handler();
                    break;
                case 0x5e:
                    jsr_di.handler();
                    break;
                case 0x5f:
                    sts_di.handler();
                    break;
                case 0x60:
                    suba_ix.handler();
                    break;
                case 0x61:
                    sbca_ix.handler();
                    break;
                case 0x62:
                    cmpa_ix.handler();
                    break;
                case 0x63:
                    illegal.handler();
                    break;
                case 0x64:
                    anda_ix.handler();
                    break;
                case 0x65:
                    lda_ix.handler();
                    break;
                case 0x66:
                    bita_ix.handler();
                    break;
                case 0x67:
                    sta_ix.handler();
                    break;
                case 0x68:
                    eora_ix.handler();
                    break;
                case 0x69:
                    ora_ix.handler();
                    break;
                case 0x6a:
                    adca_ix.handler();
                    break;
                case 0x6b:
                    adda_ix.handler();
                    break;
                case 0x6c:
                    cmpx_ix.handler();
                    break;
                case 0x6d:
                    lds_ix.handler();
                    break;
                case 0x6e:
                    jsr_ix.handler();
                    break;
                case 0x6f:
                    sts_ix.handler();
                    break;
                case 0x70:
                    suba_ex.handler();
                    break;
                case 0x71:
                    sbca_ex.handler();
                    break;
                case 0x72:
                    cmpa_ex.handler();
                    break;
                case 0x73:
                    illegal.handler();
                    break;
                case 0x74:
                    anda_ex.handler();
                    break;
                case 0x75:
                    lda_ex.handler();
                    break;
                case 0x76:
                    bita_ex.handler();
                    break;
                case 0x77:
                    sta_ex.handler();
                    break;
                case 0x78:
                    eora_ex.handler();
                    break;
                case 0x79:
                    ora_ex.handler();
                    break;
                case 0x7a:
                    adca_ex.handler();
                    break;
                case 0x7b:
                    adda_ex.handler();
                    break;
                case 0x7c:
                    cmpx_ex.handler();
                    break;
                case 0x7d:
                    lds_ex.handler();
                    break;
                case 0x7e:
                    jsr_ex.handler();
                    break;
                case 0x7f:
                    sts_ex.handler();
                    break;
                case 0x80:
                    nega.handler();
                    break;
                case 0x81:
                    illegal.handler();
                    break;
                case 0x82:
                    illegal.handler();
                    break;
                case 0x83:
                    coma.handler();
                    break;
                case 0x84:
                    lsra.handler();
                    break;
                case 0x85:
                    rora.handler();
                    break;
                case 0x86:
                    illegal.handler();
                    break;
                case 0x87:
                    asra.handler();
                    break;
                case 0x88:
                    asla.handler();
                    break;
                case 0x89:
                    deca.handler();
                    break;
                case 0x8a:
                    rola.handler();
                    break;
                case 0x8b:
                    illegal.handler();
                    break;
                case 0x8c:
                    inca.handler();
                    break;
                case 0x8d:
                    illegal.handler();
                    break;
                case 0x8e:
                    tsta.handler();
                    break;
                case 0x8f:
                    clra.handler();
                    break;
                case 0x90:
                    negb.handler();
                    break;
                case 0x91:
                    illegal.handler();
                    break;
                case 0x92:
                    illegal.handler();
                    break;
                case 0x93:
                    comb.handler();
                    break;
                case 0x94:
                    lsrb.handler();
                    break;
                case 0x95:
                    rorb.handler();
                    break;
                case 0x96:
                    illegal.handler();
                    break;
                case 0x97:
                    asrb.handler();
                    break;
                case 0x98:
                    aslb.handler();
                    break;
                case 0x99:
                    decb.handler();
                    break;
                case 0x9a:
                    rolb.handler();
                    break;
                case 0x9b:
                    illegal.handler();
                    break;
                case 0x9c:
                    incb.handler();
                    break;
                case 0x9d:
                    illegal.handler();
                    break;
                case 0x9e:
                    tstb.handler();
                    break;
                case 0x9f:
                    clrb.handler();
                    break;
                case 0xa0:
                    neg_ix.handler();
                    break;
                case 0xa1:
                    illegal.handler();
                    break;
                case 0xa2:
                    illegal.handler();
                    break;
                case 0xa3:
                    com_ix.handler();
                    break;
                case 0xa4:
                    lsr_ix.handler();
                    break;
                case 0xa5:
                    ror_ix.handler();
                    break;
                case 0xa6:
                    illegal.handler();
                    break;
                case 0xa7:
                    asr_ix.handler();
                    break;
                case 0xa8:
                    asl_ix.handler();
                    break;
                case 0xa9:
                    dec_ix.handler();
                    break;
                case 0xaa:
                    rol_ix.handler();
                    break;
                case 0xab:
                    illegal.handler();
                    break;
                case 0xac:
                    inc_ix.handler();
                    break;
                case 0xad:
                    jmp_ix.handler();
                    break;
                case 0xae:
                    tst_ix.handler();
                    break;
                case 0xaf:
                    clr_ix.handler();
                    break;
                case 0xb0:
                    neg_ex.handler();
                    break;
                case 0xb1:
                    illegal.handler();
                    break;
                case 0xb2:
                    illegal.handler();
                    break;
                case 0xb3:
                    com_ex.handler();
                    break;
                case 0xb4:
                    lsr_ex.handler();
                    break;
                case 0xb5:
                    ror_ex.handler();
                    break;
                case 0xb6:
                    illegal.handler();
                    break;
                case 0xb7:
                    asr_ex.handler();
                    break;
                case 0xb8:
                    asl_ex.handler();
                    break;
                case 0xb9:
                    dec_ex.handler();
                    break;
                case 0xba:
                    rol_ex.handler();
                    break;
                case 0xbb:
                    illegal.handler();
                    break;
                case 0xbc:
                    inc_ex.handler();
                    break;
                case 0xbd:
                    jmp_ex.handler();
                    break;
                case 0xbe:
                    tst_ex.handler();
                    break;
                case 0xbf:
                    clr_ex.handler();
                    break;
                case 0xc0:
                    subb_im.handler();
                    break;
                case 0xc1:
                    sbcb_im.handler();
                    break;
                case 0xc2:
                    cmpb_im.handler();
                    break;
                case 0xc3:
                    illegal.handler();
                    break;
                case 0xc4:
                    andb_im.handler();
                    break;
                case 0xc5:
                    ldb_im.handler();
                    break;
                case 0xc6:
                    bitb_im.handler();
                    break;
                case 0xc7:
                    stb_im.handler();
                    break;
                case 0xc8:
                    eorb_im.handler();
                    break;
                case 0xc9:
                    orb_im.handler();
                    break;
                case 0xca:
                    adcb_im.handler();
                    break;
                case 0xcb:
                    addb_im.handler();
                    break;
                case 0xcc:
                    illegal.handler();
                    break;
                case 0xcd:
                    ldx_im.handler();
                    break;
                case 0xce:
                    illegal.handler();
                    break;
                case 0xcf:
                    stx_im.handler();
                    break;
                case 0xd0:
                    subb_di.handler();
                    break;
                case 0xd1:
                    sbcb_di.handler();
                    break;
                case 0xd2:
                    cmpb_di.handler();
                    break;
                case 0xd3:
                    illegal.handler();
                    break;
                case 0xd4:
                    andb_di.handler();
                    break;
                case 0xd5:
                    ldb_di.handler();
                    break;
                case 0xd6:
                    bitb_di.handler();
                    break;
                case 0xd7:
                    stb_di.handler();
                    break;
                case 0xd8:
                    eorb_di.handler();
                    break;
                case 0xd9:
                    orb_di.handler();
                    break;
                case 0xda:
                    adcb_di.handler();
                    break;
                case 0xdb:
                    addb_di.handler();
                    break;
                case 0xdc:
                    illegal.handler();
                    break;
                case 0xdd:
                    ldx_di.handler();
                    break;
                case 0xde:
                    illegal.handler();
                    break;
                case 0xdf:
                    stx_di.handler();
                    break;
                case 0xe0:
                    subb_ix.handler();
                    break;
                case 0xe1:
                    sbcb_ix.handler();
                    break;
                case 0xe2:
                    cmpb_ix.handler();
                    break;
                case 0xe3:
                    illegal.handler();
                    break;
                case 0xe4:
                    andb_ix.handler();
                    break;
                case 0xe5:
                    ldb_ix.handler();
                    break;
                case 0xe6:
                    bitb_ix.handler();
                    break;
                case 0xe7:
                    stb_ix.handler();
                    break;
                case 0xe8:
                    eorb_ix.handler();
                    break;
                case 0xe9:
                    orb_ix.handler();
                    break;
                case 0xea:
                    adcb_ix.handler();
                    break;
                case 0xeb:
                    addb_ix.handler();
                    break;
                case 0xec:
                    illegal.handler();
                    break;
                case 0xed:
                    ldx_ix.handler();
                    break;
                case 0xee:
                    illegal.handler();
                    break;
                case 0xef:
                    stx_ix.handler();
                    break;
                case 0xf0:
                    subb_ex.handler();
                    break;
                case 0xf1:
                    sbcb_ex.handler();
                    break;
                case 0xf2:
                    cmpb_ex.handler();
                    break;
                case 0xf3:
                    illegal.handler();
                    break;
                case 0xf4:
                    andb_ex.handler();
                    break;
                case 0xf5:
                    ldb_ex.handler();
                    break;
                case 0xf6:
                    bitb_ex.handler();
                    break;
                case 0xf7:
                    stb_ex.handler();
                    break;
                case 0xf8:
                    eorb_ex.handler();
                    break;
                case 0xf9:
                    orb_ex.handler();
                    break;
                case 0xfa:
                    adcb_ex.handler();
                    break;
                case 0xfb:
                    addb_ex.handler();
                    break;
                case 0xfc:
                    addx_ex.handler();
                    break;
                case 0xfd:
                    ldx_ex.handler();
                    break;
                case 0xfe:
                    illegal.handler();
                    break;
                case 0xff:
                    stx_ex.handler();
                    break;
            }
            INCREMENT_COUNTER(cycles_nsc8105[ireg]);
        } while (m6800_ICount[0] > 0);

        return cycles - m6800_ICount[0];
    }
    int cycles_nsc8105[]
            = {
                /* 0  1  2  3  4  5  6  7  8  9  A  B  C  D  E  F */
                /*0*/0, 0, 2, 0, 0, 2, 0, 2, 4, 2, 4, 2, 2, 2, 2, 2,
                /*1*/ 2, 0, 2, 0, 0, 2, 0, 2, 0, 0, 2, 2, 0, 0, 0, 0,
                /*2*/ 4, 4, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
                /*3*/ 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 5, 10, 0, 9, 0, 12,
                /*4*/ 2, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2,
                /*5*/ 2, 0, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 2, 0, 2, 2,
                /*6*/ 7, 0, 0, 7, 7, 7, 0, 7, 7, 7, 7, 0, 7, 4, 7, 7,
                /*7*/ 6, 0, 0, 6, 6, 6, 0, 6, 6, 6, 6, 0, 6, 3, 6, 6,
                /*8*/ 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 3, 3, 8, 0,
                /*9*/ 3, 3, 3, 0, 3, 3, 3, 4, 3, 3, 3, 3, 4, 4, 0, 5,
                /*A*/ 5, 5, 5, 0, 5, 5, 5, 6, 5, 5, 5, 5, 6, 6, 8, 7,
                /*B*/ 4, 4, 4, 0, 4, 4, 4, 5, 4, 4, 4, 4, 5, 5, 9, 6,
                /*C*/ 2, 2, 2, 0, 2, 2, 2, 0, 2, 2, 2, 2, 0, 3, 0, 0,
                /*D*/ 3, 3, 3, 0, 3, 3, 3, 4, 3, 3, 3, 3, 0, 4, 0, 5,
                /*E*/ 5, 5, 5, 0, 5, 5, 5, 6, 5, 5, 5, 5, 0, 6, 0, 7,
                /*F*/ 4, 4, 4, 0, 4, 4, 4, 5, 4, 4, 4, 4, 4, 5, 0, 6
            };
}
