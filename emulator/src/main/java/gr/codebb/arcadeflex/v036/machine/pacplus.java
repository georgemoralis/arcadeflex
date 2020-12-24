
/*
 * ported to v0.36
 * using automatic conversion tool v0.08
 *
 *
 *
 */
package gr.codebb.arcadeflex.v036.machine;

import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;

public class pacplus {

    static char decrypt1(char e) {
        return e;
    }

    static char decrypt2(char e) {
        char d;

        d = (char) (((~e) & 0x80) >> 5);
        d |= ((~e) & 0x40) << 1;
        d |= (e & 0x20) >> 2;
        d |= (e & 0x10) >> 4;
        d |= (e & 0x08) << 2;
        d |= ((~e) & 0x04) << 2;
        d |= (e & 0x02) << 5;
        d |= ((~e) & 0x01) << 1;

        return d;
    }

    static char decrypt3(char e) {
        char d;

        d = (char) ((e & 0x80) >> 2);
        d |= ((~e) & 0x40) >> 2;
        d |= ((~e) & 0x20) >> 5;
        d |= (e & 0x10) >> 1;
        d |= ((~e) & 0x08) << 3;
        d |= ((~e) & 0x04) >> 0;
        d |= (e & 0x02) >> 0;
        d |= ((~e) & 0x01) << 7;

        return d;
    }

    static char decrypt4(char e) {
        char d;

        d = (char) ((e & 0x80) >> 0);
        d |= (e & 0x40) >> 0;
        d |= ((~e) & 0x20) >> 0;
        d |= (e & 0x10) >> 0;
        d |= ((~e) & 0x08) >> 0;
        d |= (e & 0x04) >> 0;
        d |= (e & 0x02) >> 0;
        d |= (e & 0x01) >> 0;

        return d;
    }

    static char decrypt5(char e) {
        char d;

        d = (char) (((~e) & 0x80) >> 5);
        d |= ((~e) & 0x40) << 1;
        d |= ((~e) & 0x20) >> 0;
        d |= (e & 0x10) >> 4;
        d |= ((~e) & 0x08) >> 0;
        d |= ((~e) & 0x04) << 2;
        d |= (e & 0x02) << 5;
        d |= ((~e) & 0x01) << 1;

        return d;
    }

    static char decrypt6(char e) {
        char d;

        d = (char) (((~e) & 0x80) >> 4);
        d |= ((~e) & 0x40) >> 2;
        d |= ((~e) & 0x20) >> 5;
        d |= (e & 0x10) << 1;
        d |= ((~e) & 0x08) << 3;
        d |= ((~e) & 0x04) >> 0;
        d |= (e & 0x02) >> 0;
        d |= ((~e) & 0x01) << 7;

        return d;
    }

    static char decrypt(int addr, char e) {
        int method = 1;

        switch (addr & 0x2A5) {
            case 0x001:
            case 0x201:
            case 0x005:
            case 0x025:
            case 0x080:
            case 0x084:
            case 0x085:
            case 0x0A5:
            case 0x2A5:
            case 0x200:
            case 0x221:
            case 0x2A1:
                method = 2;
                break;
            case 0x004:
            case 0x204:
            case 0x020:
            case 0x220:
            case 0x024:
            case 0x224:
            case 0x0A0:
            case 0x2A0:
            case 0x0A4:
            case 0x2A4:
            case 0x281:
            case 0x285:
                method = 3;
                break;
        }

        if ((addr & 0x800) == 0x800) {
            method = method + 3;
        }

        switch (method) {
            case 1:
                return decrypt1(e);
            case 2:
                return decrypt2(e);
            case 3:
                return decrypt3(e);
            case 4:
                return decrypt4(e);
            case 5:
                return decrypt5(e);
            case 6:
                return decrypt6(e);
        }

        return 0;
    }

    public static void pacplus_decode() {
        int i;
        UBytePtr RAM;

        /* CPU ROMs */
        RAM = memory_region(REGION_CPU1);
        for (i = 0; i < 0x4000; i++) {
            RAM.write(i, decrypt(i, RAM.read(i)));
        }
    }

}
