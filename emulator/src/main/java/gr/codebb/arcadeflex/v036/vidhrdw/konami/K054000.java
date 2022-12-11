package gr.codebb.arcadeflex.v036.vidhrdw.konami;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static gr.codebb.arcadeflex.v036.mame.drawgfx.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.osdependH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapC.*;
import static gr.codebb.arcadeflex.v036.mame.tilemapH.*;
import static gr.codebb.arcadeflex.v037b7.mame.palette.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.paletteH.*;
import static gr.codebb.arcadeflex.v036.mame.mameH.*;
import static arcadeflex.v036.mame.cpuintrfH.*;
import static arcadeflex.v036.mame.memoryH.*;

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
