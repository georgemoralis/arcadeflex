package vidhrdw;

import static arcadeflex.ptrlib.*;
import static mame.driverH.*;
import static mame.osdependH.*;

public class cps1 {

    /**
     * ******************************************************************
     *
     * Configuration table:
     *
     *******************************************************************
     */

    /* Game specific data */
    public static class CPS1config {

        String name;             /* game driver name */

        /* Some games interrogate a couple of registers on bootup. */
        /* These are CPS1 board B self test checks. They wander from game to */
        /* game. */
        int cpsb_addr;        /* CPS board B test register address */

        int cpsb_value;       /* CPS board B test register expected value */

        /* some games use as a protection check the ability to do 16-bit multiplies */
        /* with a 32-bit result, by writing the factors to two ports and reading the */
        /* result from two other ports. */
        /* It looks like this feature was introduced with 3wonders (CPSB ID = 08xx) */
        int mult_factor1;
        int mult_factor2;
        int mult_result_lo;
        int mult_result_hi;

        int layer_control;
        int priority0;
        int priority1;
        int priority2;
        int priority3;
        int control_reg;  /* Control register? seems to be always 0x3f */

        /* ideally, the layer enable masks should consist of only one bit, */
        /* but in many cases it is unknown which bit is which. */
        int scrl1_enable_mask;
        int scrl2_enable_mask;
        int scrl3_enable_mask;

        int bank_scroll1;
        int bank_scroll2;
        int bank_scroll3;

        /* Some characters aren't visible */
        int space_scroll1;
        int start_scroll2;
        int end_scroll2;
        int start_scroll3;
        int end_scroll3;

        int kludge;  /* Ghouls n Ghosts sprite kludge */


        private CPS1config(String name, int cpsb_addr, int cpsb_value, int mult_factor1, int mult_factor2, int mult_result_lo, int mult_result_hi,
                int layer_control, int priority0, int priority1, int priority2, int priority3, int control_reg, int scrl1_enable_mask, int scrl2_enable_mask,
                int scrl3_enable_mask, int bank_scroll1, int bank_scroll2, int bank_scroll3, int space_scroll1,
                int start_scroll2, int end_scroll2, int start_scroll3, int end_scroll3, int kludge) {
            this.name = name;
            this.cpsb_addr = cpsb_addr;
            this.cpsb_value = cpsb_value;
            this.mult_factor1 = mult_factor1;
            this.mult_factor2 = mult_factor2;
            this.mult_result_lo = mult_result_lo;
            this.mult_result_hi = mult_result_hi;
            this.layer_control = layer_control;
            this.priority0 = priority0;
            this.priority1 = priority1;
            this.priority2 = priority2;
            this.priority3 = priority3;
            this.control_reg = control_reg;
            this.scrl1_enable_mask = scrl1_enable_mask;
            this.scrl2_enable_mask = scrl2_enable_mask;
            this.scrl3_enable_mask = scrl3_enable_mask;
            this.bank_scroll1 = bank_scroll1;
            this.bank_scroll2 = bank_scroll2;
            this.bank_scroll3 = bank_scroll3;
            this.space_scroll1 = space_scroll1;
            this.start_scroll2 = start_scroll2;
            this.end_scroll2 = end_scroll2;
            this.start_scroll3 = start_scroll3;
            this.end_scroll3 = end_scroll3;
            this.kludge = kludge;
        }
    };
    public static CPS1config cps1_game_config;

