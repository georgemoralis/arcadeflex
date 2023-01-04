/**
 * ported to 0.36
 */
/**
 * Changelog
 * =========
 * 04/01/2023 - shadow - This file should be complete for 0.36 version
 */
package arcadeflex.v036.machine;

//mame imports
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.memory.*;
//TODO
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;

public class konami {

    static char decodebyte(char opcode, char address) {
        /*
         >
         > CPU_D7 = (EPROM_D7 & ~ADDRESS_1) | (~EPROM_D7 & ADDRESS_1)  >
         > CPU_D6 = EPROM_D6
         >
         > CPU_D5 = (EPROM_D5 & ADDRESS_1) | (~EPROM_D5 & ~ADDRESS_1) >
         > CPU_D4 = EPROM_D4
         >
         > CPU_D3 = (EPROM_D3 & ~ADDRESS_3) | (~EPROM_D3 & ADDRESS_3) >
         > CPU_D2 = EPROM_D2
         >
         > CPU_D1 = (EPROM_D1 & ADDRESS_3) | (~EPROM_D1 & ~ADDRESS_3) >
         > CPU_D0 = EPROM_D0
         >
         */
        char xormask;

        xormask = 0;
        if ((address & 0x02) != 0) {
            xormask |= 0x80;
        } else {
            xormask |= 0x20;
        }
        if ((address & 0x08) != 0) {
            xormask |= 0x08;
        } else {
            xormask |= 0x02;
        }

        return (char) (opcode ^ xormask);
    }

    static void decode(int cpu) {
        UBytePtr rom = memory_region(REGION_CPU1 + cpu);
        int diff = memory_region_length(REGION_CPU1 + cpu) / 2;
        int A;

        memory_set_opcode_base(cpu, new UBytePtr(rom, diff));

        for (A = 0; A < diff; A++) {
            rom.write(A + diff, decodebyte(rom.read(A), (char) A));
        }
    }

    public static void konami1_decode() {
        decode(0);
    }

    public static void konami1_decode_cpu4() {
        decode(3);
    }
}
