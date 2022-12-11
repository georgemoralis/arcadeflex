/*
 * ported to v0.36
 */
package arcadeflex.v036.mame;

//java imports
import java.util.ArrayList;

public class commonH {

    public static class RomModule {

        public String name;/* name of the file to load */
        public int offset;/* offset to load it to */        //UINT32!!!
        public int length;/* length of the file */          //UINT32!!!
        public int crc;/* standard CRC-32 checksum */    //UINT32!!!

        public RomModule(String n, int o, int s) {
            name = n;
            offset = o;
            length = s;
        }

        public RomModule(String n, int o, int s, int c) {
            name = n;
            offset = o;
            length = s;
            crc = c;
        }
    }
    /* there are some special cases for the above. name, offset and size all set to 0 */
 /* mark the end of the array. If name is 0 and the others aren't, that means "continue */
 /* reading the previous rom from this address". If length is 0 and offset is not 0, */
 /* that marks the start of a new memory region. Confused? Well, don't worry, just use */
 /* the macros below. */

    public static final int ROMFLAG_MASK = 0xf8000000;/* 5 bits worth of flags in the high nibble */

 /* Masks for individual ROMs */
    public static final int ROMFLAG_ALTERNATE = 0x80000000;/* Alternate bytes, either even or odd, or nibbles, low or high */
    public static final int ROMFLAG_WIDE = 0x40000000;/* 16-bit ROM; may need byte swapping */
    public static final int ROMFLAG_SWAP = 0x20000000;/* 16-bit ROM with bytes in wrong order */
    public static final int ROMFLAG_NIBBLE = 0x10000000;/* Nibble-wide ROM image */
    public static final int ROMFLAG_QUAD = 0x08000000;/* 32-bit data arranged as 4 interleaved 8-bit roms */

    public static final int REGION_INVALID = 0x80;
    public static final int REGION_CPU1 = 0x81;
    public static final int REGION_CPU2 = 0x82;
    public static final int REGION_CPU3 = 0x83;
    public static final int REGION_CPU4 = 0x84;
    public static final int REGION_CPU5 = 0x85;
    public static final int REGION_CPU6 = 0x86;
    public static final int REGION_CPU7 = 0x87;
    public static final int REGION_CPU8 = 0x88;
    public static final int REGION_GFX1 = 0x89;
    public static final int REGION_GFX2 = 0x8A;
    public static final int REGION_GFX3 = 0x8B;
    public static final int REGION_GFX4 = 0x8C;
    public static final int REGION_GFX5 = 0x8D;
    public static final int REGION_GFX6 = 0x8E;
    public static final int REGION_GFX7 = 0x8F;
    public static final int REGION_GFX8 = 0x90;
    public static final int REGION_PROMS = 0x91;
    public static final int REGION_SOUND1 = 0x92;
    public static final int REGION_SOUND2 = 0x93;
    public static final int REGION_SOUND3 = 0x94;
    public static final int REGION_SOUND4 = 0x95;
    public static final int REGION_SOUND5 = 0x96;
    public static final int REGION_SOUND6 = 0x97;
    public static final int REGION_SOUND7 = 0x98;
    public static final int REGION_SOUND8 = 0x99;
    public static final int REGION_USER1 = 0x9A;
    public static final int REGION_USER2 = 0x9B;
    public static final int REGION_USER3 = 0x9C;
    public static final int REGION_USER4 = 0x9D;
    public static final int REGION_USER5 = 0x9E;
    public static final int REGION_USER6 = 0x9F;
    public static final int REGION_USER7 = 0xA0;
    public static final int REGION_USER8 = 0xA1;
    public static final int REGION_MAX = 0xA2;

    public static int BADCRC(int crc) {
        return ~crc;
    }

    public static final int REGIONFLAG_MASK = 0xf8000000;
    public static final int REGIONFLAG_DISPOSE = 0x80000000;
    /* Dispose of this region when done */
    public static final int REGIONFLAG_SOUNDONLY = 0x40000000;
    /* load only if sound emulation is turned on */

    public static RomModule[] rommodule_macro = null;
    static ArrayList<RomModule> arload = new ArrayList<RomModule>();

    /* start of memory region */
    public static void ROM_REGION(int offset, int type) {
        arload.add(new RomModule(null, offset, 0, type));
    }

    /* ROM to load */
    public static void ROM_LOAD(String name, int offset, int size, int crc) {
        arload.add(new RomModule(name, offset, size, crc));
    }

    /* continue loading the previous ROM to a new address */
    public static void ROM_CONTINUE(int offset, int length) {
        arload.add(new RomModule(null, offset, length, 0));
    }

    /* restart loading the previous ROM to a new address */
    public static void ROM_RELOAD(int offset, int length) {
        arload.add(new RomModule("-1", offset, length, 0));
    }

