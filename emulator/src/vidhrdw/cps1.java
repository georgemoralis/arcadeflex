package vidhrdw;

import static arcadeflex.ptrlib.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.common.*;
import static mame.commonH.*;
import static arcadeflex.libc.*;
import static arcadeflex.libc_old.*;
import static mame.mame.*;
import static drivers.WIP.cps1.*;
import static mame.memoryH.*;
import static arcadeflex.video.*;
import static mame.palette.*;
import static mame.paletteH.*;
import static mame.drawgfx.*;
import static mame.drawgfxH.*;
import static vidhrdw.cps1draw.*;

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

    static void cps1_init_machine() {
        String gamename = Machine.gamedrv.name;
        UBytePtr RAM = memory_region(REGION_CPU1);

        int ptr = 0;
        while (cps1_config_table[ptr] != null) {
            if (strcmp(cps1_config_table[ptr].name, gamename) == 0) {
                break;
            }
            ptr++;
        }
        cps1_game_config = cps1_config_table[ptr];
        //System.out.println("game name " + gamename);
        //System.out.println("loaded " + cps1_game_config.name);
        if (strcmp(gamename, "sf2rb") == 0) {
            /* Patch out protection check */
            RAM.WRITE_WORD(0xe5464, 0x6012);

        }
    }
    public static ReadHandlerPtr cps1_port = new ReadHandlerPtr() {
        public int handler(int offset) {
            return cps1_output.READ_WORD(offset);
        }
    };

    public static UBytePtr cps1_base(int offset, int boundary) {
        int base = cps1_port.handler(offset) * 256;
        /*
         The scroll RAM must start on a 0x4000 boundary.
         Some games do not do this.
         For example:
         Captain commando     - continue screen will not display
         Muscle bomber games  - will animate garbage during gameplay
         Mask out the irrelevant bits.
         */
        base &= ~(boundary - 1);
        return new UBytePtr(cps1_gfxram, base & 0x3ffff);//&cps1_gfxram[base&0x3ffff];
    }

    public static ReadHandlerPtr cps1_output_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* Some games interrogate a couple of registers on bootup. */
            /* These are CPS1 board B self test checks. They wander from game to */
            /* game. */
            if (offset != 0 && offset == cps1_game_config.cpsb_addr) {
                return cps1_game_config.cpsb_value;
            }

            /* some games use as a protection check the ability to do 16-bit multiplies */
            /* with a 32-bit result, by writing the factors to two ports and reading the */
            /* result from two other ports. */
            if (offset != 0 && offset == cps1_game_config.mult_result_lo) {
                return (cps1_output.READ_WORD(cps1_game_config.mult_factor1)
                        * cps1_output.READ_WORD(cps1_game_config.mult_factor2)) & 0xffff;
            }
            if (offset != 0 && offset == cps1_game_config.mult_result_hi) {
                return (cps1_output.READ_WORD(cps1_game_config.mult_factor1)
                        * cps1_output.READ_WORD(cps1_game_config.mult_factor2)) >> 16;
            }

            /* Pang 3 EEPROM interface */
            if (cps1_game_config.kludge == 5 && offset == 0x7a) {
                return cps1_eeprom_port_r.handler(0);
            }

            return cps1_output.READ_WORD(offset);
        }
    };
    public static WriteHandlerPtr cps1_output_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            /* Pang 3 EEPROM interface */
            if (cps1_game_config.kludge == 5 && offset == 0x7a) {
                cps1_eeprom_port_w.handler(0, data);
                return;
            }

            COMBINE_WORD_MEM(cps1_output, offset, data);
        }
    };

    /* Public variables */
    public static UBytePtr cps1_gfxram = new UBytePtr();
    public static UBytePtr cps1_output = new UBytePtr();

    public static int[] cps1_gfxram_size = new int[1];
    public static int[] cps1_output_size = new int[1];

    /* Private */

    /* Offset of each palette entry */
    public static final int cps1_obj_palette = 0;
    public static final int cps1_scroll1_palette = 32;
    public static final int cps1_scroll2_palette = 32 + 32;
    public static final int cps1_scroll3_palette = 32 + 32 + 32;
    public static final int cps1_palette_entries = (32 * 4);  /* Number colour schemes in palette */

    public static final int cps1_scroll1_size = 0x4000;
    public static final int cps1_scroll2_size = 0x4000;
    public static final int cps1_scroll3_size = 0x4000;
    public static final int cps1_obj_size = 0x0800;
    public static final int cps1_other_size = 0x0800;
    public static final int cps1_palette_size = cps1_palette_entries * 32; /* Size of palette RAM */

    public static int cps1_flip_screen;    /* Flip screen on / off */

    static UBytePtr cps1_scroll1;
    static UBytePtr cps1_scroll2;
    static UBytePtr cps1_scroll3;
    static UBytePtr cps1_obj;
    static UBytePtr cps1_buffered_obj;
    static UBytePtr cps1_palette;
    static UBytePtr cps1_other;
    static UBytePtr cps1_old_palette;
    /* Working variables */
    static int cps1_last_sprite_offset;     /* Offset of the last sprite */

    static int[] cps1_layer_enabled = new int[4];       /* Layer enabled [Y/N] */

    static int scroll1x, scroll1y, scroll2x, scroll2y, scroll3x, scroll3y;
    static char[] cps1_scroll2_old;
    static osd_bitmap cps1_scroll2_bitmap;


    /* Output ports */
    public static final int CPS1_OBJ_BASE = 0x00;    /* Base address of objects */

    public static final int CPS1_SCROLL1_BASE = 0x02;    /* Base address of scroll 1 */

    public static final int CPS1_SCROLL2_BASE = 0x04;   /* Base address of scroll 2 */

    public static final int CPS1_SCROLL3_BASE = 0x06;   /* Base address of scroll 3 */

    public static final int CPS1_OTHER_BASE = 0x08;    /* Base address of other video */

    public static final int CPS1_PALETTE_BASE = 0x0a;    /* Base address of palette */

    public static final int CPS1_SCROLL1_SCROLLX = 0x0c;    /* Scroll 1 X */

    public static final int CPS1_SCROLL1_SCROLLY = 0x0e;    /* Scroll 1 Y */

    public static final int CPS1_SCROLL2_SCROLLX = 0x10;   /* Scroll 2 X */

    public static final int CPS1_SCROLL2_SCROLLY = 0x12;    /* Scroll 2 Y */

    public static final int CPS1_SCROLL3_SCROLLX = 0x14;   /* Scroll 3 X */

    public static final int CPS1_SCROLL3_SCROLLY = 0x16;    /* Scroll 3 Y */

    public static final int CPS1_ROWSCROLL_OFFS = 0x20;    /* base of row scroll offsets in other RAM */

    public static final int CPS1_SCROLL2_WIDTH = 0x40;
    public static final int CPS1_SCROLL2_HEIGHT = 0x40;


    /*
     CPS1 VIDEO RENDERER

     */
    static /*UINT32*/ int[] cps1_gfx;		 /* Converted GFX memory */

    static int[] cps1_char_pen_usage;	/* pen usage array */

    static int[] cps1_tile16_pen_usage;      /* pen usage array */

    static int[] cps1_tile32_pen_usage;      /* pen usage array */

    static int cps1_max_char;	       /* Maximum number of 8x8 chars */

    static int cps1_max_tile16;	     /* Maximum number of 16x16 tiles */

    static int cps1_max_tile32;	     /* Maximum number of 32x32 tiles */


    static int cps1_gfx_start() {
        int/*UINT32*/ dwval;
        int size = memory_region_length(REGION_GFX1);
        UBytePtr data = memory_region(REGION_GFX1);
        int i, j, nchar, penusage, gfxsize;

        gfxsize = size / 4;

        /* Set up maximum values */
        cps1_max_char = (gfxsize / 2) / 8;
        cps1_max_tile16 = (gfxsize / 4) / 8;
        cps1_max_tile32 = (gfxsize / 16) / 8;

        cps1_gfx = new int[gfxsize * 4];//cps1_gfx=malloc(gfxsize*sizeof(UINT32));
        if (cps1_gfx == null) {
            return -1;
        }

        cps1_char_pen_usage = new int[cps1_max_char * 4];//cps1_char_pen_usage=malloc(cps1_max_char*sizeof(int));
        if (cps1_char_pen_usage == null) {
            return -1;
        }
        memset(cps1_char_pen_usage, 0, cps1_max_char * 4);

        cps1_tile16_pen_usage = new int[cps1_max_tile16 * 4];
        if (cps1_tile16_pen_usage == null) {
            return -1;
        }
        memset(cps1_tile16_pen_usage, 0, cps1_max_tile16 * 4);

        cps1_tile32_pen_usage = new int[cps1_max_tile32 * 4];
        if (cps1_tile32_pen_usage == null) {
            return -1;
        }
        memset(cps1_tile32_pen_usage, 0, cps1_max_tile32 * 4);

        {
            for (i = 0; i < gfxsize / 2; i++) {
                nchar = i / 8;  /* 8x8 char number */

                dwval = 0;
                for (j = 0; j < 8; j++) {
                    int n, mask;
                    n = 0;
                    mask = 0x80 >> j;
                    if ((data.read(size / 4) & mask) != 0) {
                        n |= 1;
                    }
                    if ((data.read(size / 4 + 1) & mask) != 0) {
                        n |= 2;
                    }
                    if ((data.read(size / 2 + size / 4) & mask) != 0) {
                        n |= 4;
                    }
                    if ((data.read(size / 2 + size / 4 + 1) & mask) != 0) {
                        n |= 8;
                    }
                    dwval |= n << (28 - j * 4);
                    penusage = 1 << n;
                    cps1_char_pen_usage[nchar] |= penusage;
                    cps1_tile16_pen_usage[nchar / 2] |= penusage;
                    cps1_tile32_pen_usage[nchar / 8] |= penusage;
                }
                cps1_gfx[2 * i] = dwval;
                dwval = 0;
                for (j = 0; j < 8; j++) {
                    int n, mask;
                    n = 0;
                    mask = 0x80 >> j;
                    if ((data.read() & mask) != 0) {
                        n |= 1;
                    }
                    if ((data.read(1) & mask) != 0) {
                        n |= 2;
                    }
                    if ((data.read(size / 2) & mask) != 0) {
                        n |= 4;
                    }
                    if ((data.read(size / 2 + 1) & mask) != 0) {
                        n |= 8;
                    }
                    dwval |= n << (28 - j * 4);
                    penusage = 1 << n;
                    cps1_char_pen_usage[nchar] |= penusage;
                    cps1_tile16_pen_usage[nchar / 2] |= penusage;
                    cps1_tile32_pen_usage[nchar / 8] |= penusage;
                }
                cps1_gfx[2 * i + 1] = dwval;
                data.offset += 2;
            }
        }
        return 0;
    }

    public static void cps1_gfx_stop() {
        if (cps1_gfx != null) {
            cps1_gfx = null;
        }
        if (cps1_char_pen_usage != null) {
            cps1_char_pen_usage = null;;
        }
        if (cps1_tile16_pen_usage != null) {
            cps1_tile16_pen_usage = null;
        }
        if (cps1_tile32_pen_usage != null) {
            cps1_tile32_pen_usage = null;
        }
    }

    
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
    public static void cps1_draw_scroll1(osd_bitmap dest, int code, int color, int flipx, int flipy, int sx, int sy, int tpens) {
        if (Machine.scrbitmap.depth == 16) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///        cps1_draw_gfx16(dest,
/*TODO*///            Machine->gfx[0],
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_char_pen_usage,8, cps1_max_char, 16, 1);
        } else {
            cps1_draw_gfx(dest,
                    Machine.gfx[0],
                    code, color, flipx, flipy, sx, sy,
                    tpens, cps1_char_pen_usage, 8, cps1_max_char, 16, 1);
        }
    }

    public static void cps1_draw_tile16(osd_bitmap dest, GfxElement gfx, int code, int color, int flipx, int flipy, int sx, int sy, int tpens) {
        if (Machine.scrbitmap.depth == 16) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///        cps1_draw_gfx16(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_tile16_pen_usage,16, cps1_max_tile16, 16*2,0);
        } else {
            cps1_draw_gfx(dest, gfx, code, color, flipx, flipy, sx, sy, tpens, cps1_tile16_pen_usage, 16, cps1_max_tile16, 16 * 2, 0);
        }
    }

    public static void cps1_draw_tile32(osd_bitmap dest, GfxElement gfx, int code, int color, int flipx, int flipy, int sx, int sy, int tpens) {
        if (Machine.scrbitmap.depth == 16) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///        cps1_draw_gfx16(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            tpens,cps1_tile32_pen_usage,32, cps1_max_tile32, 16*2*4,0);
        } else {
            cps1_draw_gfx(dest,
                    gfx,
                    code, color, flipx, flipy, sx, sy,
                    tpens, cps1_tile32_pen_usage, 32, cps1_max_tile32, 16 * 2 * 4, 0);
        }
    }

    public static void cps1_draw_blank16(osd_bitmap dest, int sx, int sy) {
        int i, j;

        if ((Machine.orientation & ORIENTATION_SWAP_XY) != 0) {
            int temp;
            temp = sx;
            sx = sy;
            sy = dest.height - temp - 16;
        }

        if (cps1_flip_screen != 0) {
            /* Handle flipped screen */
            sx = dest.width - sx - 16;
            sy = dest.height - sy - 16;
        }

        if (Machine.scrbitmap.depth == 16) {
            throw new UnsupportedOperationException("Unimplemented");
            /*TODO*///        for (i=15; i>=0; i--)
/*TODO*///		{
/*TODO*///			register unsigned short *bm=(unsigned short *)dest->line[sy+i]+sx;
/*TODO*///			for (j=15; j>=0; j--)
/*TODO*///			{
/*TODO*///				*bm=palette_transparent_pen;
/*TODO*///				bm++;
/*TODO*///			}
/*TODO*///		}
        } else {
            for (i = 15; i >= 0; i--) {
                UBytePtr bm = new UBytePtr(dest.line[sy + i], sx);
                for (j = 15; j >= 0; j--) {
                    bm.write(palette_transparent_pen);
                    bm.inc();
                }
            }
        }
    }

    public static void cps1_draw_tile16_bmp(osd_bitmap dest, GfxElement gfx, int code, int color, int flipx, int flipy, int sx, int sy) {
        if (Machine.scrbitmap.depth == 16) {
            /*TODO*///        cps1_draw_gfx_opaque16(dest,
/*TODO*///            gfx,
/*TODO*///            code,color,flipx,flipy,sx,sy,
/*TODO*///            -1,cps1_tile16_pen_usage,16, cps1_max_tile16, 16*2,0);
            throw new UnsupportedOperationException("Unimplemented");
        } else {
            cps1_draw_gfx_opaque(dest, gfx, code, color, flipx, flipy, sx, sy, -1, cps1_tile16_pen_usage, 16, cps1_max_tile16, 16 * 2, 0);
        }
    }

    static int[] cps1_transparency_scroll = new int[4];

    public static void cps1_get_video_base() {
        int layercontrol;

        /* Re-calculate the VIDEO RAM base */
        cps1_scroll1 = cps1_base(CPS1_SCROLL1_BASE, cps1_scroll1_size);
        cps1_scroll2 = cps1_base(CPS1_SCROLL2_BASE, cps1_scroll2_size);
        cps1_scroll3 = cps1_base(CPS1_SCROLL3_BASE, cps1_scroll3_size);
        cps1_obj = cps1_base(CPS1_OBJ_BASE, cps1_obj_size);
        cps1_palette = cps1_base(CPS1_PALETTE_BASE, cps1_palette_size);
        cps1_other = cps1_base(CPS1_OTHER_BASE, cps1_other_size);

        /* Get scroll values */
        scroll1x = cps1_port.handler(CPS1_SCROLL1_SCROLLX);
        scroll1y = cps1_port.handler(CPS1_SCROLL1_SCROLLY);
        scroll2x = cps1_port.handler(CPS1_SCROLL2_SCROLLX);
        scroll2y = cps1_port.handler(CPS1_SCROLL2_SCROLLY);
        scroll3x = cps1_port.handler(CPS1_SCROLL3_SCROLLX);
        scroll3y = cps1_port.handler(CPS1_SCROLL3_SCROLLY);

        /* Get transparency registers */
        if (cps1_game_config.priority1 != 0) {
            cps1_transparency_scroll[0] = cps1_port.handler(cps1_game_config.priority0);
            cps1_transparency_scroll[1] = cps1_port.handler(cps1_game_config.priority1);
            cps1_transparency_scroll[2] = cps1_port.handler(cps1_game_config.priority2);
            cps1_transparency_scroll[3] = cps1_port.handler(cps1_game_config.priority3);
        }

        /* Get layer enable bits */
        layercontrol = cps1_port.handler(cps1_game_config.layer_control);
        cps1_layer_enabled[0] = 1;
        cps1_layer_enabled[1] = layercontrol & cps1_game_config.scrl1_enable_mask;
        cps1_layer_enabled[2] = layercontrol & cps1_game_config.scrl2_enable_mask;
        cps1_layer_enabled[3] = layercontrol & cps1_game_config.scrl3_enable_mask;

    }

    /**
     * *************************************************************************
     *
     * Start the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStartPtr cps1_vh_start = new VhStartPtr() {
        public int handler() {
            int i;

            cps1_init_machine();

            if (cps1_gfx_start() != 0) {
                return -1;
            }

            cps1_scroll2_bitmap = osd_new_bitmap(CPS1_SCROLL2_WIDTH * 16,
                    CPS1_SCROLL2_HEIGHT * 16, Machine.scrbitmap.depth);
            if (cps1_scroll2_bitmap == null) {
                return -1;
            }
            cps1_scroll2_old = new char[cps1_scroll2_size];
            if (cps1_scroll2_old == null) {
                return -1;
            }
            memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);

            cps1_old_palette = new UBytePtr(cps1_palette_size);//(unsigned char *)malloc(cps1_palette_size);
            if (cps1_old_palette == null) {
                return -1;
            }
            memset(cps1_old_palette, 0x00, cps1_palette_size);
            for (i = 0; i < cps1_palette_entries * 16; i++) {
                palette_change_color(i, 0, 0, 0);
            }

            cps1_buffered_obj = new UBytePtr(cps1_obj_size);
            if (cps1_buffered_obj == null) {
                return -1;
            }
            memset(cps1_buffered_obj, 0x00, cps1_obj_size);

            memset(cps1_gfxram, 0, cps1_gfxram_size[0]);   /* Clear GFX RAM */

            memset(cps1_output, 0, cps1_output_size[0]);   /* Clear output ports */

            /* Put in some defaults */
            cps1_output.WRITE_WORD(0x00, 0x9200);
            cps1_output.WRITE_WORD(0x02, 0x9000);
            cps1_output.WRITE_WORD(0x04, 0x9040);
            cps1_output.WRITE_WORD(0x06, 0x9080);
            cps1_output.WRITE_WORD(0x08, 0x9100);
            cps1_output.WRITE_WORD(0x0a, 0x90c0);

            if (cps1_game_config == null) {
                if (errorlog != null) {
                    fprintf(errorlog, "cps1_game_config hasn't been set up yet");
                }
                return -1;
            }


            /* Set up old base */
            cps1_get_video_base();   /* Calculate base pointers */

            cps1_get_video_base();   /* Calculate old base pointers */


            for (i = 0; i < 4; i++) {
                cps1_transparency_scroll[i] = 0x0000;
            }
            return 0;
        }
    };

    /**
     * *************************************************************************
     *
     * Stop the video hardware emulation.
     *
     **************************************************************************
     */
    public static VhStopPtr cps1_vh_stop = new VhStopPtr() {
        public void handler() {
            if (cps1_old_palette != null) {
                cps1_old_palette = null;
            }
            if (cps1_scroll2_bitmap != null) {
                osd_free_bitmap(cps1_scroll2_bitmap);
            }
            if (cps1_scroll2_old != null) {
                cps1_scroll2_old = null;
            }
            if (cps1_buffered_obj != null) {
                cps1_buffered_obj = null;
            }
            cps1_gfx_stop();
        }
    };

    /**
     * *************************************************************************
     *
     * Build palette from palette RAM
     *
     * 12 bit RGB with a 4 bit brightness value.
     *
     **************************************************************************
     */
    public static void cps1_build_palette() {
        int offset;

        for (offset = 0; offset < cps1_palette_entries * 16; offset++) {
            int palette = cps1_palette.READ_WORD(offset * 2);

            if (palette != cps1_old_palette.READ_WORD(offset * 2)) {
                int red, green, blue, bright;

                bright = (palette >> 12);
                if (bright != 0) {
                    bright += 2;
                }

                red = ((palette >> 8) & 0x0f) * bright;
                green = ((palette >> 4) & 0x0f) * bright;
                blue = (palette & 0x0f) * bright;

                palette_change_color(offset, red, green, blue);
                cps1_old_palette.WRITE_WORD(offset * 2, palette);
            }
        }
    }

    /***************************************************************************

  Scroll 1 (8x8)

  Attribute word layout:
  0x0001	colour
  0x0002	colour
  0x0004	colour
  0x0008	colour
  0x0010	colour
  0x0020	X Flip
  0x0040	Y Flip
  0x0080
  0x0100
  0x0200
  0x0400
  0x0800
  0x1000
  0x2000
  0x4000
  0x8000


***************************************************************************/

    public static void cps1_palette_scroll1(char[] base, int offset) {
        int x, y, offs, offsx;

        int scrlxrough = (scroll1x >> 3) + 8;
        int scrlyrough = (scroll1y >> 3);
        int basecode = cps1_game_config.bank_scroll1 * 0x08000;

        for (x = 0; x < 0x36; x++) {
            offsx = (scrlxrough + x) * 0x80;
            offsx &= 0x1fff;

            for (y = 0; y < 0x20; y++) {
                int code, colour, offsy;
                int n = scrlyrough + y;
                offsy = ((n & 0x1f) * 4 | ((n & 0x20) * 0x100)) & 0x3fff;
                offs = offsy + offsx;
                offs &= 0x3fff;
                code = basecode + cps1_scroll1.READ_WORD(offs);
                colour = cps1_scroll1.READ_WORD(offs + 2);
                if (code < cps1_max_char) {
                    base[offset + (colour & 0x1f)]
                            |= cps1_char_pen_usage[code] & 0x7fff;
                }
            }
        }
    }

    public static void cps1_render_scroll1(osd_bitmap bitmap, int priority) {
        int x, y, offs, offsx, sx, sy, ytop;

        int scrlxrough = (scroll1x >> 3) + 4;
        int scrlyrough = (scroll1y >> 3);
        int base = cps1_game_config.bank_scroll1 * 0x08000;
        int spacechar = cps1_game_config.space_scroll1;

        sx = -(scroll1x & 0x07);
        ytop = -(scroll1y & 0x07) + 32;

        for (x = 0; x < 0x35; x++) {
            sy = ytop;
            offsx = (scrlxrough + x) * 0x80;
            offsx &= 0x1fff;

            for (y = 0; y < 0x20; y++) {
                int code, offsy, colour;
                int n = scrlyrough + y;
                offsy = ((n & 0x1f) * 4 | ((n & 0x20) * 0x100)) & 0x3fff;
                offs = offsy + offsx;
                offs &= 0x3fff;

                code = cps1_scroll1.READ_WORD(offs);
                colour = cps1_scroll1.READ_WORD(offs + 2);

                if (code != 0x20 && code != spacechar) {
                    int transp;

                    /* 0x0020 appears to never be drawn */
                    if (priority != 0) {
                        transp = cps1_transparency_scroll[(colour & 0x0180) >> 7];
                    } else {
                        transp = 0x7fff;
                    }

                    cps1_draw_scroll1(bitmap,
                            code + base,
                            colour & 0x1f,
                            colour & 0x20,
                            colour & 0x40,
                            sx, sy, transp);
                }
                sy += 8;
            }
            sx += 8;
        }
    }

    

