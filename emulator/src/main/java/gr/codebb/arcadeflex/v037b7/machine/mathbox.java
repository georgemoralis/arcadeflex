/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */
package gr.codebb.arcadeflex.v037b7.machine;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class mathbox {

    /* math box scratch registers */
    public static short[] mb_reg = new short[16];

    /* math box result */
    public static short mb_result = 0;

    public static short REG0 = mb_reg[0x00];
    public static short REG1 = mb_reg[0x01];
    public static short REG2 = mb_reg[0x02];
    public static short REG3 = mb_reg[0x03];
    public static short REG4 = mb_reg[0x04];
    public static short REG5 = mb_reg[0x05];
    public static short REG6 = mb_reg[0x06];
    public static short REG7 = mb_reg[0x07];
    public static short REG8 = mb_reg[0x08];
    public static short REG9 = mb_reg[0x09];
    public static short REGa = mb_reg[0x0a];
    public static short REGb = mb_reg[0x0b];
    public static short REGc = mb_reg[0x0c];
    public static short REGd = mb_reg[0x0d];
    public static short REGe = mb_reg[0x0e];
    public static short REGf = mb_reg[0x0f];

    /*define MB_TEST*/
    public static WriteHandlerPtr mb_go_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            int mb_temp;
            /* temp 32-bit multiply results */
            short mb_q;
            /* temp used in division */
            int msb;

            switch (offset) {
                case 0x00:
                    mb_result = REG0 = (short) ((REG0 & 0xff00) | data);
                    break;
                case 0x01:
                    mb_result = REG0 = (short) ((REG0 & 0x00ff) | (data << 8));
                    break;
                case 0x02:
                    mb_result = REG1 = (short) ((REG1 & 0xff00) | data);
                    break;
                case 0x03:
                    mb_result = REG1 = (short) ((REG1 & 0x00ff) | (data << 8));
                    break;
                case 0x04:
                    mb_result = REG2 = (short) ((REG2 & 0xff00) | data);
                    break;
                case 0x05:
                    mb_result = REG2 = (short) ((REG2 & 0x00ff) | (data << 8));
                    break;
                case 0x06:
                    mb_result = REG3 = (short) ((REG3 & 0xff00) | data);
                    break;
                case 0x07:
                    mb_result = REG3 = (short) ((REG3 & 0x00ff) | (data << 8));
                    break;
                case 0x08:
                    mb_result = REG4 = (short) ((REG4 & 0xff00) | data);
                    break;
                case 0x09:
                    mb_result = REG4 = (short) ((REG4 & 0x00ff) | (data << 8));
                    break;

                case 0x0a:
                    mb_result = REG5 = (short) ((REG5 & 0xff00) | data);
                    break;
                /* note: no function loads low part of REG5 without performing a computation */

                case 0x0c:
                    mb_result = REG6 = (short) (data);
                    break;
                /* note: no function loads high part of REG6 */

                case 0x15:
                    mb_result = REG7 = (short) ((REG7 & 0xff00) | data);
                    break;
                case 0x16:
                    mb_result = REG7 = (short) ((REG7 & 0x00ff) | (data << 8));
                    break;

                case 0x1a:
                    mb_result = REG8 = (short) ((REG8 & 0xff00) | data);
                    break;
                case 0x1b:
                    mb_result = REG8 = (short) ((REG8 & 0x00ff) | (data << 8));
                    break;

                case 0x0d:
                    mb_result = REGa = (short) ((REGa & 0xff00) | data);
                    break;
                case 0x0e:
                    mb_result = REGa = (short) ((REGa & 0x00ff) | (data << 8));
                    break;
                case 0x0f:
                    mb_result = REGb = (short) ((REGb & 0xff00) | data);
                    break;
                case 0x10:
                    mb_result = REGb = (short) ((REGb & 0x00ff) | (data << 8));
                    break;

                case 0x17:
                    mb_result = REG7;
                    break;
                case 0x19:
                    mb_result = REG8;
                    break;
                case 0x18:
                    mb_result = REG9;
                    break;

                case 0x0b:

                    REG5 = (short) ((REG5 & 0x00ff) | (data << 8));

                    REGf = (short) 0xffff;
                    REG4 -= REG2;
                    REG5 -= REG3;

                    //step_048:
                    mb_temp = ((int) REG0) * ((int) REG4);
                    REGc = (short) (mb_temp >> 16);
                    REGe = (short) (mb_temp & 0xffff);

                    mb_temp = ((int) -REG1) * ((int) REG5);
                    REG7 = (short) (mb_temp >> 16);
                    mb_q = (short) (mb_temp & 0xffff);

                    REG7 += REGc;

                    /* rounding */
                    REGe = (short) ((REGe >> 1) & 0x7fff);
                    REGc = (short) ((mb_q >> 1) & 0x7fff);
                    mb_q = (short) (REGc + REGe);
                    if (mb_q < 0) {
                        REG7++;
                    }

                    mb_result = REG7;

                    if (REGf < 0) {
                        break;
                    }

                    REG7 += REG2;

                /* fall into command 12 */
                case 0x12:

                    mb_temp = ((int) REG1) * ((int) REG4);
                    REGc = (short) (mb_temp >> 16);
                    REG9 = (short) (mb_temp & 0xffff);

                    mb_temp = ((int) REG0) * ((int) REG5);
                    REG8 = (short) (mb_temp >> 16);
                    mb_q = (short) (mb_temp & 0xffff);

                    REG8 += REGc;

                    /* rounding */
                    REG9 = (short) ((REG9 >> 1) & 0x7fff);
                    REGc = (short) ((mb_q >> 1) & 0x7fff);
                    REG9 += REGc;
                    if (REG9 < 0) {
                        REG8++;
                    }
                    REG9 <<= 1;
                    /* why? only to get the desired load address? */

                    mb_result = REG8;

                    if (REGf < 0) {
                        break;
                    }

                    REG8 += REG3;

                    REG9 &= 0xff00;

                /* fall into command 13 */
                case 0x13:

                    REGc = REG9;
                    mb_q = REG8;
                    //goto step_0bf;
                     {
                        REGe = (short) (REG7 ^ mb_q);
                        /* save sign of result */
                        REGd = mb_q;
                        if (mb_q >= 0) {
                            mb_q = REGc;
                        } else {
                            REGd = (short) (-mb_q - 1);
                            mb_q = (short) (-REGc - 1);
                            if ((mb_q < 0) && ((mb_q + 1) < 0)) {
                                REGd++;
                            }
                            mb_q++;
                        }

                        /* step 0c9: */
 /* REGc = abs (REG7) */
                        if (REG7 >= 0) {
                            REGc = REG7;
                        } else {
                            REGc = (short) -REG7;
                        }

                        REGf = REG6;
                        /* step counter */

                        do {
                            REGd -= REGc;
                            msb = ((mb_q & 0x8000) != 0) ? 1 : 0;
                            mb_q <<= 1;
                            if (REGd >= 0) {
                                mb_q++;
                            } else {
                                REGd += REGc;
                            }
                            REGd <<= 1;
                            REGd += msb;
                        } while (--REGf >= 0);

                        if (REGe >= 0) {
                            mb_result = mb_q;
                        } else {
                            mb_result = (short) -mb_q;
                        }
                        break;
                    }

                case 0x14:
                    REGc = REGa;
                    mb_q = REGb;

                    //step_0bf:
                    REGe = (short) (REG7 ^ mb_q);
                    /* save sign of result */
                    REGd = mb_q;
                    if (mb_q >= 0) {
                        mb_q = REGc;
                    } else {
                        REGd = (short) (-mb_q - 1);
                        mb_q = (short) (-REGc - 1);
                        if ((mb_q < 0) && ((mb_q + 1) < 0)) {
                            REGd++;
                        }
                        mb_q++;
                    }

                    /* step 0c9: */
 /* REGc = abs (REG7) */
                    if (REG7 >= 0) {
                        REGc = REG7;
                    } else {
                        REGc = (short) -REG7;
                    }

                    REGf = REG6;
                    /* step counter */

                    do {
                        REGd -= REGc;
                        msb = ((mb_q & 0x8000) != 0) ? 1 : 0;
                        mb_q <<= 1;
                        if (REGd >= 0) {
                            mb_q++;
                        } else {
                            REGd += REGc;
                        }
                        REGd <<= 1;
                        REGd += msb;
                    } while (--REGf >= 0);

                    if (REGe >= 0) {
                        mb_result = mb_q;
                    } else {
                        mb_result = (short) -mb_q;
                    }
                    break;

                case 0x11:
                    REG5 = (short) ((REG5 & 0x00ff) | (data << 8));
                    REGf = 0x0000;
                    /* do everything in one step */
                    //goto step_048;
                     {
                        mb_temp = ((int) REG0) * ((int) REG4);
                        REGc = (short) (mb_temp >> 16);
                        REGe = (short) (mb_temp & 0xffff);

                        mb_temp = ((int) -REG1) * ((int) REG5);
                        REG7 = (short) (mb_temp >> 16);
                        mb_q = (short) (mb_temp & 0xffff);

                        REG7 += REGc;

                        /* rounding */
                        REGe = (short) ((REGe >> 1) & 0x7fff);
                        REGc = (short) ((mb_q >> 1) & 0x7fff);
                        mb_q = (short) (REGc + REGe);
                        if (mb_q < 0) {
                            REG7++;
                        }

                        mb_result = REG7;

                        if (REGf < 0) {
                            break;
                        }

                        REG7 += REG2;

                        /* fall into command 12 */
                        mb_temp = ((int) REG1) * ((int) REG4);
                        REGc = (short) (mb_temp >> 16);
                        REG9 = (short) (mb_temp & 0xffff);

                        mb_temp = ((int) REG0) * ((int) REG5);
                        REG8 = (short) (mb_temp >> 16);
                        mb_q = (short) (mb_temp & 0xffff);

                        REG8 += REGc;

                        /* rounding */
                        REG9 = (short) ((REG9 >> 1) & 0x7fff);
                        REGc = (short) ((mb_q >> 1) & 0x7fff);
                        REG9 += REGc;
                        if (REG9 < 0) {
                            REG8++;
                        }
                        REG9 <<= 1;
                        /* why? only to get the desired load address? */

                        mb_result = REG8;

                        if (REGf < 0) {
                            break;
                        }

                        REG8 += REG3;

                        REG9 &= 0xff00;

                        /* fall into command 13 */
                        REGc = REG9;
                        mb_q = REG8;
                        //goto step_0bf;
                        {
                            REGe = (short) (REG7 ^ mb_q);
                            /* save sign of result */
                            REGd = mb_q;
                            if (mb_q >= 0) {
                                mb_q = REGc;
                            } else {
                                REGd = (short) (-mb_q - 1);
                                mb_q = (short) (-REGc - 1);
                                if ((mb_q < 0) && ((mb_q + 1) < 0)) {
                                    REGd++;
                                }
                                mb_q++;
                            }

                            /* step 0c9: */
 /* REGc = abs (REG7) */
                            if (REG7 >= 0) {
                                REGc = REG7;
                            } else {
                                REGc = (short) -REG7;
                            }

                            REGf = REG6;
                            /* step counter */

                            do {
                                REGd -= REGc;
                                msb = ((mb_q & 0x8000) != 0) ? 1 : 0;
                                mb_q <<= 1;
                                if (REGd >= 0) {
                                    mb_q++;
                                } else {
                                    REGd += REGc;
                                }
                                REGd <<= 1;
                                REGd += msb;
                            } while (--REGf >= 0);

                            if (REGe >= 0) {
                                mb_result = mb_q;
                            } else {
                                mb_result = (short) -mb_q;
                            }

                        }
                    }
                    break;

                case 0x1c:
                    /* window test? */
                    REG5 = (short) ((REG5 & 0x00ff) | (data << 8));
                    do {
                        REGe = (short) ((REG4 + REG7) >> 1);
                        REGf = (short) ((REG5 + REG8) >> 1);
                        if ((REGb < REGe) && (REGf < REGe) && ((REGe + REGf) >= 0)) {
                            REG7 = REGe;
                            REG8 = REGf;
                        } else {
                            REG4 = REGe;
                            REG5 = REGf;
                        }
                    } while (--REG6 >= 0);

                    mb_result = REG8;
                    break;

                case 0x1d:
                    REG3 = (short) ((REG3 & 0x00ff) | (data << 8));

                    REG2 -= REG0;
                    if (REG2 < 0) {
                        REG2 = (short) -REG2;
                    }

                    REG3 -= REG1;
                    if (REG3 < 0) {
                        REG3 = (short) -REG3;
                    }

                /* fall into command 1e */
                case 0x1e:
                    /* result = max (REG2, REG3) + 3/8 * min (REG2, REG3) */
                    if (REG3 >= REG2) {
                        REGc = REG2;
                        REGd = REG3;
                    } else {
                        REGd = REG2;
                        REGc = REG3;
                    }
                    REGc >>= 2;
                    REGd += REGc;
                    REGc >>= 1;
                    mb_result = REGd = (short) (REGc + REGd);
                    break;

                case 0x1f:
                    logerror("math box function 0x1f\n");
                    /* $$$ do some computation here (selftest? signature analysis? */
                    break;
            }

        }
    };

    public static ReadHandlerPtr mb_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return 0x00;
            /* always done! */
        }
    };

    public static ReadHandlerPtr mb_lo_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return mb_result & 0xff;
        }
    };

    public static ReadHandlerPtr mb_hi_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            return (mb_result >> 8) & 0xff;
        }
    };
}