    /* These are for nibble-wide ROMs, can be used with code or data */
    public static void ROM_LOAD_NIB_LOW(String name, int offset, int length, int crc) {
        arload.add(new RomModule(name, offset, (length) | ROMFLAG_NIBBLE, crc));
    }

    public static void ROM_LOAD_NIB_HIGH(String name, int offset, int length, int crc) {
        arload.add(new RomModule(name, offset, (length) | ROMFLAG_NIBBLE | ROMFLAG_ALTERNATE, crc));
    }

    /*TODO*/ //#define ROM_RELOAD_NIB_LOW(offset,length) { (char *)-1, offset, (length) | ROMFLAG_NIBBLE, 0 },
/*TODO*/ //#define ROM_RELOAD_NIB_HIGH(offset,length) { (char *)-1, offset, (length) | ROMFLAG_NIBBLE | ROMFLAG_ALTERNATE, 0 },

    /* The following ones are for code ONLY - don't use for graphics data!!! */
 /* load the ROM at even/odd addresses. Useful with 16 bit games */
    public static void ROM_LOAD_EVEN(String name, int offset, int length, int crc) {
        arload.add(new RomModule(name, (offset) & ~1, (length) | ROMFLAG_ALTERNATE, crc));
    }

    public static void ROM_RELOAD_EVEN(int offset, int length) {
        arload.add(new RomModule("-1", (offset) & ~1, (length) | ROMFLAG_ALTERNATE, 0));
    }

    public static void ROM_LOAD_ODD(String name, int offset, int length, int crc) {
        arload.add(new RomModule(name, (offset) | 1, (length) | ROMFLAG_ALTERNATE, crc));
    }

    public static void ROM_RELOAD_ODD(int offset, int length) {
        arload.add(new RomModule("-1", (offset) | 1, (length) | ROMFLAG_ALTERNATE, 0));
    }

    /* load the ROM at even/odd addresses. Useful with 16 bit games */
    public static void ROM_LOAD_WIDE(String name, int offset, int length, int crc) {
        arload.add(new RomModule(name, offset, (length) | ROMFLAG_WIDE, crc));
    }

    /*TODO*/ //#define ROM_RELOAD_WIDE(offset,length) { (char *)-1, offset, (length) | ROMFLAG_WIDE, 0 },
    public static void ROM_LOAD_WIDE_SWAP(String name, int offset, int length, int crc) {
        arload.add(new RomModule(name, offset, (length) | ROMFLAG_WIDE | ROMFLAG_SWAP, crc));
    }

    /*TODO*/ //#define ROM_RELOAD_WIDE_SWAP(offset,length) { (char *)-1, offset, (length) | ROMFLAG_WIDE | ROMFLAG_SWAP, 0 },
    /* Data is split between 4 roms, always use this in groups of 4! */
 /*TODO*/ //#define ROM_LOAD_QUAD(name,offset,length,crc) { name, offset, length | ROMFLAG_QUAD, crc },

    /*TODO*/ //    #define ROM_LOAD_V20_EVEN	ROM_LOAD_EVEN
/*TODO*///     #define ROM_RELOAD_V20_EVEN  ROM_RELOAD_EVEN
/*TODO*/ //    #define ROM_LOAD_V20_ODD	ROM_LOAD_ODD
/*TODO*///     #define ROM_RELOAD_V20_ODD   ROM_RELOAD_ODD
/*TODO*///     #define ROM_LOAD_V20_WIDE	ROM_LOAD_WIDE

    /* Use THESE ones for graphics data */
    public static void ROM_LOAD_GFX_EVEN(String name, int offset, int length, int crc) {
        ROM_LOAD_ODD(name, offset, length, crc);
    }

    public static void ROM_LOAD_GFX_ODD(String name, int offset, int length, int crc) {
        ROM_LOAD_EVEN(name, offset, length, crc);
    }

    public static void ROM_LOAD_GFX_SWAP(String name, int offset, int length, int crc) {
        ROM_LOAD_WIDE(name, offset, length, crc);
    }

    /* end of table */
    public static void ROM_END() {
        arload.add(new RomModule(null, 0, 0));
        rommodule_macro = arload.toArray(new RomModule[arload.size()]);
        arload.clear();
    }

    public static class GameSample {

        public GameSample() {
            data = new byte[1];
        }

        public GameSample(int len) {
            data = new byte[len];
        }

        public int length;
        public int smpfreq;
        public int resolution;
        public byte data[];/*1? */ /* extendable */
    }

    public static class GameSamples {

        public GameSamples() {
            sample = new GameSample[1];
            sample[0] = new GameSample();
        }

        public GameSamples(int size) {
            sample = new GameSample[size];
            for (int i = 0; i < size; i++) {
                sample[i] = new GameSample(1);
            }
        }

        public int total;/* total number of samples */
        public GameSample sample[];/*1? */ /* extendable */
    }
    public static final int COIN_COUNTERS = 4;/* total # of coin counters */
}