/***************************************************************************

								Sprites
								=======

  Sprites are represented by a number of 8 byte values

  xx xx yy yy nn nn aa aa

  where xxxx = x position
		yyyy = y position
		nnnn = tile number
		aaaa = attribute word
					0x0001	colour
					0x0002	colour
					0x0004	colour
					0x0008	colour
					0x0010	colour
					0x0020	X Flip
					0x0040	Y Flip
					0x0080	unknown
					0x0100	X block size (in sprites)
					0x0200	X block size
					0x0400	X block size
					0x0800	X block size
					0x1000	Y block size (in sprites)
					0x2000	Y block size
					0x4000	Y block size
					0x8000	Y block size

  The end of the table (may) be marked by an attribute value of 0xff00.

***************************************************************************/
    public static void cps1_find_last_sprite() /* Find the offset of last sprite */ {
        int offset = 6;
        /* Locate the end of table marker */
        while (offset < cps1_obj_size) {
            int colour = cps1_buffered_obj.READ_WORD(offset);
            if (colour == 0xff00) {
                /* Marker found. This is the last sprite. */
                cps1_last_sprite_offset = offset - 6 - 8;
                return;
            }
            offset += 8;
        }
        /* Sprites must use full sprite RAM */
        cps1_last_sprite_offset = cps1_obj_size - 8;
    }

    /* Find used colours */
    static void cps1_palette_sprites(char[] base, int offset) {
        int i;

        for (i = cps1_last_sprite_offset; i >= 0; i -= 8) {
            int x = cps1_buffered_obj.READ_WORD(i);
            int y = cps1_buffered_obj.READ_WORD(i + 2);
            if (x != 0 && y != 0) {
                int colour = cps1_buffered_obj.READ_WORD(i + 6);
                int col = colour & 0x1f;
                /*unsigned*/ int code = Math.abs(cps1_buffered_obj.READ_WORD(i + 4));//get a non negative value
                if (cps1_game_config.kludge == 7) {
                    code += 0x4000;
                }
                if (cps1_game_config.kludge == 1 && code >= 0x01000) {
                    code += 0x4000;
                }
                if (cps1_game_config.kludge == 2 && code >= 0x02a00) {
                    code += 0x4000;
                }

                if ((colour & 0xff00) != 0) {
                    int nys, nxs;
                    int nx = (colour & 0x0f00) >> 8;
                    int ny = (colour & 0xf000) >> 12;
                    nx++;
                    ny++;

                    if ((colour & 0x40) != 0) /* Y Flip */ /* Y flip */ {
                        if ((colour & 0x20) != 0) {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    int cod = code + (nx - 1) - nxs + 0x10 * (ny - 1 - nys);
                                    base[col + offset]
                                            |= cps1_tile16_pen_usage[cod % cps1_max_tile16];
                                }
                            }
                        } else {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    int cod = code + nxs + 0x10 * (ny - 1 - nys);
                                    base[col + offset]
                                            |= cps1_tile16_pen_usage[cod % cps1_max_tile16];
                                }
                            }
                        }
                    } else {
                        if ((colour & 0x20) != 0) {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    int cod = code + (nx - 1) - nxs + 0x10 * nys;
                                    base[col + offset]
                                            |= cps1_tile16_pen_usage[cod % cps1_max_tile16];
                                }
                            }
                        } else {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    int cod = code + nxs + 0x10 * nys;
                                    base[col + offset]
                                            |= cps1_tile16_pen_usage[cod % cps1_max_tile16];
                                }
                            }
                        }
                    }
                    base[col + offset] &= 0x7fff;
                } else {
                    base[col + offset]
                            |= cps1_tile16_pen_usage[code % cps1_max_tile16] & 0x7fff;
                }
            }
        }
    }

    public static void cps1_render_sprites(osd_bitmap bitmap) {
        int mask = 0x7fff;
        int i;
//mish
	/* Draw the sprites */
        for (i = cps1_last_sprite_offset; i >= 0; i -= 8) {
            int x = cps1_buffered_obj.READ_WORD(i);
            int y = cps1_buffered_obj.READ_WORD(i + 2);
            if (x != 0 && y != 0) {
                /*unsigned*/ int code = Math.abs(cps1_buffered_obj.READ_WORD(i + 4));//make sure it's not negative (shadow)
                int colour = cps1_buffered_obj.READ_WORD(i + 6);
                int col = colour & 0x1f;

                y &= 0x1ff;
                if (y > 450) {
                    y -= 0x200;
                }

                /* in cawing, skyscrapers parts on level 2 have all the top bits of the */
                /* x coordinate set. Does this have a special meaning? */
                x &= 0x1ff;
                if (x > 450) {
                    x -= 0x200;
                }

                x -= 0x20;
                y += 0x20;

                if (cps1_game_config.kludge == 7) {
                    code += 0x4000;
                }
                if (cps1_game_config.kludge == 1 && code >= 0x01000) {
                    code += 0x4000;
                }
                if (cps1_game_config.kludge == 2 && code >= 0x02a00) {
                    code += 0x4000;
                }

                if ((colour & 0xff00) != 0) {
                    /* handle blocked sprites */
                    int nx = (colour & 0x0f00) >> 8;
                    int ny = (colour & 0xf000) >> 12;
                    int nxs, nys, sx, sy;
                    nx++;
                    ny++;

                    if ((colour & 0x40) != 0) {
                        /* Y flip */
                        if ((colour & 0x20) != 0) {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    sx = x + nxs * 16;
                                    sy = y + nys * 16;
                                    if (sx > 450) {
                                        sx -= 0x200;
                                    }
                                    if (sy > 450) {
                                        sy -= 0x200;
                                    }

                                    cps1_draw_tile16(bitmap, Machine.gfx[1],
                                            code + (nx - 1) - nxs + 0x10 * (ny - 1 - nys),
                                            col & 0x1f,
                                            1, 1,
                                            sx, sy, mask);
                                }
                            }
                        } else {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    sx = x + nxs * 16;
                                    sy = y + nys * 16;
                                    if (sx > 450) {
                                        sx -= 0x200;
                                    }
                                    if (sy > 450) {
                                        sy -= 0x200;
                                    }

                                    cps1_draw_tile16(bitmap, Machine.gfx[1],
                                            code + nxs + 0x10 * (ny - 1 - nys),
                                            col & 0x1f,
                                            0, 1,
                                            sx, sy, mask);
                                }
                            }
                        }
                    } else {
                        if ((colour & 0x20) != 0) {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    sx = x + nxs * 16;
                                    sy = y + nys * 16;
                                    if (sx > 450) {
                                        sx -= 0x200;
                                    }
                                    if (sy > 450) {
                                        sy -= 0x200;
                                    }

                                    cps1_draw_tile16(bitmap, Machine.gfx[1],
                                            code + (nx - 1) - nxs + 0x10 * nys,
                                            col & 0x1f,
                                            1, 0,
                                            sx, sy, mask
                                    );
                                }
                            }
                        } else {
                            for (nys = 0; nys < ny; nys++) {
                                for (nxs = 0; nxs < nx; nxs++) {
                                    sx = x + nxs * 16;
                                    sy = y + nys * 16;
                                    if (sx > 450) {
                                        sx -= 0x200;
                                    }
                                    if (sy > 450) {
                                        sy -= 0x200;
                                    }

                                    cps1_draw_tile16(bitmap, Machine.gfx[1],
                                            code + nxs + 0x10 * nys,
                                            col & 0x1f,
                                            0, 0,
                                            sx, sy, mask);
                                }
                            }
                        }
                    }
                } else {
                    /* Simple case... 1 sprite */
                    cps1_draw_tile16(bitmap, Machine.gfx[1],
                            code,
                            col & 0x1f,
                            colour & 0x20, colour & 0x40,
                            x, y, mask);
                }
            }
        }
    }
    