    static CPS1config cps1_config_table[]
            = {
                /* name       CPSB ID    multiply protection  ctrl    priority masks  unknwn  layer enable   banks spacechr kludge */
                new CPS1config("forgottn", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 7),
                new CPS1config("lostwrld", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 7),
                new CPS1config("ghouls", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 1),
                new CPS1config("ghoulsu", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 1),
                new CPS1config("ghoulsj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 1),
                new CPS1config("strider", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 1, 0, 1, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("striderj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 1, 0, 1, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("stridrja", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 1, 0, 1, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("dwj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x6c, 0x6a, 0x68, 0x66, 0x64, 0x62, 0x02, 0x04, 0x08, 0, 1, 1, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("willow", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x70, 0x6e, 0x6c, 0x6a, 0x68, 0x66, 0x20, 0x10, 0x08, 0, 1, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("willowj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x70, 0x6e, 0x6c, 0x6a, 0x68, 0x66, 0x20, 0x10, 0x08, 0, 1, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("unsquad", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x38, 0x38, 0x38, 0, 0, 0, -1, 0x0000, 0xffff, 0x0001, 0xffff, 0),
                new CPS1config("area88", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x38, 0x38, 0x38, 0, 0, 0, -1, 0x0000, 0xffff, 0x0001, 0xffff, 0),
                new CPS1config("ffight", 0x60, 0x0004, 0x00, 0x00, 0x00, 0x00, 0x6e, 0x66, 0x70, 0x68, 0x72, 0x6a, 0x02, 0x0c, 0x0c, 0, 0, 0, -1, 0x0001, 0xffff, 0x0001, 0xffff, 0),
                new CPS1config("ffightu", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 0, 0, 0, -1, 0x0001, 0xffff, 0x0001, 0xffff, 0),
                new CPS1config("ffightj", 0x60, 0x0004, 0x00, 0x00, 0x00, 0x00, 0x6e, 0x66, 0x70, 0x68, 0x72, 0x6a, 0x02, 0x0c, 0x0c, 0, 0, 0, -1, 0x0001, 0xffff, 0x0001, 0xffff, 0),
                new CPS1config("1941", 0x60, 0x0005, 0x00, 0x00, 0x00, 0x00, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x72, 0x02, 0x08, 0x20, 0, 0, 0, -1, 0x0000, 0xffff, 0x0400, 0x07ff, 0),
                new CPS1config("1941j", 0x60, 0x0005, 0x00, 0x00, 0x00, 0x00, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x72, 0x02, 0x08, 0x20, 0, 0, 0, -1, 0x0000, 0xffff, 0x0400, 0x07ff, 0),
                new CPS1config("mercs", 0x60, 0x0402, 0x00, 0x00, 0x00, 0x00, 0x6c, 0x00, 0x00, 0x00, 0x00, 0x62, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0600, 0x5bff, 0x0700, 0x17ff, 4), /* (uses port 74) */
                new CPS1config("mercsu", 0x60, 0x0402, 0x00, 0x00, 0x00, 0x00, 0x6c, 0x00, 0x00, 0x00, 0x00, 0x62, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0600, 0x5bff, 0x0700, 0x17ff, 4), /* (uses port 74) */
                new CPS1config("mercsj", 0x60, 0x0402, 0x00, 0x00, 0x00, 0x00, 0x6c, 0x00, 0x00, 0x00, 0x00, 0x62, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0600, 0x5bff, 0x0700, 0x17ff, 4), /* (uses port 74) */
                new CPS1config("mtwins", 0x5e, 0x0404, 0x00, 0x00, 0x00, 0x00, 0x52, 0x54, 0x56, 0x58, 0x5a, 0x5c, 0x38, 0x38, 0x38, 0, 0, 0, -1, 0x0000, 0xffff, 0x0e00, 0xffff, 0),
                new CPS1config("chikij", 0x5e, 0x0404, 0x00, 0x00, 0x00, 0x00, 0x52, 0x54, 0x56, 0x58, 0x5a, 0x5c, 0x38, 0x38, 0x38, 0, 0, 0, -1, 0x0000, 0xffff, 0x0e00, 0xffff, 0),
                new CPS1config("msword", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x62, 0x64, 0x66, 0x68, 0x6a, 0x6c, 0x20, 0x06, 0x06, 0, 0, 0, -1, 0x2800, 0x37ff, 0x0000, 0xffff, 0),
                new CPS1config("mswordu", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x62, 0x64, 0x66, 0x68, 0x6a, 0x6c, 0x20, 0x06, 0x06, 0, 0, 0, -1, 0x2800, 0x37ff, 0x0000, 0xffff, 0),
                new CPS1config("mswordj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x62, 0x64, 0x66, 0x68, 0x6a, 0x6c, 0x20, 0x06, 0x06, 0, 0, 0, -1, 0x2800, 0x37ff, 0x0000, 0xffff, 0),
                new CPS1config("cawing", 0x40, 0x0406, 0x00, 0x00, 0x00, 0x00, 0x4c, 0x4a, 0x48, 0x46, 0x44, 0x42, 0x10, 0x0a, 0x0a, 0, 0, 0, 0x0002, 0x0000, 0xffff, 0x0000, 0xffff, 6), /* row scroll used at the beginning of mission 8, put 07 at ff8501 to jump there */
                new CPS1config("cawingj", 0x40, 0x0406, 0x00, 0x00, 0x00, 0x00, 0x4c, 0x4a, 0x48, 0x46, 0x44, 0x42, 0x10, 0x0a, 0x0a, 0, 0, 0, 0x0002, 0x0000, 0xffff, 0x0000, 0xffff, 6), /* row scroll used at the beginning of mission 8, put 07 at ff8501 to jump there */
                new CPS1config("nemo", 0x4e, 0x0405, 0x00, 0x00, 0x00, 0x00, 0x42, 0x44, 0x46, 0x48, 0x4a, 0x4c, 0x04, 0x22, 0x22, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("nemoj", 0x4e, 0x0405, 0x00, 0x00, 0x00, 0x00, 0x42, 0x44, 0x46, 0x48, 0x4a, 0x4c, 0x04, 0x22, 0x22, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2", 0x48, 0x0407, 0x00, 0x00, 0x00, 0x00, 0x54, 0x52, 0x50, 0x4e, 0x4c, 0x4a, 0x08, 0x12, 0x12, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2a", 0x48, 0x0407, 0x00, 0x00, 0x00, 0x00, 0x54, 0x52, 0x50, 0x4e, 0x4c, 0x4a, 0x08, 0x12, 0x12, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2b", 0x48, 0x0407, 0x00, 0x00, 0x00, 0x00, 0x54, 0x52, 0x50, 0x4e, 0x4c, 0x4a, 0x08, 0x12, 0x12, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2e", 0xd0, 0x0408, 0x00, 0x00, 0x00, 0x00, 0xdc, 0xda, 0xd8, 0xd6, 0xd4, 0xd2, 0x10, 0x0a, 0x0a, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2j", 0x6e, 0x0403, 0x00, 0x00, 0x00, 0x00, 0x62, 0x64, 0x66, 0x68, 0x6a, 0x6c, 0x20, 0x06, 0x06, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2jb", 0x48, 0x0407, 0x00, 0x00, 0x00, 0x00, 0x54, 0x52, 0x50, 0x4e, 0x4c, 0x4a, 0x08, 0x12, 0x12, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("3wonders", 0x72, 0x0800, 0x4e, 0x4c, 0x4a, 0x48, 0x68, 0x66, 0x64, 0x62, 0x60, 0x70, 0x20, 0x04, 0x08, 0, 1, 1, -1, 0x0000, 0xffff, 0x0000, 0xffff, 2),
                new CPS1config("wonder3", 0x72, 0x0800, 0x4e, 0x4c, 0x4a, 0x48, 0x68, 0x66, 0x64, 0x62, 0x60, 0x70, 0x20, 0x04, 0x08, 0, 1, 1, -1, 0x0000, 0xffff, 0x0000, 0xffff, 2),
                new CPS1config("kod", 0x00, 0x0000, 0x5e, 0x5c, 0x5a, 0x58, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x30, 0x08, 0x30, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("kodj", 0x00, 0x0000, 0x5e, 0x5c, 0x5a, 0x58, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x30, 0x08, 0x30, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("kodb", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x30, 0x08, 0x30, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("captcomm", 0x00, 0x0000, 0x46, 0x44, 0x42, 0x40, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x20, 0x12, 0x12, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* multiply is used only to center the startup text */
                new CPS1config("captcomu", 0x00, 0x0000, 0x46, 0x44, 0x42, 0x40, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x20, 0x12, 0x12, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* multiply is used only to center the startup text */
                new CPS1config("captcomj", 0x00, 0x0000, 0x46, 0x44, 0x42, 0x40, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x20, 0x12, 0x12, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* multiply is used only to center the startup text */
                new CPS1config("knights", 0x00, 0x0000, 0x46, 0x44, 0x42, 0x40, 0x68, 0x66, 0x64, 0x62, 0x60, 0x70, 0x20, 0x10, 0x02, 0, 0, 0, 0xf020, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("knightsj", 0x00, 0x0000, 0x46, 0x44, 0x42, 0x40, 0x68, 0x66, 0x64, 0x62, 0x60, 0x70, 0x20, 0x10, 0x02, 0, 0, 0, 0xf020, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2ce", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2cea", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2ceb", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2cej", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2rb", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2red", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2accp2", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("varth", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x6e, 0x66, 0x70, 0x68, 0x72, 0x6a, 0x02, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* CPSB test has been patched out (60=0008) */
                new CPS1config("varthu", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x6e, 0x66, 0x70, 0x68, 0x72, 0x6a, 0x02, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* CPSB test has been patched out (60=0008) */
                new CPS1config("varthj", 0x00, 0x0000, 0x4e, 0x4c, 0x4a, 0x48, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x20, 0x06, 0x06, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* CPSB test has been patched out (72=0001) */
                new CPS1config("cworld2j", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x60, 0x6e, 0x6c, 0x6a, 0x68, 0x70, 0x20, 0x14, 0x14, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* The 0x76 priority values are incorrect values */
                new CPS1config("wof", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("wofj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x62, 0x64, 0x66, 0x68, 0x6a, 0x6c, 0x10, 0x08, 0x04, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("dino", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x4a, 0x4c, 0x4e, 0x40, 0x42, 0x44, 0x16, 0x16, 0x16, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* layer enable never used */
                new CPS1config("dinoj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x4a, 0x4c, 0x4e, 0x40, 0x42, 0x44, 0x16, 0x16, 0x16, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0), /* layer enable never used */
                new CPS1config("punisher", 0x4e, 0x0c00, 0x00, 0x00, 0x00, 0x00, 0x52, 0x54, 0x56, 0x48, 0x4a, 0x4c, 0x04, 0x02, 0x20, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("punishru", 0x4e, 0x0c00, 0x00, 0x00, 0x00, 0x00, 0x52, 0x54, 0x56, 0x48, 0x4a, 0x4c, 0x04, 0x02, 0x20, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("punishrj", 0x4e, 0x0c00, 0x00, 0x00, 0x00, 0x00, 0x52, 0x54, 0x56, 0x48, 0x4a, 0x4c, 0x04, 0x02, 0x20, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("slammast", 0x6e, 0x0c01, 0x00, 0x00, 0x00, 0x00, 0x56, 0x40, 0x42, 0x68, 0x6a, 0x6c, 0x10, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("mbomberj", 0x6e, 0x0c01, 0x00, 0x00, 0x00, 0x00, 0x56, 0x40, 0x42, 0x68, 0x6a, 0x6c, 0x10, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("mbombrd", 0x5e, 0x0c02, 0x00, 0x00, 0x00, 0x00, 0x6a, 0x6c, 0x6e, 0x70, 0x72, 0x5c, 0x10, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("mbombrdj", 0x5e, 0x0c02, 0x00, 0x00, 0x00, 0x00, 0x6a, 0x6c, 0x6e, 0x70, 0x72, 0x5c, 0x10, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2t", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sf2tj", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 2, 2, 2, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("pnickj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x00, 0x00, 0x00, 0x00, 0x70, 0x0e, 0x0e, 0x0e, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("qad", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x6c, 0x00, 0x00, 0x00, 0x00, 0x52, 0x14, 0x02, 0x14, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("qadj", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x0e, 0x0e, 0x0e, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("qtono2", 0x00, 0x0000, 0x40, 0x42, 0x44, 0x46, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x06, 0x06, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("pang3", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x0c, 0x0c, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 5), /* EEPROM port is among the CPS registers */
                new CPS1config("megaman", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("rockmanj", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                new CPS1config("sfzch", 0x00, 0x0000, 0x00, 0x00, 0x00, 0x00, 0x66, 0x68, 0x6a, 0x6c, 0x6e, 0x70, 0x02, 0x04, 0x08, 0, 0, 0, -1, 0x0000, 0xffff, 0x0000, 0xffff, 0),
                null /* End of table */};
    /*TODO*///
/*TODO*///static void cps1_init_machine(void)
/*TODO*///{
/*TODO*///	const char *gamename = Machine->gamedrv->name;
/*TODO*///	unsigned char *RAM = memory_region(REGION_CPU1);
/*TODO*///
/*TODO*///
/*TODO*///	struct CPS1config *pCFG=&cps1_config_table[0];
/*TODO*///	while(pCFG->name)
/*TODO*///	{
/*TODO*///		if (strcmp(pCFG->name, gamename) == 0)
/*TODO*///		{
/*TODO*///			break;
/*TODO*///		}
/*TODO*///		pCFG++;
/*TODO*///	}
/*TODO*///	cps1_game_config=pCFG;
/*TODO*///
/*TODO*///	if (strcmp(gamename, "sf2rb" )==0)
/*TODO*///	{
/*TODO*///		/* Patch out protection check */
/*TODO*///		WRITE_WORD(&RAM[0xe5464],0x6012);
/*TODO*///	}
/*TODO*///#if 0
/*TODO*///	else if (strcmp(gamename, "ghouls" )==0)
/*TODO*///	{
/*TODO*///		/* Patch out self-test... it takes forever */
/*TODO*///		WRITE_WORD(&RAM[0x61964+0], 0x4ef9);
/*TODO*///		WRITE_WORD(&RAM[0x61964+2], 0x0000);
/*TODO*///		WRITE_WORD(&RAM[0x61964+4], 0x0400);
/*TODO*///	}
/*TODO*///#endif
/*TODO*///  /*
/*TODO*///	else if (strcmp(gamename, "slammast" )==0 || strcmp(gamename, "mbomberj" )==0)
/*TODO*///	{
/*TODO*///		WRITE_WORD(&RAM[0x0fbe], 0x4e75);
/*TODO*///	}
/*TODO*///	else if (strcmp(gamename, "mbombrd" )==0 || strcmp(gamename, "mbombrdj" )==0)
/*TODO*///	{
/*TODO*///		WRITE_WORD(&RAM[0x0f1a], 0x4e75);
/*TODO*///	}
/*TODO*///    */
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE int cps1_port(int offset)
/*TODO*///{
/*TODO*///    return READ_WORD(&cps1_output[offset]);
/*TODO*///}
/*TODO*///
/*TODO*///INLINE unsigned char * cps1_base(int offset,int boundary)
/*TODO*///{
/*TODO*///    int base=cps1_port(offset)*256;
/*TODO*///    /*
/*TODO*///    The scroll RAM must start on a 0x4000 boundary.
/*TODO*///    Some games do not do this.
/*TODO*///    For example:
/*TODO*///       Captain commando     - continue screen will not display
/*TODO*///       Muscle bomber games  - will animate garbage during gameplay
/*TODO*///    Mask out the irrelevant bits.
/*TODO*///    */
/*TODO*///	base &= ~(boundary-1);
/*TODO*/// 	return &cps1_gfxram[base&0x3ffff];
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///

    public static ReadHandlerPtr cps1_output_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /*TODO*///int cps1_output_r(int offset)
/*TODO*///{
/*TODO*///#if VERBOSE
/*TODO*///if (errorlog && offset >= 0x18) fprintf(errorlog,"PC %06x: read output port %02x\n",cpu_get_pc(),offset);
/*TODO*///#endif
/*TODO*///
/*TODO*///	/* Some games interrogate a couple of registers on bootup. */
/*TODO*///	/* These are CPS1 board B self test checks. They wander from game to */
/*TODO*///	/* game. */
/*TODO*///	if (offset && offset == cps1_game_config->cpsb_addr)
/*TODO*///		return cps1_game_config->cpsb_value;
/*TODO*///
/*TODO*///	/* some games use as a protection check the ability to do 16-bit multiplies */
/*TODO*///	/* with a 32-bit result, by writing the factors to two ports and reading the */
/*TODO*///	/* result from two other ports. */
/*TODO*///	if (offset && offset == cps1_game_config->mult_result_lo)
/*TODO*///        return (READ_WORD(&cps1_output[cps1_game_config->mult_factor1]) *
/*TODO*///				READ_WORD(&cps1_output[cps1_game_config->mult_factor2])) & 0xffff;
/*TODO*///	if (offset && offset == cps1_game_config->mult_result_hi)
/*TODO*///		return (READ_WORD(&cps1_output[cps1_game_config->mult_factor1]) *
/*TODO*///				READ_WORD(&cps1_output[cps1_game_config->mult_factor2])) >> 16;
/*TODO*///
/*TODO*///	/* Pang 3 EEPROM interface */
/*TODO*///	if (cps1_game_config->kludge == 5 && offset == 0x7a)
/*TODO*///		return cps1_eeprom_port_r(0);
/*TODO*///
            return cps1_output.READ_WORD(offset);
        }
    };
    public static WriteHandlerPtr cps1_output_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /*TODO*///#if VERBOSE
/*TODO*///if (errorlog && offset >= 0x18 && //offset != 0x22 &&
/*TODO*///		offset != cps1_game_config->layer_control &&
/*TODO*///		offset != cps1_game_config->priority0 &&
/*TODO*///		offset != cps1_game_config->priority1 &&
/*TODO*///		offset != cps1_game_config->priority2 &&
/*TODO*///		offset != cps1_game_config->priority3 &&
/*TODO*///		offset != cps1_game_config->control_reg)
/*TODO*///	fprintf(errorlog,"PC %06x: write %02x to output port %02x\n",cpu_get_pc(),data,offset);
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///if (offset == 0x22 && (data & ~0x8001) != 0x0e)
/*TODO*///{
/*TODO*///	char baf[40];
/*TODO*///	sprintf(baf,"port 22 = %02x",data);
/*TODO*///	usrintf_showmessage(baf);
/*TODO*///}
/*TODO*///if (cps1_game_config->priority0 && offset == cps1_game_config->priority0 && data != 0x00)
/*TODO*///{
/*TODO*///	char baf[40];
/*TODO*///	sprintf(baf,"priority0 %04x",data);
/*TODO*///	usrintf_showmessage(baf);
/*TODO*///}
/*TODO*///if (cps1_game_config->control_reg && offset == cps1_game_config->control_reg && data != 0x3f)
/*TODO*///{
/*TODO*///	char baf[40];
/*TODO*///	sprintf(baf,"control_reg %02x",data);
/*TODO*///	usrintf_showmessage(baf);
/*TODO*///}
/*TODO*///#endif
/*TODO*///#endif
/*TODO*///
/*TODO*///	/* Pang 3 EEPROM interface */
/*TODO*///	if (cps1_game_config->kludge == 5 && offset == 0x7a)
/*TODO*///	{
/*TODO*///		cps1_eeprom_port_w(0,data);
/*TODO*///		return;
/*TODO*///	}
/*TODO*///
/*TODO*///	COMBINE_WORD_MEM(&cps1_output[offset],data);
        }
    };
    /*TODO*///
/*TODO*///
/*TODO*///
/* Public variables */
    public static UBytePtr cps1_gfxram = new UBytePtr();
    public static UBytePtr cps1_output = new UBytePtr();

    public static int[] cps1_gfxram_size = new int[1];
    public static int[] cps1_output_size = new int[1];

    /*TODO*////* Private */
/*TODO*///
/*TODO*////* Offset of each palette entry */
/*TODO*///const int cps1_obj_palette    =0;
/*TODO*///const int cps1_scroll1_palette=32;
/*TODO*///const int cps1_scroll2_palette=32+32;
/*TODO*///const int cps1_scroll3_palette=32+32+32;
/*TODO*///#define cps1_palette_entries (32*4)  /* Number colour schemes in palette */
/*TODO*///
/*TODO*///const int cps1_scroll1_size=0x4000;
/*TODO*///const int cps1_scroll2_size=0x4000;
/*TODO*///const int cps1_scroll3_size=0x4000;
/*TODO*///const int cps1_obj_size    =0x0800;
/*TODO*///const int cps1_other_size  =0x0800;
/*TODO*///const int cps1_palette_size=cps1_palette_entries*32; /* Size of palette RAM */
/*TODO*///static int cps1_flip_screen;    /* Flip screen on / off */
/*TODO*///
/*TODO*///static unsigned char *cps1_scroll1;
/*TODO*///static unsigned char *cps1_scroll2;
/*TODO*///static unsigned char *cps1_scroll3;
/*TODO*///static unsigned char *cps1_obj;
/*TODO*///static unsigned char *cps1_buffered_obj;
/*TODO*///static unsigned char *cps1_palette;
/*TODO*///static unsigned char *cps1_other;
/*TODO*///static unsigned char *cps1_old_palette;
/*TODO*///
/*TODO*////* Working variables */
/*TODO*///static int cps1_last_sprite_offset;     /* Offset of the last sprite */
/*TODO*///static int cps1_layer_enabled[4];       /* Layer enabled [Y/N] */
/*TODO*///
/*TODO*///int scroll1x, scroll1y, scroll2x, scroll2y, scroll3x, scroll3y;
/*TODO*///static unsigned char *cps1_scroll2_old;
/*TODO*///static struct osd_bitmap *cps1_scroll2_bitmap;
/*TODO*///
/*TODO*///
/*TODO*////* Output ports */
/*TODO*///#define CPS1_OBJ_BASE			0x00    /* Base address of objects */
/*TODO*///#define CPS1_SCROLL1_BASE       0x02    /* Base address of scroll 1 */
/*TODO*///#define CPS1_SCROLL2_BASE       0x04    /* Base address of scroll 2 */
/*TODO*///#define CPS1_SCROLL3_BASE       0x06    /* Base address of scroll 3 */
/*TODO*///#define CPS1_OTHER_BASE			0x08    /* Base address of other video */
/*TODO*///#define CPS1_PALETTE_BASE       0x0a    /* Base address of palette */
/*TODO*///#define CPS1_SCROLL1_SCROLLX    0x0c    /* Scroll 1 X */
/*TODO*///#define CPS1_SCROLL1_SCROLLY    0x0e    /* Scroll 1 Y */
/*TODO*///#define CPS1_SCROLL2_SCROLLX    0x10    /* Scroll 2 X */
/*TODO*///#define CPS1_SCROLL2_SCROLLY    0x12    /* Scroll 2 Y */
/*TODO*///#define CPS1_SCROLL3_SCROLLX    0x14    /* Scroll 3 X */
/*TODO*///#define CPS1_SCROLL3_SCROLLY    0x16    /* Scroll 3 Y */
/*TODO*///
/*TODO*///#define CPS1_ROWSCROLL_OFFS     0x20    /* base of row scroll offsets in other RAM */
/*TODO*///
/*TODO*///#define CPS1_SCROLL2_WIDTH      0x40
/*TODO*///#define CPS1_SCROLL2_HEIGHT     0x40
/*TODO*///
/*TODO*///
/*TODO*////*
/*TODO*///CPS1 VIDEO RENDERER
/*TODO*///
/*TODO*///*/
/*TODO*///static UINT32 *cps1_gfx;		 /* Converted GFX memory */
/*TODO*///static int *cps1_char_pen_usage;	/* pen usage array */
/*TODO*///static int *cps1_tile16_pen_usage;      /* pen usage array */
/*TODO*///static int *cps1_tile32_pen_usage;      /* pen usage array */
/*TODO*///static int cps1_max_char;	       /* Maximum number of 8x8 chars */
/*TODO*///static int cps1_max_tile16;	     /* Maximum number of 16x16 tiles */
/*TODO*///static int cps1_max_tile32;	     /* Maximum number of 32x32 tiles */
/*TODO*///
/*TODO*///int cps1_gfx_start(void)
/*TODO*///{
/*TODO*///	UINT32 dwval;
/*TODO*///	int size=memory_region_length(REGION_GFX1);
/*TODO*///	unsigned char *data = memory_region(REGION_GFX1);
/*TODO*///	int i,j,nchar,penusage,gfxsize;
/*TODO*///
/*TODO*///	gfxsize=size/4;
/*TODO*///
/*TODO*///	/* Set up maximum values */
/*TODO*///	cps1_max_char  =(gfxsize/2)/8;
/*TODO*///	cps1_max_tile16=(gfxsize/4)/8;
/*TODO*///	cps1_max_tile32=(gfxsize/16)/8;
/*TODO*///
/*TODO*///	cps1_gfx=malloc(gfxsize*sizeof(UINT32));
/*TODO*///	if (!cps1_gfx)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	cps1_char_pen_usage=malloc(cps1_max_char*sizeof(int));
/*TODO*///	if (!cps1_char_pen_usage)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	memset(cps1_char_pen_usage, 0, cps1_max_char*sizeof(int));
/*TODO*///
/*TODO*///	cps1_tile16_pen_usage=malloc(cps1_max_tile16*sizeof(int));
/*TODO*///	if (!cps1_tile16_pen_usage)
/*TODO*///		return -1;
/*TODO*///	memset(cps1_tile16_pen_usage, 0, cps1_max_tile16*sizeof(int));
/*TODO*///
/*TODO*///	cps1_tile32_pen_usage=malloc(cps1_max_tile32*sizeof(int));
/*TODO*///	if (!cps1_tile32_pen_usage)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	memset(cps1_tile32_pen_usage, 0, cps1_max_tile32*sizeof(int));
/*TODO*///
/*TODO*///	{
/*TODO*///		for (i=0; i<gfxsize/2; i++)
/*TODO*///		{
/*TODO*///			nchar=i/8;  /* 8x8 char number */
/*TODO*///		   dwval=0;
/*TODO*///		   for (j=0; j<8; j++)
/*TODO*///		   {
/*TODO*///				int n,mask;
/*TODO*///				n=0;
/*TODO*///				mask=0x80>>j;
/*TODO*///				if (*(data+size/4)&mask)	   n|=1;
/*TODO*///				if (*(data+size/4+1)&mask)	 n|=2;
/*TODO*///				if (*(data+size/2+size/4)&mask)    n|=4;
/*TODO*///				if (*(data+size/2+size/4+1)&mask)  n|=8;
/*TODO*///				dwval|=n<<(28-j*4);
/*TODO*///				penusage=1<<n;
/*TODO*///				cps1_char_pen_usage[nchar]|=penusage;
/*TODO*///				cps1_tile16_pen_usage[nchar/2]|=penusage;
/*TODO*///				cps1_tile32_pen_usage[nchar/8]|=penusage;
/*TODO*///		   }
/*TODO*///		   cps1_gfx[2*i]=dwval;
/*TODO*///		   dwval=0;
/*TODO*///		   for (j=0; j<8; j++)
/*TODO*///		   {
/*TODO*///				int n,mask;
/*TODO*///				n=0;
/*TODO*///				mask=0x80>>j;
/*TODO*///				if (*(data)&mask)	  n|=1;
/*TODO*///				if (*(data+1)&mask)	n|=2;
/*TODO*///				if (*(data+size/2)&mask)   n|=4;
/*TODO*///				if (*(data+size/2+1)&mask) n|=8;
/*TODO*///				dwval|=n<<(28-j*4);
/*TODO*///				penusage=1<<n;
/*TODO*///				cps1_char_pen_usage[nchar]|=penusage;
/*TODO*///				cps1_tile16_pen_usage[nchar/2]|=penusage;
/*TODO*///				cps1_tile32_pen_usage[nchar/8]|=penusage;
/*TODO*///		   }
/*TODO*///		   cps1_gfx[2*i+1]=dwval;
/*TODO*///		   data+=2;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return 0;
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_gfx_stop(void)
/*TODO*///{
/*TODO*///	if (cps1_gfx)
/*TODO*///	{
/*TODO*///		free(cps1_gfx);
/*TODO*///	}
/*TODO*///	if (cps1_char_pen_usage)
/*TODO*///	{
/*TODO*///		free(cps1_char_pen_usage);
/*TODO*///	}
/*TODO*///	if (cps1_tile16_pen_usage)
/*TODO*///	{
/*TODO*///		free(cps1_tile16_pen_usage);
/*TODO*///	}
/*TODO*///	if (cps1_tile32_pen_usage)
/*TODO*///	{
/*TODO*///		free(cps1_tile32_pen_usage);
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cps1_draw_gfx(
/*TODO*///	struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///	unsigned int code,
/*TODO*///	int color,
/*TODO*///	int flipx,int flipy,
/*TODO*///	int sx,int sy,
/*TODO*///	int tpens,
/*TODO*///	int *pusage,
/*TODO*///	const int size,
/*TODO*///	const int max,
/*TODO*///	const int delta,
/*TODO*///	const int srcdelta)
/*TODO*///{
/*TODO*///	#define DATATYPE unsigned char
/*TODO*///	#define IF_NOT_TRANSPARENT(n) if (tpens & (0x01 << n))
/*TODO*///	#include "cps1draw.c"
/*TODO*///	#undef DATATYPE
/*TODO*///	#undef IF_NOT_TRANSPARENT
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_draw_gfx16(
/*TODO*///	struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///	unsigned int code,
/*TODO*///	int color,
/*TODO*///	int flipx,int flipy,
/*TODO*///	int sx,int sy,
/*TODO*///	int tpens,
/*TODO*///	int *pusage,
/*TODO*///	const int size,
/*TODO*///	const int max,
/*TODO*///	const int delta,
/*TODO*///	const int srcdelta)
/*TODO*///{
/*TODO*///	#define DATATYPE unsigned short
/*TODO*///	#define IF_NOT_TRANSPARENT(n) if (tpens & (0x01 << n))
/*TODO*///	#include "cps1draw.c"
/*TODO*///	#undef DATATYPE
/*TODO*///	#undef IF_NOT_TRANSPARENT
/*TODO*///}
/*TODO*///
/*TODO*////*
/*TODO*///
/*TODO*///This is an optimized version that doesn't take into account transparency
/*TODO*///
/*TODO*///Draws complete tiles without checking transparency. Used for scroll 2 low
/*TODO*///priority rendering.
/*TODO*///
/*TODO*///*/
/*TODO*///void cps1_draw_gfx_opaque(
/*TODO*///	struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///	unsigned int code,
/*TODO*///	int color,
/*TODO*///	int flipx,int flipy,
/*TODO*///	int sx,int sy,
/*TODO*///	int tpens,
/*TODO*///	int *pusage,
/*TODO*///	const int size,
/*TODO*///	const int max,
/*TODO*///	const int delta,
/*TODO*///	const int srcdelta)
/*TODO*///{
/*TODO*///	#define DATATYPE unsigned char
/*TODO*///	#define IF_NOT_TRANSPARENT(n)
/*TODO*///	#include "cps1draw.c"
/*TODO*///	#undef DATATYPE
/*TODO*///	#undef IF_NOT_TRANSPARENT
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_draw_gfx_opaque16(
/*TODO*///	struct osd_bitmap *dest,const struct GfxElement *gfx,
/*TODO*///	unsigned int code,
/*TODO*///	int color,
/*TODO*///	int flipx,int flipy,
/*TODO*///	int sx,int sy,
/*TODO*///	int tpens,
/*TODO*///	int *pusage,
/*TODO*///	const int size,
/*TODO*///	const int max,
/*TODO*///	const int delta,
/*TODO*///	const int srcdelta)
/*TODO*///{
/*TODO*///	#define DATATYPE unsigned short
/*TODO*///	#define IF_NOT_TRANSPARENT(n)
/*TODO*///	#include "cps1draw.c"
/*TODO*///	#undef DATATYPE
/*TODO*///	#undef IF_NOT_TRANSPARENT
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE void cps1_draw_scroll1(
/*TODO*///	struct osd_bitmap *dest,
/*TODO*///	unsigned int code, int color,
/*TODO*///	int flipx, int flipy,int sx, int sy, int tpens)
/*TODO*///{
/*TODO*///    if (Machine->scrbitmap->depth==16)
/*TODO*///    {
/*TODO*///        cps1_draw_gfx16(dest,
/*TODO*///            Machine->gfx[0],
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_char_pen_usage,8, cps1_max_char, 16, 1);
/*TODO*///    }
/*TODO*///    else
/*TODO*///    {
/*TODO*///        cps1_draw_gfx(dest,
/*TODO*///            Machine->gfx[0],
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_char_pen_usage,8, cps1_max_char, 16, 1);
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void cps1_draw_tile16(struct osd_bitmap *dest,
/*TODO*///	const struct GfxElement *gfx,
/*TODO*///	unsigned int code, int color,
/*TODO*///	int flipx, int flipy,int sx, int sy, int tpens)
/*TODO*///{
/*TODO*///    if (Machine->scrbitmap->depth==16)
/*TODO*///    {
/*TODO*///        cps1_draw_gfx16(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_tile16_pen_usage,16, cps1_max_tile16, 16*2,0);
/*TODO*///    }
/*TODO*///    else
/*TODO*///    {
/*TODO*///        cps1_draw_gfx(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_tile16_pen_usage,16, cps1_max_tile16, 16*2,0);
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///INLINE void cps1_draw_tile32(struct osd_bitmap *dest,
/*TODO*///	const struct GfxElement *gfx,
/*TODO*///	unsigned int code, int color,
/*TODO*///	int flipx, int flipy,int sx, int sy, int tpens)
/*TODO*///{
/*TODO*///    if (Machine->scrbitmap->depth==16)
/*TODO*///    {
/*TODO*///        cps1_draw_gfx16(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_tile32_pen_usage,32, cps1_max_tile32, 16*2*4,0);
/*TODO*///    }
/*TODO*///    else
/*TODO*///    {
/*TODO*///        cps1_draw_gfx(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_tile32_pen_usage,32, cps1_max_tile32, 16*2*4,0);
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///INLINE void cps1_draw_blank16(struct osd_bitmap *dest, int sx, int sy )
/*TODO*///{
/*TODO*///    int i,j;
/*TODO*///
/*TODO*///	if (Machine->orientation & ORIENTATION_SWAP_XY)
/*TODO*///	{
/*TODO*///		int temp;
/*TODO*///		temp=sx;
/*TODO*///		sx=sy;
/*TODO*///        sy=dest->height-temp-16;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (cps1_flip_screen)
/*TODO*///	{
/*TODO*///		/* Handle flipped screen */
/*TODO*///		sx=dest->width-sx-16;
/*TODO*///		sy=dest->height-sy-16;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (Machine->scrbitmap->depth==16)
/*TODO*///    {
/*TODO*///        for (i=15; i>=0; i--)
/*TODO*///		{
/*TODO*///			register unsigned short *bm=(unsigned short *)dest->line[sy+i]+sx;
/*TODO*///			for (j=15; j>=0; j--)
/*TODO*///			{
/*TODO*///				*bm=palette_transparent_pen;
/*TODO*///				bm++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///    }
/*TODO*///    else
/*TODO*///    {
/*TODO*///        for (i=15; i>=0; i--)
/*TODO*///		{
/*TODO*///			register unsigned char *bm=dest->line[sy+i]+sx;
/*TODO*///			for (j=15; j>=0; j--)
/*TODO*///			{
/*TODO*///				*bm=palette_transparent_pen;
/*TODO*///				bm++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///INLINE void cps1_draw_tile16_bmp(struct osd_bitmap *dest,
/*TODO*///	const struct GfxElement *gfx,
/*TODO*///	unsigned int code, int color,
/*TODO*///    int flipx, int flipy,int sx, int sy)
/*TODO*///{
/*TODO*///    if (Machine->scrbitmap->depth==16)
/*TODO*///    {
/*TODO*///        cps1_draw_gfx_opaque16(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            -1,cps1_tile16_pen_usage,16, cps1_max_tile16, 16*2,0);
/*TODO*///    }
/*TODO*///    else
/*TODO*///    {
/*TODO*///        cps1_draw_gfx_opaque(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            -1,cps1_tile16_pen_usage,16, cps1_max_tile16, 16*2,0);
/*TODO*///    }
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///static int cps1_transparency_scroll[4];
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///#if CPS1_DUMP_VIDEO
/*TODO*///void cps1_dump_video(void)
/*TODO*///{
/*TODO*///	FILE *fp;
/*TODO*///	fp=fopen("SCROLL1.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_scroll1, cps1_scroll1_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///	fp=fopen("SCROLL2.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_scroll2, cps1_scroll2_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///	fp=fopen("SCROLL3.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_scroll3, cps1_scroll3_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///	fp=fopen("OBJ.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_obj, cps1_obj_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///
/*TODO*///	fp=fopen("OTHER.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_other, cps1_other_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///
/*TODO*///	fp=fopen("PALETTE.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_palette, cps1_palette_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///
/*TODO*///	fp=fopen("OUTPUT.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_output, cps1_output_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///	fp=fopen("VIDEO.DMP", "w+b");
/*TODO*///	if (fp)
/*TODO*///	{
/*TODO*///		fwrite(cps1_gfxram, cps1_gfxram_size, 1, fp);
/*TODO*///		fclose(fp);
/*TODO*///	}
/*TODO*///
/*TODO*///}
/*TODO*///#endif
/*TODO*///
/*TODO*///
/*TODO*///INLINE void cps1_get_video_base(void )
/*TODO*///{
/*TODO*///	int layercontrol;
/*TODO*///
/*TODO*///	/* Re-calculate the VIDEO RAM base */
/*TODO*///    cps1_scroll1=cps1_base(CPS1_SCROLL1_BASE,cps1_scroll1_size);
/*TODO*///    cps1_scroll2=cps1_base(CPS1_SCROLL2_BASE,cps1_scroll2_size);
/*TODO*///    cps1_scroll3=cps1_base(CPS1_SCROLL3_BASE,cps1_scroll3_size);
/*TODO*///    cps1_obj=cps1_base(CPS1_OBJ_BASE,cps1_obj_size);
/*TODO*///    cps1_palette=cps1_base(CPS1_PALETTE_BASE,cps1_palette_size);
/*TODO*///    cps1_other=cps1_base(CPS1_OTHER_BASE,cps1_other_size);
/*TODO*///
/*TODO*///    /* Get scroll values */
/*TODO*///    scroll1x=cps1_port(CPS1_SCROLL1_SCROLLX);
/*TODO*///    scroll1y=cps1_port(CPS1_SCROLL1_SCROLLY);
/*TODO*///    scroll2x=cps1_port(CPS1_SCROLL2_SCROLLX);
/*TODO*///    scroll2y=cps1_port(CPS1_SCROLL2_SCROLLY);
/*TODO*///    scroll3x=cps1_port(CPS1_SCROLL3_SCROLLX);
/*TODO*///    scroll3y=cps1_port(CPS1_SCROLL3_SCROLLY);
/*TODO*///
/*TODO*///	/* Get transparency registers */
/*TODO*///	if (cps1_game_config->priority1)
/*TODO*///	{
/*TODO*///        cps1_transparency_scroll[0]=cps1_port(cps1_game_config->priority0);
/*TODO*///        cps1_transparency_scroll[1]=cps1_port(cps1_game_config->priority1);
/*TODO*///        cps1_transparency_scroll[2]=cps1_port(cps1_game_config->priority2);
/*TODO*///        cps1_transparency_scroll[3]=cps1_port(cps1_game_config->priority3);
/*TODO*///    }
/*TODO*///
/*TODO*///	/* Get layer enable bits */
/*TODO*///    layercontrol=cps1_port(cps1_game_config->layer_control);
/*TODO*///	cps1_layer_enabled[0]=1;
/*TODO*///	cps1_layer_enabled[1]=layercontrol & cps1_game_config->scrl1_enable_mask;
/*TODO*///	cps1_layer_enabled[2]=layercontrol & cps1_game_config->scrl2_enable_mask;
/*TODO*///	cps1_layer_enabled[3]=layercontrol & cps1_game_config->scrl3_enable_mask;
/*TODO*///
/*TODO*///
/*TODO*///#ifdef MAME_DEBUG
/*TODO*///{
/*TODO*///	char baf[40];
/*TODO*///	int enablemask;
/*TODO*///
/*TODO*///if (keyboard_pressed(KEYCODE_Z))
/*TODO*///{
/*TODO*///	if (keyboard_pressed(KEYCODE_Q)) cps1_layer_enabled[3]=0;
/*TODO*///	if (keyboard_pressed(KEYCODE_W)) cps1_layer_enabled[2]=0;
/*TODO*///	if (keyboard_pressed(KEYCODE_E)) cps1_layer_enabled[1]=0;
/*TODO*///	if (keyboard_pressed(KEYCODE_R)) cps1_layer_enabled[0]=0;
/*TODO*///	if (keyboard_pressed(KEYCODE_T))
/*TODO*///	{
/*TODO*///		sprintf(baf,"%d %d %d %d layer %02x",
/*TODO*///			(layercontrol>>0x06)&03,
/*TODO*///			(layercontrol>>0x08)&03,
/*TODO*///			(layercontrol>>0x0a)&03,
/*TODO*///			(layercontrol>>0x0c)&03,
/*TODO*///			layercontrol&0xc03f
/*TODO*///			);
/*TODO*///		usrintf_showmessage(baf);
/*TODO*///	}
/*TODO*///
/*TODO*///}
/*TODO*///
/*TODO*///	enablemask = 0;
/*TODO*///	if (cps1_game_config->scrl1_enable_mask == cps1_game_config->scrl2_enable_mask)
/*TODO*///		enablemask = cps1_game_config->scrl1_enable_mask;
/*TODO*///	if (cps1_game_config->scrl1_enable_mask == cps1_game_config->scrl3_enable_mask)
/*TODO*///		enablemask = cps1_game_config->scrl1_enable_mask;
/*TODO*///	if (cps1_game_config->scrl2_enable_mask == cps1_game_config->scrl3_enable_mask)
/*TODO*///		enablemask = cps1_game_config->scrl2_enable_mask;
/*TODO*///	if (enablemask)
/*TODO*///	{
/*TODO*///		if (((layercontrol & enablemask) && (layercontrol & enablemask) != enablemask))
/*TODO*///		{
/*TODO*///			sprintf(baf,"layer %02x",layercontrol&0xc03f);
/*TODO*///			usrintf_showmessage(baf);
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	enablemask = cps1_game_config->scrl1_enable_mask | cps1_game_config->scrl2_enable_mask | cps1_game_config->scrl3_enable_mask;
/*TODO*///#if 0
/*TODO*///	if (((layercontrol & ~enablemask) & 0xc03e) != 0)
/*TODO*///	{
/*TODO*///		sprintf(baf,"layer %02x",layercontrol&0xc03f);
/*TODO*///		usrintf_showmessage(baf);
/*TODO*///   }
/*TODO*///   #endif
/*TODO*///}
/*TODO*///#endif
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Start the video hardware emulation.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
    public static VhStartPtr cps1_vh_start = new VhStartPtr() {
        public int handler() {
            /*TODO*///	int i;
/*TODO*///
/*TODO*///	cps1_init_machine();
/*TODO*///
/*TODO*///	if (cps1_gfx_start())
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///	cps1_scroll2_bitmap=osd_new_bitmap(CPS1_SCROLL2_WIDTH*16,
/*TODO*///		CPS1_SCROLL2_HEIGHT*16, Machine->scrbitmap->depth );
/*TODO*///	if (!cps1_scroll2_bitmap)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	cps1_scroll2_old=malloc(cps1_scroll2_size);
/*TODO*///	if (!cps1_scroll2_old)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
/*TODO*///
/*TODO*///
/*TODO*///	cps1_old_palette=(unsigned char *)malloc(cps1_palette_size);
/*TODO*///	if (!cps1_old_palette)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	memset(cps1_old_palette, 0x00, cps1_palette_size);
/*TODO*///	for (i = 0;i < cps1_palette_entries*16;i++)
/*TODO*///	{
/*TODO*///	   palette_change_color(i,0,0,0);
/*TODO*///	}
/*TODO*///
/*TODO*///	cps1_buffered_obj = malloc (cps1_obj_size);
/*TODO*///	if (!cps1_buffered_obj)
/*TODO*///	{
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///	memset(cps1_buffered_obj, 0x00, cps1_obj_size);
/*TODO*///
/*TODO*///	memset(cps1_gfxram, 0, cps1_gfxram_size);   /* Clear GFX RAM */
/*TODO*///	memset(cps1_output, 0, cps1_output_size);   /* Clear output ports */
/*TODO*///
/*TODO*///	/* Put in some defaults */
/*TODO*///	WRITE_WORD(&cps1_output[0x00], 0x9200);
/*TODO*///	WRITE_WORD(&cps1_output[0x02], 0x9000);
/*TODO*///	WRITE_WORD(&cps1_output[0x04], 0x9040);
/*TODO*///	WRITE_WORD(&cps1_output[0x06], 0x9080);
/*TODO*///	WRITE_WORD(&cps1_output[0x08], 0x9100);
/*TODO*///	WRITE_WORD(&cps1_output[0x0a], 0x90c0);
/*TODO*///
/*TODO*///	if (!cps1_game_config)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///		{
/*TODO*///			fprintf(errorlog, "cps1_game_config hasn't been set up yet");
/*TODO*///		}
/*TODO*///		return -1;
/*TODO*///	}
/*TODO*///
/*TODO*///
/*TODO*///	/* Set up old base */
/*TODO*///	cps1_get_video_base();   /* Calculate base pointers */
/*TODO*///	cps1_get_video_base();   /* Calculate old base pointers */
/*TODO*///
/*TODO*///
/*TODO*///	for (i=0; i<4; i++)
/*TODO*///	{
/*TODO*///		cps1_transparency_scroll[i]=0x0000;
/*TODO*///	}
            return 0;
        }
    };
    /*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Stop the video hardware emulation.
/*TODO*///
/*TODO*///***************************************************************************/
    public static VhStopPtr cps1_vh_stop = new VhStopPtr() {
        public void handler() {
            /*TODO*///	if (cps1_old_palette)
/*TODO*///		free(cps1_old_palette);
/*TODO*///	if (cps1_scroll2_bitmap)
/*TODO*///		osd_free_bitmap(cps1_scroll2_bitmap);
/*TODO*///	if (cps1_scroll2_old)
/*TODO*///		free(cps1_scroll2_old);
/*TODO*///	if (cps1_buffered_obj)
/*TODO*///		free(cps1_buffered_obj);
/*TODO*///	cps1_gfx_stop();
        }
    };
    /*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Build palette from palette RAM
/*TODO*///
/*TODO*///  12 bit RGB with a 4 bit brightness value.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///void cps1_build_palette(void)
/*TODO*///{
/*TODO*///	int offset;
/*TODO*///
/*TODO*///	for (offset = 0; offset < cps1_palette_entries*16; offset++)
/*TODO*///	{
/*TODO*///		int palette = READ_WORD (&cps1_palette[offset * 2]);
/*TODO*///
/*TODO*///		if (palette != READ_WORD (&cps1_old_palette[offset * 2]) )
/*TODO*///		{
/*TODO*///		   int red, green, blue, bright;
/*TODO*///
/*TODO*///		   bright= (palette>>12);
/*TODO*///		   if (bright) bright += 2;
/*TODO*///
/*TODO*///		   red   = ((palette>>8)&0x0f) * bright;
/*TODO*///		   green = ((palette>>4)&0x0f) * bright;
/*TODO*///		   blue  = (palette&0x0f) * bright;
/*TODO*///
/*TODO*///		   palette_change_color (offset, red, green, blue);
/*TODO*///		   WRITE_WORD(&cps1_old_palette[offset * 2], palette);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Scroll 1 (8x8)
/*TODO*///
/*TODO*///  Attribute word layout:
/*TODO*///  0x0001	colour
/*TODO*///  0x0002	colour
/*TODO*///  0x0004	colour
/*TODO*///  0x0008	colour
/*TODO*///  0x0010	colour
/*TODO*///  0x0020	X Flip
/*TODO*///  0x0040	Y Flip
/*TODO*///  0x0080
/*TODO*///  0x0100
/*TODO*///  0x0200
/*TODO*///  0x0400
/*TODO*///  0x0800
/*TODO*///  0x1000
/*TODO*///  0x2000
/*TODO*///  0x4000
/*TODO*///  0x8000
/*TODO*///
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///INLINE void cps1_palette_scroll1(unsigned short *base)
/*TODO*///{
/*TODO*///	int x,y, offs, offsx;
/*TODO*///
/*TODO*///	int scrlxrough=(scroll1x>>3)+8;
/*TODO*///	int scrlyrough=(scroll1y>>3);
/*TODO*///	int basecode=cps1_game_config->bank_scroll1*0x08000;
/*TODO*///
/*TODO*///	for (x=0; x<0x36; x++)
/*TODO*///	{
/*TODO*///		 offsx=(scrlxrough+x)*0x80;
/*TODO*///		 offsx&=0x1fff;
/*TODO*///
/*TODO*///		 for (y=0; y<0x20; y++)
/*TODO*///		 {
/*TODO*///			int code, colour, offsy;
/*TODO*///			int n=scrlyrough+y;
/*TODO*///			offsy=( (n&0x1f)*4 | ((n&0x20)*0x100)) & 0x3fff;
/*TODO*///			offs=offsy+offsx;
/*TODO*///			offs &= 0x3fff;
/*TODO*///			code=basecode+READ_WORD(&cps1_scroll1[offs]);
/*TODO*///			colour=READ_WORD(&cps1_scroll1[offs+2]);
/*TODO*///			if (code < cps1_max_char)
/*TODO*///			{
/*TODO*///				base[colour&0x1f] |=
/*TODO*///					  cps1_char_pen_usage[code]&0x7fff;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_render_scroll1(struct osd_bitmap *bitmap,int priority)
/*TODO*///{
/*TODO*///	int x,y, offs, offsx, sx, sy, ytop;
/*TODO*///
/*TODO*///	int scrlxrough=(scroll1x>>3)+4;
/*TODO*///	int scrlyrough=(scroll1y>>3);
/*TODO*///	int base=cps1_game_config->bank_scroll1*0x08000;
/*TODO*///	const int spacechar=cps1_game_config->space_scroll1;
/*TODO*///
/*TODO*///
/*TODO*///	sx=-(scroll1x&0x07);
/*TODO*///	ytop=-(scroll1y&0x07)+32;
/*TODO*///
/*TODO*///	for (x=0; x<0x35; x++)
/*TODO*///	{
/*TODO*///		 sy=ytop;
/*TODO*///		 offsx=(scrlxrough+x)*0x80;
/*TODO*///		 offsx&=0x1fff;
/*TODO*///
/*TODO*///		 for (y=0; y<0x20; y++)
/*TODO*///		 {
/*TODO*///			int code, offsy, colour;
/*TODO*///			int n=scrlyrough+y;
/*TODO*///			offsy=( (n&0x1f)*4 | ((n&0x20)*0x100)) & 0x3fff;
/*TODO*///			offs=offsy+offsx;
/*TODO*///			offs &= 0x3fff;
/*TODO*///
/*TODO*///			code  =READ_WORD(&cps1_scroll1[offs]);
/*TODO*///			colour=READ_WORD(&cps1_scroll1[offs+2]);
/*TODO*///
/*TODO*///			if (code != 0x20 && code != spacechar)
/*TODO*///			{
/*TODO*///				int transp;
/*TODO*///
/*TODO*///				/* 0x0020 appears to never be drawn */
/*TODO*///				if (priority)
/*TODO*///					transp=cps1_transparency_scroll[(colour & 0x0180)>>7];
/*TODO*///				else transp = 0x7fff;
/*TODO*///
/*TODO*///				cps1_draw_scroll1(bitmap,
/*TODO*///						 code+base,
/*TODO*///						 colour&0x1f,
/*TODO*///						 colour&0x20,
/*TODO*///						 colour&0x40,
/*TODO*///						 sx,sy,transp);
/*TODO*///			 }
/*TODO*///			 sy+=8;
/*TODO*///		 }
/*TODO*///		 sx+=8;
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///								Sprites
/*TODO*///								=======
/*TODO*///
/*TODO*///  Sprites are represented by a number of 8 byte values
/*TODO*///
/*TODO*///  xx xx yy yy nn nn aa aa
/*TODO*///
/*TODO*///  where xxxx = x position
/*TODO*///		yyyy = y position
/*TODO*///		nnnn = tile number
/*TODO*///		aaaa = attribute word
/*TODO*///					0x0001	colour
/*TODO*///					0x0002	colour
/*TODO*///					0x0004	colour
/*TODO*///					0x0008	colour
/*TODO*///					0x0010	colour
/*TODO*///					0x0020	X Flip
/*TODO*///					0x0040	Y Flip
/*TODO*///					0x0080	unknown
/*TODO*///					0x0100	X block size (in sprites)
/*TODO*///					0x0200	X block size
/*TODO*///					0x0400	X block size
/*TODO*///					0x0800	X block size
/*TODO*///					0x1000	Y block size (in sprites)
/*TODO*///					0x2000	Y block size
/*TODO*///					0x4000	Y block size
/*TODO*///					0x8000	Y block size
/*TODO*///
/*TODO*///  The end of the table (may) be marked by an attribute value of 0xff00.
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///void cps1_find_last_sprite(void)    /* Find the offset of last sprite */
/*TODO*///{
/*TODO*///	int offset=6;
/*TODO*///	/* Locate the end of table marker */
/*TODO*///	while (offset < cps1_obj_size)
/*TODO*///	{
/*TODO*///		int colour=READ_WORD(&cps1_buffered_obj[offset]);
/*TODO*///		if (colour == 0xff00)
/*TODO*///		{
/*TODO*///			/* Marker found. This is the last sprite. */
/*TODO*///			cps1_last_sprite_offset=offset-6-8;
/*TODO*///			return;
/*TODO*///		}
/*TODO*///		offset+=8;
/*TODO*///	}
/*TODO*///	/* Sprites must use full sprite RAM */
/*TODO*///	cps1_last_sprite_offset=cps1_obj_size-8;
/*TODO*///}
/*TODO*///
/*TODO*////* Find used colours */
/*TODO*///void cps1_palette_sprites(unsigned short *base)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///
/*TODO*///	for (i=cps1_last_sprite_offset; i>=0; i-=8)
/*TODO*///	{
/*TODO*///		int x=READ_WORD(&cps1_buffered_obj[i]);
/*TODO*///		int y=READ_WORD(&cps1_buffered_obj[i+2]);
/*TODO*///		if (x && y)
/*TODO*///		{
/*TODO*///			int colour=READ_WORD(&cps1_buffered_obj[i+6]);
/*TODO*///			int col=colour&0x1f;
/*TODO*///			unsigned int code=READ_WORD(&cps1_buffered_obj[i+4]);
/*TODO*///			if (cps1_game_config->kludge == 7)
/*TODO*///			{
/*TODO*///			       code += 0x4000;
/*TODO*///			}
/*TODO*///			if (cps1_game_config->kludge == 1 && code >= 0x01000)
/*TODO*///			{
/*TODO*///			       code += 0x4000;
/*TODO*///			}
/*TODO*///			if (cps1_game_config->kludge == 2 && code >= 0x02a00)
/*TODO*///			{
/*TODO*///			       code += 0x4000;
/*TODO*///			}
/*TODO*///
/*TODO*///			if ( colour & 0xff00 )
/*TODO*///			{
/*TODO*///				int nys, nxs;
/*TODO*///				int nx=(colour & 0x0f00) >> 8;
/*TODO*///				int ny=(colour & 0xf000) >> 12;
/*TODO*///				nx++;
/*TODO*///				ny++;
/*TODO*///
/*TODO*///				if (colour & 0x40)   /* Y Flip */					      /* Y flip */
/*TODO*///				{
/*TODO*///					if (colour &0x20)
/*TODO*///					{
/*TODO*///					for (nys=0; nys<ny; nys++)
/*TODO*///					{
/*TODO*///						for (nxs=0; nxs<nx; nxs++)
/*TODO*///						{
/*TODO*///							int cod=code+(nx-1)-nxs+0x10*(ny-1-nys);
/*TODO*///							base[col] |=
/*TODO*///							cps1_tile16_pen_usage[cod % cps1_max_tile16];
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					for (nys=0; nys<ny; nys++)
/*TODO*///					{
/*TODO*///						for (nxs=0; nxs<nx; nxs++)
/*TODO*///						{
/*TODO*///							int cod=code+nxs+0x10*(ny-1-nys);
/*TODO*///							base[col] |=
/*TODO*///							cps1_tile16_pen_usage[cod % cps1_max_tile16];
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				if (colour &0x20)
/*TODO*///				{
/*TODO*///					for (nys=0; nys<ny; nys++)
/*TODO*///					{
/*TODO*///						for (nxs=0; nxs<nx; nxs++)
/*TODO*///						{
/*TODO*///							int cod=code+(nx-1)-nxs+0x10*nys;
/*TODO*///							base[col] |=
/*TODO*///							cps1_tile16_pen_usage[cod % cps1_max_tile16];
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					for (nys=0; nys<ny; nys++)
/*TODO*///					{
/*TODO*///						for (nxs=0; nxs<nx; nxs++)
/*TODO*///						{
/*TODO*///							int cod=code+nxs+0x10*nys;
/*TODO*///							base[col] |=
/*TODO*///							cps1_tile16_pen_usage[cod % cps1_max_tile16];
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///			}
/*TODO*///			base[col]&=0x7fff;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				base[col] |=
/*TODO*///				cps1_tile16_pen_usage[code % cps1_max_tile16]&0x7fff;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void cps1_render_sprites(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///    const int mask=0x7fff;
/*TODO*///	int i;
/*TODO*/////mish
/*TODO*///	/* Draw the sprites */
/*TODO*///	for (i=cps1_last_sprite_offset; i>=0; i-=8)
/*TODO*///	{
/*TODO*///		int x=READ_WORD(&cps1_buffered_obj[i]);
/*TODO*///		int y=READ_WORD(&cps1_buffered_obj[i+2]);
/*TODO*///		if (x && y )
/*TODO*///		{
/*TODO*///			unsigned int code=READ_WORD(&cps1_buffered_obj[i+4]);
/*TODO*///			int colour=READ_WORD(&cps1_buffered_obj[i+6]);
/*TODO*///			int col=colour&0x1f;
/*TODO*///
/*TODO*///			y &= 0x1ff;
/*TODO*///			if (y > 450) y -= 0x200;
/*TODO*///
/*TODO*///			/* in cawing, skyscrapers parts on level 2 have all the top bits of the */
/*TODO*///			/* x coordinate set. Does this have a special meaning? */
/*TODO*///			x &= 0x1ff;
/*TODO*///			if (x > 450) x -= 0x200;
/*TODO*///
/*TODO*///			x-=0x20;
/*TODO*///			y+=0x20;
/*TODO*///
/*TODO*///			if (cps1_game_config->kludge == 7)
/*TODO*///			{
/*TODO*///			       code += 0x4000;
/*TODO*///			}
/*TODO*///			if (cps1_game_config->kludge == 1 && code >= 0x01000)
/*TODO*///			{
/*TODO*///			       code += 0x4000;
/*TODO*///			}
/*TODO*///			if (cps1_game_config->kludge == 2 && code >= 0x02a00)
/*TODO*///			{
/*TODO*///			       code += 0x4000;
/*TODO*///			}
/*TODO*///
/*TODO*///			if (colour & 0xff00 )
/*TODO*///			{
/*TODO*///					/* handle blocked sprites */
/*TODO*///					int nx=(colour & 0x0f00) >> 8;
/*TODO*///					int ny=(colour & 0xf000) >> 12;
/*TODO*///					int nxs,nys,sx,sy;
/*TODO*///					nx++;
/*TODO*///					ny++;
/*TODO*///
/*TODO*///					if (colour & 0x40)
/*TODO*///					{
/*TODO*///						/* Y flip */
/*TODO*///						if (colour &0x20)
/*TODO*///						{
/*TODO*///							for (nys=0; nys<ny; nys++)
/*TODO*///							{
/*TODO*///								for (nxs=0; nxs<nx; nxs++)
/*TODO*///								{
/*TODO*///									sx = x+nxs*16;
/*TODO*///									sy = y+nys*16;
/*TODO*///									if (sx > 450) sx -= 0x200;
/*TODO*///									if (sy > 450) sy -= 0x200;
/*TODO*///
/*TODO*///									cps1_draw_tile16(bitmap,Machine->gfx[1],
/*TODO*///										code+(nx-1)-nxs+0x10*(ny-1-nys),
/*TODO*///										col&0x1f,
/*TODO*///										1,1,
/*TODO*///                                        sx,sy,mask);
/*TODO*///								}
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for (nys=0; nys<ny; nys++)
/*TODO*///							{
/*TODO*///								for (nxs=0; nxs<nx; nxs++)
/*TODO*///								{
/*TODO*///									sx = x+nxs*16;
/*TODO*///									sy = y+nys*16;
/*TODO*///									if (sx > 450) sx -= 0x200;
/*TODO*///									if (sy > 450) sy -= 0x200;
/*TODO*///
/*TODO*///									cps1_draw_tile16(bitmap,Machine->gfx[1],
/*TODO*///										code+nxs+0x10*(ny-1-nys),
/*TODO*///										col&0x1f,
/*TODO*///										0,1,
/*TODO*///                                        sx,sy,mask );
/*TODO*///								}
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						if (colour &0x20)
/*TODO*///						{
/*TODO*///							for (nys=0; nys<ny; nys++)
/*TODO*///							{
/*TODO*///								for (nxs=0; nxs<nx; nxs++)
/*TODO*///								{
/*TODO*///									sx = x+nxs*16;
/*TODO*///									sy = y+nys*16;
/*TODO*///									if (sx > 450) sx -= 0x200;
/*TODO*///									if (sy > 450) sy -= 0x200;
/*TODO*///
/*TODO*///									cps1_draw_tile16(bitmap,Machine->gfx[1],
/*TODO*///										code+(nx-1)-nxs+0x10*nys,
/*TODO*///										col&0x1f,
/*TODO*///										1,0,
/*TODO*///                                        sx,sy,mask
/*TODO*///										);
/*TODO*///								}
/*TODO*///							}
/*TODO*///						}
/*TODO*///						else
/*TODO*///						{
/*TODO*///							for (nys=0; nys<ny; nys++)
/*TODO*///							{
/*TODO*///								for (nxs=0; nxs<nx; nxs++)
/*TODO*///								{
/*TODO*///									sx = x+nxs*16;
/*TODO*///									sy = y+nys*16;
/*TODO*///									if (sx > 450) sx -= 0x200;
/*TODO*///									if (sy > 450) sy -= 0x200;
/*TODO*///
/*TODO*///									cps1_draw_tile16(bitmap,Machine->gfx[1],
/*TODO*///										code+nxs+0x10*nys,
/*TODO*///										col&0x1f,
/*TODO*///										0,0,
/*TODO*///                                        sx,sy, mask);
/*TODO*///								}
/*TODO*///							}
/*TODO*///						}
/*TODO*///					}
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					/* Simple case... 1 sprite */
/*TODO*///					cps1_draw_tile16(bitmap,Machine->gfx[1],
/*TODO*///						   code,
/*TODO*///						   col&0x1f,
/*TODO*///						   colour&0x20,colour&0x40,
/*TODO*///                           x,y,mask);
/*TODO*///				}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Scroll 2 (16x16 layer)
/*TODO*///
/*TODO*///  Attribute word layout:
/*TODO*///  0x0001	colour
/*TODO*///  0x0002	colour
/*TODO*///  0x0004	colour
/*TODO*///  0x0008	colour
/*TODO*///  0x0010	colour
/*TODO*///  0x0020	X Flip
/*TODO*///  0x0040	Y Flip
/*TODO*///  0x0080	??? Priority
/*TODO*///  0x0100	??? Priority
/*TODO*///  0x0200
/*TODO*///  0x0400
/*TODO*///  0x0800
/*TODO*///  0x1000
/*TODO*///  0x2000
/*TODO*///  0x4000
/*TODO*///  0x8000
/*TODO*///
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///INLINE void cps1_palette_scroll2(unsigned short *base)
/*TODO*///{
/*TODO*///	int offs, code, colour;
/*TODO*///    int basecode=cps1_game_config->bank_scroll2*0x04000;
/*TODO*///
/*TODO*///	for (offs=cps1_scroll2_size-4; offs>=0; offs-=4)
/*TODO*///	{
/*TODO*///		code=basecode+READ_WORD(&cps1_scroll2[offs]);
/*TODO*///		colour=READ_WORD(&cps1_scroll2[offs+2])&0x1f;
/*TODO*///		if (code < cps1_max_tile16)
/*TODO*///		{
/*TODO*///			base[colour] |= cps1_tile16_pen_usage[code];
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_render_scroll2_bitmap(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	int sx, sy;
/*TODO*///	int ny=(scroll2y>>4);	  /* Rough Y */
/*TODO*///    int base=cps1_game_config->bank_scroll2*0x04000;
/*TODO*///	const int startcode=cps1_game_config->start_scroll2;
/*TODO*///	const int endcode=cps1_game_config->end_scroll2;
/*TODO*///	const int kludge=cps1_game_config->kludge;
/*TODO*///
/*TODO*///    for (sx=CPS1_SCROLL2_WIDTH-1; sx>=0; sx--)
/*TODO*///	{
/*TODO*///		int n=ny;
/*TODO*///        for (sy=0x09*2-1; sy>=0; sy--)
/*TODO*///		{
/*TODO*///			long newvalue;
/*TODO*///			int offsy, offsx, offs, colour, code;
/*TODO*///
/*TODO*///			n&=0x3f;
/*TODO*///			offsy  = ((n&0x0f)*4 | ((n&0x30)*0x100))&0x3fff;
/*TODO*///			offsx=(sx*0x040)&0xfff;
/*TODO*///			offs=offsy+offsx;
/*TODO*///
/*TODO*///			colour=READ_WORD(&cps1_scroll2[offs+2]);
/*TODO*///
/*TODO*///			newvalue=*(long*)(&cps1_scroll2[offs]);
/*TODO*///			if ( newvalue != *(long*)(&cps1_scroll2_old[offs]) )
/*TODO*///			{
/*TODO*///				*(long*)(&cps1_scroll2_old[offs])=newvalue;
/*TODO*///                code=READ_WORD(&cps1_scroll2[offs]);
/*TODO*///				if ( code >= startcode && code <= endcode
/*TODO*///					/*
/*TODO*///					MERCS has an gap in the scroll 2 layout
/*TODO*///					(bad tiles at start of level 2)*/
/*TODO*///					&&	!(kludge == 4 && (code >= 0x1e00 && code < 0x5400))
/*TODO*///					)
/*TODO*///				{
/*TODO*///					code += base;
/*TODO*///					cps1_draw_tile16_bmp(bitmap,
/*TODO*///						Machine->gfx[2],
/*TODO*///						code,
/*TODO*///						colour&0x1f,
/*TODO*///						colour&0x20,colour&0x40,
/*TODO*///                        16*sx, 16*n);
/*TODO*///				}
/*TODO*///				else
/*TODO*///				{
/*TODO*///					cps1_draw_blank16(bitmap, 16*sx, 16*n);
/*TODO*///				}
/*TODO*///				//cps1_print_debug_tile_info(bitmap, 16*sx, 16*n, colour,1);
/*TODO*///			}
/*TODO*///			n++;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cps1_render_scroll2_high(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///#ifdef LAYER_DEBUG
/*TODO*///	static int s=0;
/*TODO*///#endif
/*TODO*///	int sx, sy;
/*TODO*///	int nxoffset=(scroll2x&0x0f)+32;    /* Smooth X */
/*TODO*///	int nyoffset=(scroll2y&0x0f);    /* Smooth Y */
/*TODO*///	int nx=(scroll2x>>4);	  /* Rough X */
/*TODO*///	int ny=(scroll2y>>4)-4;	/* Rough Y */
/*TODO*///    int base=cps1_game_config->bank_scroll2*0x04000;
/*TODO*///
/*TODO*///	for (sx=0; sx<0x32/2+4; sx++)
/*TODO*///	{
/*TODO*///		for (sy=0; sy<0x09*2; sy++)
/*TODO*///		{
/*TODO*///			int offsy, offsx, offs, colour, code, transp;
/*TODO*///			int n;
/*TODO*///			n=ny+sy+2;
/*TODO*///			offsy  = ((n&0x0f)*4 | ((n&0x30)*0x100))&0x3fff;
/*TODO*///			offsx=((nx+sx)*0x040)&0xfff;
/*TODO*///			offs=offsy+offsx;
/*TODO*///			offs &= 0x3fff;
/*TODO*///
/*TODO*///			code=READ_WORD(&cps1_scroll2[offs]);
/*TODO*///			colour=READ_WORD(&cps1_scroll2[offs+2]);
/*TODO*///
/*TODO*///			transp=cps1_transparency_scroll[(colour & 0x0180)>>7];
/*TODO*///
/*TODO*///			cps1_draw_tile16(bitmap,
/*TODO*///						Machine->gfx[2],
/*TODO*///						code+base,
/*TODO*///						colour&0x1f,
/*TODO*///						colour&0x20,colour&0x40,
/*TODO*///						16*sx-nxoffset,
/*TODO*///						16*sy-nyoffset,
/*TODO*///						transp);
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_render_scroll2_low(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///      int scrly=-(scroll2y-0x20);
/*TODO*///      int scrlx=-(scroll2x+0x40-0x20);
/*TODO*///
/*TODO*///      if (cps1_flip_screen)
/*TODO*///      {
/*TODO*///            scrly=(CPS1_SCROLL2_HEIGHT*16)-scrly;
/*TODO*///      }
/*TODO*///
/*TODO*///      cps1_render_scroll2_bitmap(cps1_scroll2_bitmap);
/*TODO*///
/*TODO*///      copyscrollbitmap(bitmap,cps1_scroll2_bitmap,1,&scrlx,1,&scrly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cps1_render_scroll2_distort(struct osd_bitmap *bitmap)
/*TODO*///{
/*TODO*///	int scrly=-scroll2y;
/*TODO*///	int i,scrollx[1024];
/*TODO*///	int otheroffs;
/*TODO*///
/*TODO*////*
/*TODO*///	Games known to use row scrolling:
/*TODO*///
/*TODO*///	SF2
/*TODO*///	Mega Twins (underwater, cave)
/*TODO*///	Carrier Air Wing (hazy background at beginning of mission 8)
/*TODO*///	Magic Sword (fire on floor 3; screen distort after continue)
/*TODO*///	Varth (title screen)
/*TODO*///*/
/*TODO*///
/*TODO*///	if (cps1_flip_screen)
/*TODO*///		scrly=(CPS1_SCROLL2_HEIGHT*16)-scrly;
/*TODO*///
/*TODO*///	cps1_render_scroll2_bitmap(cps1_scroll2_bitmap);
/*TODO*///
/*TODO*///	otheroffs = cps1_port(CPS1_ROWSCROLL_OFFS);
/*TODO*///
/*TODO*///	for (i = 0;i < 256;i++)
/*TODO*///		scrollx[(i - scrly) & 0x3ff] = -(scroll2x+0x40-0x20) - READ_WORD(&cps1_other[(2*(i + otheroffs)) & 0x7ff]);
/*TODO*///
/*TODO*///	scrly+=0x20;
/*TODO*///
/*TODO*///	copyscrollbitmap(bitmap,cps1_scroll2_bitmap,1024,scrollx,1,&scrly,&Machine->drv->visible_area,TRANSPARENCY_PEN,palette_transparent_pen);
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///  Scroll 3 (32x32 layer)
/*TODO*///
/*TODO*///  Attribute word layout:
/*TODO*///  0x0001	colour
/*TODO*///  0x0002	colour
/*TODO*///  0x0004	colour
/*TODO*///  0x0008	colour
/*TODO*///  0x0010	colour
/*TODO*///  0x0020	X Flip
/*TODO*///  0x0040	Y Flip
/*TODO*///  0x0080
/*TODO*///  0x0100
/*TODO*///  0x0200
/*TODO*///  0x0400
/*TODO*///  0x0800
/*TODO*///  0x1000
/*TODO*///  0x2000
/*TODO*///  0x4000
/*TODO*///  0x8000
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
/*TODO*///void cps1_palette_scroll3(unsigned short *base)
/*TODO*///{
/*TODO*///	int sx,sy;
/*TODO*///	int nx=(scroll3x>>5)+1;
/*TODO*///	int ny=(scroll3y>>5)-1;
/*TODO*///    int basecode=cps1_game_config->bank_scroll3*0x01000;
/*TODO*///
/*TODO*///	for (sx=0; sx<0x32/4+2; sx++)
/*TODO*///	{
/*TODO*///		for (sy=0; sy<0x20/4+2; sy++)
/*TODO*///		{
/*TODO*///			int offsy, offsx, offs, colour, code;
/*TODO*///			int n;
/*TODO*///			n=ny+sy;
/*TODO*///			offsy  = ((n&0x07)*4 | ((n&0xf8)*0x0100))&0x3fff;
/*TODO*///			offsx=((nx+sx)*0x020)&0x7ff;
/*TODO*///			offs=offsy+offsx;
/*TODO*///			offs &= 0x3fff;
/*TODO*///            code=basecode+READ_WORD(&cps1_scroll3[offs]);
/*TODO*///			if (cps1_game_config->kludge == 2 && code >= 0x01500)
/*TODO*///			{
/*TODO*///			       code -= 0x1000;
/*TODO*///			}
/*TODO*///			colour=READ_WORD(&cps1_scroll3[offs+2]);
/*TODO*///			if (code < cps1_max_tile32)
/*TODO*///			{
/*TODO*///				base[colour&0x1f] |= cps1_tile32_pen_usage[code];
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///void cps1_render_scroll3(struct osd_bitmap *bitmap, int priority)
/*TODO*///{
/*TODO*///	int sx,sy;
/*TODO*///	int nxoffset=scroll3x&0x1f;
/*TODO*///	int nyoffset=scroll3y&0x1f;
/*TODO*///	int nx=(scroll3x>>5)+1;
/*TODO*///	int ny=(scroll3y>>5)-1;
/*TODO*///    int basecode=cps1_game_config->bank_scroll3*0x01000;
/*TODO*///	const int startcode=cps1_game_config->start_scroll3;
/*TODO*///	const int endcode=cps1_game_config->end_scroll3;
/*TODO*///
/*TODO*///	for (sx=1; sx<0x32/4+2; sx++)
/*TODO*///	{
/*TODO*///		for (sy=1; sy<0x20/4+2; sy++)
/*TODO*///		{
/*TODO*///			int offsy, offsx, offs, colour, code;
/*TODO*///			int n;
/*TODO*///			n=ny+sy;
/*TODO*///			offsy  = ((n&0x07)*4 | ((n&0xf8)*0x0100))&0x3fff;
/*TODO*///			offsx=((nx+sx)*0x020)&0x7ff;
/*TODO*///			offs=offsy+offsx;
/*TODO*///			offs &= 0x3fff;
/*TODO*///            code=READ_WORD(&cps1_scroll3[offs]);
/*TODO*///			if (code >= startcode && code <= endcode)
/*TODO*///			{
/*TODO*///				int transp;
/*TODO*///
/*TODO*///				code+=basecode;
/*TODO*///				if (cps1_game_config->kludge == 2 && code >= 0x01500)
/*TODO*///				{
/*TODO*///					   code -= 0x1000;
/*TODO*///				}
/*TODO*///				colour=READ_WORD(&cps1_scroll3[offs+2]);
/*TODO*///				if (priority)
/*TODO*///					transp=cps1_transparency_scroll[(colour & 0x0180)>>7];
/*TODO*///				else transp = 0x7fff;
/*TODO*///
/*TODO*///				cps1_draw_tile32(bitmap,Machine->gfx[3],
/*TODO*///						code,
/*TODO*///						colour&0x1f,
/*TODO*///						colour&0x20,colour&0x40,
/*TODO*///						32*sx-nxoffset,32*sy-nyoffset,
/*TODO*///						transp);
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*///
/*TODO*///void cps1_render_layer(struct osd_bitmap *bitmap, int layer, int distort)
/*TODO*///{
/*TODO*///	if (cps1_layer_enabled[layer])
/*TODO*///	{
/*TODO*///		switch (layer)
/*TODO*///		{
/*TODO*///			case 0:
/*TODO*///				cps1_render_sprites(bitmap);
/*TODO*///				break;
/*TODO*///			case 1:
/*TODO*///                cps1_render_scroll1(bitmap, 0);
/*TODO*///				break;
/*TODO*///			case 2:
/*TODO*///				if (distort)
/*TODO*///                    cps1_render_scroll2_distort(bitmap);
/*TODO*///				else
/*TODO*///                    cps1_render_scroll2_low(bitmap);
/*TODO*///				break;
/*TODO*///			case 3:
/*TODO*///				cps1_render_scroll3(bitmap, 0);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///void cps1_render_high_layer(struct osd_bitmap *bitmap, int layer)
/*TODO*///{
/*TODO*///	if (cps1_layer_enabled[layer])
/*TODO*///	{
/*TODO*///		switch (layer)
/*TODO*///		{
/*TODO*///			case 0:
/*TODO*///				/* there are no high priority sprites */
/*TODO*///				break;
/*TODO*///			case 1:
/*TODO*///                cps1_render_scroll1(bitmap, 1);
/*TODO*///				break;
/*TODO*///			case 2:
/*TODO*///                cps1_render_scroll2_high(bitmap);
/*TODO*///				break;
/*TODO*///			case 3:
/*TODO*///				cps1_render_scroll3(bitmap, 1);
/*TODO*///				break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////***************************************************************************
/*TODO*///
/*TODO*///	Refresh screen
/*TODO*///
/*TODO*///***************************************************************************/
/*TODO*///
    public static VhUpdatePtr cps1_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {
            /*TODO*///	unsigned short palette_usage[cps1_palette_entries];
/*TODO*///	int layercontrol,l0,l1,l2,l3;
/*TODO*///	int i,offset;
/*TODO*///	int distort_scroll2=0;
/*TODO*///	int videocontrol=cps1_port(0x22);
/*TODO*///	int old_flip;
/*TODO*///
/*TODO*///
/*TODO*///	old_flip=cps1_flip_screen;
/*TODO*///	cps1_flip_screen=videocontrol&0x8000;
/*TODO*///	if (old_flip != cps1_flip_screen)
/*TODO*///	{
/*TODO*///		 /* Mark all of scroll 2 as dirty */
/*TODO*///		memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
/*TODO*///	}
/*TODO*///
/*TODO*///	layercontrol = READ_WORD(&cps1_output[cps1_game_config->layer_control]);
/*TODO*///
/*TODO*///	distort_scroll2 = videocontrol & 0x01;
/*TODO*///
/*TODO*///	/* Get video memory base registers */
/*TODO*///	cps1_get_video_base();
/*TODO*///
/*TODO*///	/* Find the offset of the last sprite in the sprite table */
/*TODO*///	cps1_find_last_sprite();
/*TODO*///
/*TODO*///	/* Build palette */
/*TODO*///	cps1_build_palette();
/*TODO*///
/*TODO*///	/* Compute the used portion of the palette */
/*TODO*///	memset (palette_usage, 0, sizeof (palette_usage));
/*TODO*///	cps1_palette_sprites (&palette_usage[cps1_obj_palette]);
/*TODO*///	if (cps1_layer_enabled[1])
/*TODO*///		cps1_palette_scroll1 (&palette_usage[cps1_scroll1_palette]);
/*TODO*///	if (cps1_layer_enabled[2])
/*TODO*///		cps1_palette_scroll2 (&palette_usage[cps1_scroll2_palette]);
/*TODO*///	else
/*TODO*///		memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
/*TODO*///	if (cps1_layer_enabled[3])
/*TODO*///		cps1_palette_scroll3 (&palette_usage[cps1_scroll3_palette]);
/*TODO*///
/*TODO*///	for (i = offset = 0; i < cps1_palette_entries; i++)
/*TODO*///	{
/*TODO*///		int usage = palette_usage[i];
/*TODO*///		if (usage)
/*TODO*///		{
/*TODO*///			int j;
/*TODO*///			for (j = 0; j < 15; j++)
/*TODO*///			{
/*TODO*///				if (usage & (1 << j))
/*TODO*///					palette_used_colors[offset++] = PALETTE_COLOR_USED;
/*TODO*///				else
/*TODO*///					palette_used_colors[offset++] = PALETTE_COLOR_UNUSED;
/*TODO*///			}
/*TODO*///			palette_used_colors[offset++] = PALETTE_COLOR_TRANSPARENT;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{
/*TODO*///			memset (&palette_used_colors[offset], PALETTE_COLOR_UNUSED, 16);
/*TODO*///			offset += 16;
/*TODO*///		}
/*TODO*///	}
/*TODO*///
/*TODO*///	if (palette_recalc ())
/*TODO*///	{
/*TODO*///		 /* Mark all of scroll 2 as dirty */
/*TODO*///		memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
/*TODO*///	}
/*TODO*///
/*TODO*///	/* Blank screen */
/*TODO*/////	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);
/*TODO*///// TODO: the draw functions don't clip correctly at the sides of the screen, so
/*TODO*///// for now let's clear the whole bitmap otherwise ctrl-f11 would show wrong counts
/*TODO*///	fillbitmap(bitmap,palette_transparent_pen,0);
/*TODO*///
/*TODO*///
/*TODO*///	/* Draw layers */
/*TODO*///	l0 = (layercontrol >> 0x06) & 03;
/*TODO*///	l1 = (layercontrol >> 0x08) & 03;
/*TODO*///	l2 = (layercontrol >> 0x0a) & 03;
/*TODO*///	l3 = (layercontrol >> 0x0c) & 03;
/*TODO*///
/*TODO*///	cps1_render_layer(bitmap,l0,distort_scroll2);
/*TODO*///	cps1_render_layer(bitmap,l1,distort_scroll2);
/*TODO*///	if (l1 == 0) cps1_render_high_layer(bitmap,l0);	/* overlay sprites */
/*TODO*///	cps1_render_layer(bitmap,l2,distort_scroll2);
/*TODO*///	if (l2 == 0) cps1_render_high_layer(bitmap,l1);	/* overlay sprites */
/*TODO*///	cps1_render_layer(bitmap,l3,distort_scroll2);
/*TODO*///	if (l3 == 0) cps1_render_high_layer(bitmap,l2);	/* overlay sprites */
/*TODO*///
/*TODO*///#if CPS1_DUMP_VIDEO
/*TODO*///    if (keyboard_pressed(KEYCODE_F))
/*TODO*///    {
/*TODO*///        cps1_dump_video();
/*TODO*///    }
/*TODO*///#endif
        }
    };
    public static VhEofCallbackPtr cps1_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            /*TODO*///	/* Get video memory base registers */
/*TODO*///	cps1_get_video_base();
/*TODO*///
/*TODO*///	/* Mish: 181099: Buffer sprites for next frame - the hardware must do
/*TODO*///		this at the end of vblank */
/*TODO*///	memcpy(cps1_buffered_obj,cps1_obj,cps1_obj_size);
        }
    };
}
