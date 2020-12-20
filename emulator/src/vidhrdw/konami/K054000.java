package vidhrdw.konami;

import static platform.libc_old.*;
import static platform.ptrlib.*;
import static mame.drawgfxH.*;
import static mame.drawgfxH.*;
import static mame.drawgfx.*;
import static mame.driverH.*;
import static mame.osdependH.*;
import static mame.mame.*;
import static mame.tilemapC.*;
import static mame.tilemapH.*;
import static mame.palette.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static mame.paletteH.*;
import static mame.mameH.*;
import static mame.cpuintrfH.*;
import static mame.memoryH.*;

public class K054000 {

    static UBytePtr K054000_ram = new UBytePtr(0x20);

    public static WriteHandlerPtr collision_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

        }
    };
    public static WriteHandlerPtr K054000_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {

            K054000_ram.write(offset, data);
        }
    };
    public static ReadHandlerPtr K054000_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            int Acx, Acy, Aax, Aay;
            int Bcx, Bcy, Bax, Bay;

            if (offset != 0x18) {
                return 0;
            }

            Acx = (K054000_ram.read(0x01) << 16) | (K054000_ram.read(0x02) << 8) | K054000_ram.read(0x03);
            Acy = (K054000_ram.read(0x09) << 16) | (K054000_ram.read(0x0a) << 8) | K054000_ram.read(0x0b);
            /* TODO: this is a hack to make thndrx2 pass the startup check. It is certainly wrong. */
            if (K054000_ram.read(0x04) == 0xff) {
                Acx += 3;
            }
            if (K054000_ram.read(0x0c) == 0xff) {
                Acy += 3;
            }
            Aax = K054000_ram.read(0x06) + 1;
            Aay = K054000_ram.read(0x07) + 1;

            Bcx = (K054000_ram.read(0x15) << 16) | (K054000_ram.read(0x16) << 8) | K054000_ram.read(0x17);
            Bcy = (K054000_ram.read(0x11) << 16) | (K054000_ram.read(0x12) << 8) | K054000_ram.read(0x13);
            Bax = K054000_ram.read(0x0e) + 1;
            Bay = K054000_ram.read(0x0f) + 1;

            if (Acx + Aax < Bcx - Bax) {
                return 1;
            }

            if (Bcx + Bax < Acx - Aax) {
                return 1;
            }

            if (Acy + Aay < Bcy - Bay) {
                return 1;
            }

            if (Bcy + Bay < Acy - Aay) {
                return 1;
            }

            return 0;
        }
    };

}