/***************************************************************************

  Scroll 2 (16x16 layer)

  Attribute word layout:
  0x0001	colour
  0x0002	colour
  0x0004	colour
  0x0008	colour
  0x0010	colour
  0x0020	X Flip
  0x0040	Y Flip
  0x0080	??? Priority
  0x0100	??? Priority
  0x0200
  0x0400
  0x0800
  0x1000
  0x2000
  0x4000
  0x8000


***************************************************************************/


    public static void cps1_palette_scroll2(char[] base, int offset) {
        int offs, code, colour;
        int basecode = cps1_game_config.bank_scroll2 * 0x04000;

        for (offs = cps1_scroll2_size - 4; offs >= 0; offs -= 4) {
            code = basecode + cps1_scroll2.READ_WORD(offs);
            colour = cps1_scroll2.READ_WORD(offs + 2) & 0x1f;
            if (code < cps1_max_tile16) {
                base[colour + offset] |= cps1_tile16_pen_usage[code];
            }
        }
    }

    public static int readint(char[] memory, int base, int offset) {
        int myNumber = (((int) memory[base + offset]) << 0)
                | (((int) memory[base + offset + 1]) << 8)
                | (((int) memory[base + offset + 2]) << 16)
                | (((int) memory[base + offset + 3]) << 24);
        return myNumber;
    }

    public static void writeint(char[] memory, int offset, long value) {
        memory[offset + 3] = (char) (value >> 24 & 0xFF);
        memory[offset + 2] = (char) (value >> 16 & 0xFF);
        memory[offset + 1] = (char) (value >> 8 & 0xFF);
        memory[offset] = (char) (value & 0xFF);

    }

    public static void cps1_render_scroll2_bitmap(osd_bitmap bitmap) {//not sure...
        int sx, sy;
        int ny = (scroll2y >> 4);	  /* Rough Y */

        int base = cps1_game_config.bank_scroll2 * 0x04000;
        int startcode = cps1_game_config.start_scroll2;
        int endcode = cps1_game_config.end_scroll2;
        int kludge = cps1_game_config.kludge;

        for (sx = CPS1_SCROLL2_WIDTH - 1; sx >= 0; sx--) {
            int n = ny;
            for (sy = 0x09 * 2 - 1; sy >= 0; sy--) {
                long newvalue;
                int offsy, offsx, offs, colour, code;

                n &= 0x3f;
                offsy = ((n & 0x0f) * 4 | ((n & 0x30) * 0x100)) & 0x3fff;
                offsx = (sx * 0x040) & 0xfff;
                offs = offsy + offsx;

                colour = cps1_scroll2.READ_WORD(offs + 2);

                //newvalue=*(long*)(&cps1_scroll2[offs]);
                newvalue = readint(cps1_scroll2.memory, cps1_scroll2.offset, offs);
                //if ( newvalue != *(long*)(&cps1_scroll2_old[offs]) )
                if (newvalue != readint(cps1_scroll2_old, 0, offs)) {
                    //* (long *)(&cps1_scroll2_old[offs])=newvalue;
                    writeint(cps1_scroll2_old, offs, newvalue);
                    code = cps1_scroll2.READ_WORD(offs);
                    if (code >= startcode && code <= endcode
                            /*
                             MERCS has an gap in the scroll 2 layout
                             (bad tiles at start of level 2)*/
                            && !(kludge == 4 && (code >= 0x1e00 && code < 0x5400))) {
                        code += base;
                        cps1_draw_tile16_bmp(bitmap,
                                Machine.gfx[2],
                                code,
                                colour & 0x1f,
                                colour & 0x20, colour & 0x40,
                                16 * sx, 16 * n);
                    } else {
                        cps1_draw_blank16(bitmap, 16 * sx, 16 * n);
                    }
                    //cps1_print_debug_tile_info(bitmap, 16*sx, 16*n, colour,1);
                }
                n++;
            }
        }
    }

    public static void cps1_render_scroll2_high(osd_bitmap bitmap) {

        int sx, sy;
        int nxoffset = (scroll2x & 0x0f) + 32;    /* Smooth X */

        int nyoffset = (scroll2y & 0x0f);    /* Smooth Y */

        int nx = (scroll2x >> 4);	  /* Rough X */

        int ny = (scroll2y >> 4) - 4;	/* Rough Y */

        int base = cps1_game_config.bank_scroll2 * 0x04000;

        for (sx = 0; sx < 0x32 / 2 + 4; sx++) {
            for (sy = 0; sy < 0x09 * 2; sy++) {
                int offsy, offsx, offs, colour, code, transp;
                int n;
                n = ny + sy + 2;
                offsy = ((n & 0x0f) * 4 | ((n & 0x30) * 0x100)) & 0x3fff;
                offsx = ((nx + sx) * 0x040) & 0xfff;
                offs = offsy + offsx;
                offs &= 0x3fff;

                code = cps1_scroll2.READ_WORD(offs);
                colour = cps1_scroll2.READ_WORD(offs + 2);

                transp = cps1_transparency_scroll[(colour & 0x0180) >> 7];

                cps1_draw_tile16(bitmap,
                        Machine.gfx[2],
                        code + base,
                        colour & 0x1f,
                        colour & 0x20, colour & 0x40,
                        16 * sx - nxoffset,
                        16 * sy - nyoffset,
                        transp);
            }
        }
    }

    public static void cps1_render_scroll2_low(osd_bitmap bitmap) {
        int scrly = -(scroll2y - 0x20);
        int scrlx = -(scroll2x + 0x40 - 0x20);

        if (cps1_flip_screen != 0) {
            scrly = (CPS1_SCROLL2_HEIGHT * 16) - scrly;
        }

        cps1_render_scroll2_bitmap(cps1_scroll2_bitmap);

        copyscrollbitmap(bitmap, cps1_scroll2_bitmap, 1, new int[]{scrlx}, 1, new int[]{scrly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);
    }

    public static void cps1_render_scroll2_distort(osd_bitmap bitmap) {
        int scrly = -scroll2y;
        int i;
        int[] scrollx = new int[1024];
        int otheroffs;

        /*
         Games known to use row scrolling:

         SF2
         Mega Twins (underwater, cave)
         Carrier Air Wing (hazy background at beginning of mission 8)
         Magic Sword (fire on floor 3; screen distort after continue)
         Varth (title screen)
         */
        if (cps1_flip_screen != 0) {
            scrly = (CPS1_SCROLL2_HEIGHT * 16) - scrly;
        }

        cps1_render_scroll2_bitmap(cps1_scroll2_bitmap);

        otheroffs = cps1_port.handler(CPS1_ROWSCROLL_OFFS);

        for (i = 0; i < 256; i++) {
            scrollx[(i - scrly) & 0x3ff] = -(scroll2x + 0x40 - 0x20) - cps1_other.READ_WORD((2 * (i + otheroffs)) & 0x7ff);
        }

        scrly += 0x20;

        copyscrollbitmap(bitmap, cps1_scroll2_bitmap, 1024, scrollx, 1, new int[]{scrly}, Machine.drv.visible_area, TRANSPARENCY_PEN, palette_transparent_pen);
    }
    

/***************************************************************************

  Scroll 3 (32x32 layer)

  Attribute word layout:
  0x0001	colour
  0x0002	colour
  0x0004	colour
  0x0008	colour
  0x0010	colour
  0x0020	X Flip
  0x0040	Y Flip
  0x0080
  0x0100
  0x0200
  0x0400
  0x0800
  0x1000
  0x2000
  0x4000
  0x8000

***************************************************************************/


    public static void cps1_palette_scroll3(char[] base, int offset) {
        int sx, sy;
        int nx = (scroll3x >> 5) + 1;
        int ny = (scroll3y >> 5) - 1;
        int basecode = cps1_game_config.bank_scroll3 * 0x01000;

        for (sx = 0; sx < 0x32 / 4 + 2; sx++) {
            for (sy = 0; sy < 0x20 / 4 + 2; sy++) {
                int offsy, offsx, offs, colour, code;
                int n;
                n = ny + sy;
                offsy = ((n & 0x07) * 4 | ((n & 0xf8) * 0x0100)) & 0x3fff;
                offsx = ((nx + sx) * 0x020) & 0x7ff;
                offs = offsy + offsx;
                offs &= 0x3fff;
                code = basecode + cps1_scroll3.READ_WORD(offs);
                if (cps1_game_config.kludge == 2 && code >= 0x01500) {
                    code -= 0x1000;
                }
                colour = cps1_scroll3.READ_WORD(offs + 2);
                if (code < cps1_max_tile32) {
                    base[offset + (colour & 0x1f)] |= cps1_tile32_pen_usage[code];
                }
            }
        }
    }

    public static void cps1_render_scroll3(osd_bitmap bitmap, int priority) {
        int sx, sy;
        int nxoffset = scroll3x & 0x1f;
        int nyoffset = scroll3y & 0x1f;
        int nx = (scroll3x >> 5) + 1;
        int ny = (scroll3y >> 5) - 1;
        int basecode = cps1_game_config.bank_scroll3 * 0x01000;
        int startcode = cps1_game_config.start_scroll3;
        int endcode = cps1_game_config.end_scroll3;

        for (sx = 1; sx < 0x32 / 4 + 2; sx++) {
            for (sy = 1; sy < 0x20 / 4 + 2; sy++) {
                int offsy, offsx, offs, colour, code;
                int n;
                n = ny + sy;
                offsy = ((n & 0x07) * 4 | ((n & 0xf8) * 0x0100)) & 0x3fff;
                offsx = ((nx + sx) * 0x020) & 0x7ff;
                offs = offsy + offsx;
                offs &= 0x3fff;
                code = cps1_scroll3.READ_WORD(offs);
                if (code >= startcode && code <= endcode) {
                    int transp;

                    code += basecode;
                    if (cps1_game_config.kludge == 2 && code >= 0x01500) {
                        code -= 0x1000;
                    }
                    colour = cps1_scroll3.READ_WORD(offs + 2);
                    if (priority != 0) {
                        transp = cps1_transparency_scroll[(colour & 0x0180) >> 7];
                    } else {
                        transp = 0x7fff;
                    }

                    cps1_draw_tile32(bitmap, Machine.gfx[3],
                            code,
                            colour & 0x1f,
                            colour & 0x20, colour & 0x40,
                            32 * sx - nxoffset, 32 * sy - nyoffset,
                            transp);
                }
            }
        }
    }

    public static void cps1_render_layer(osd_bitmap bitmap, int layer, int distort) {
        if (cps1_layer_enabled[layer] != 0) {
            ///System.out.println("layer "+layer);
            switch (layer) {
                case 0:
                    cps1_render_sprites(bitmap);
                    break;
                case 1:
                    cps1_render_scroll1(bitmap, 0);
                    break;
                case 2:
                    if (distort != 0) {
                        cps1_render_scroll2_distort(bitmap);
                    } else {
                        cps1_render_scroll2_low(bitmap);
                    }
                    break;
                case 3:
                    cps1_render_scroll3(bitmap, 0);
                    break;
            }
        }
    }

    public static void cps1_render_high_layer(osd_bitmap bitmap, int layer) {
        if (cps1_layer_enabled[layer] != 0) {
            switch (layer) {
                case 0:
                    /* there are no high priority sprites */
                    break;
                case 1:
                    cps1_render_scroll1(bitmap, 1);
                    break;
                case 2:
                    cps1_render_scroll2_high(bitmap);
                    break;
                case 3:
                    cps1_render_scroll3(bitmap, 1);
                    break;
            }
        }
    }

    /**
     * *************************************************************************
     *
     * Refresh screen
     *
     **************************************************************************
     */
    public static VhUpdatePtr cps1_vh_screenrefresh = new VhUpdatePtr() {
        public void handler(osd_bitmap bitmap, int full_refresh) {

            char[] palette_usage = new char[cps1_palette_entries];
            int layercontrol, l0, l1, l2, l3;
            int i, offset;
            int distort_scroll2 = 0;
            int videocontrol = cps1_port.handler(0x22);
            int old_flip;

            old_flip = cps1_flip_screen;
            cps1_flip_screen = videocontrol & 0x8000;
            if (old_flip != cps1_flip_screen) {
                /* Mark all of scroll 2 as dirty */
                memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
            }

            layercontrol = cps1_output.READ_WORD(cps1_game_config.layer_control);

            distort_scroll2 = videocontrol & 0x01;

            /* Get video memory base registers */
            cps1_get_video_base();

            /* Find the offset of the last sprite in the sprite table */
            cps1_find_last_sprite();

            /* Build palette */
            cps1_build_palette();

            /* Compute the used portion of the palette */
            memset(palette_usage, 0, sizeof(palette_usage));
            cps1_palette_sprites(palette_usage, cps1_obj_palette);
            if (cps1_layer_enabled[1] != 0) {
                cps1_palette_scroll1(palette_usage, cps1_scroll1_palette);
            }
            if (cps1_layer_enabled[2] != 0) {
                cps1_palette_scroll2(palette_usage, cps1_scroll2_palette);
            } else {
                memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
            }
            if (cps1_layer_enabled[3] != 0) {
                cps1_palette_scroll3(palette_usage, cps1_scroll3_palette);
            }

            for (i = offset = 0; i < cps1_palette_entries; i++) {
                int usage = palette_usage[i];
                if (usage != 0) {
                    int j;
                    for (j = 0; j < 15; j++) {
                        if ((usage & (1 << j)) != 0) {
                            palette_used_colors.write(offset++, PALETTE_COLOR_USED);
                        } else {
                            palette_used_colors.write(offset++, PALETTE_COLOR_UNUSED);
                        }
                    }
                    palette_used_colors.write(offset++, PALETTE_COLOR_TRANSPARENT);
                } else {
                    memset(palette_used_colors, offset, PALETTE_COLOR_UNUSED, 16);
                    offset += 16;
                }
            }

            if (palette_recalc() != null) {
                /* Mark all of scroll 2 as dirty */
                memset(cps1_scroll2_old, 0xff, cps1_scroll2_size);
            }

            /* Blank screen */
//	fillbitmap(bitmap,palette_transparent_pen,&Machine->drv->visible_area);
// TODO: the draw functions don't clip correctly at the sides of the screen, so
// for now let's clear the whole bitmap otherwise ctrl-f11 would show wrong counts
            fillbitmap(bitmap, palette_transparent_pen, null);


            /* Draw layers */
            l0 = (layercontrol >> 0x06) & 03;
            l1 = (layercontrol >> 0x08) & 03;
            l2 = (layercontrol >> 0x0a) & 03;
            l3 = (layercontrol >> 0x0c) & 03;

            cps1_render_layer(bitmap, l0, distort_scroll2);
            cps1_render_layer(bitmap, l1, distort_scroll2);
            if (l1 == 0) {
                cps1_render_high_layer(bitmap, l0);	/* overlay sprites */

            }
            cps1_render_layer(bitmap, l2, distort_scroll2);
            if (l2 == 0) {
                cps1_render_high_layer(bitmap, l1);	/* overlay sprites */

            }
            cps1_render_layer(bitmap, l3, distort_scroll2);
            if (l3 == 0) {
                cps1_render_high_layer(bitmap, l2);	/* overlay sprites */

            }

        }
    };
    public static VhEofCallbackPtr cps1_eof_callback = new VhEofCallbackPtr() {
        public void handler() {
            /* Get video memory base registers */
            cps1_get_video_base();

            /* Mish: 181099: Buffer sprites for next frame - the hardware must do
             this at the end of vblank */
            memcpy(cps1_buffered_obj, cps1_obj, cps1_obj_size);
        }
    };
}
