package gr.codebb.arcadeflex.v036.machine;


import static gr.codebb.arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.mame.memory.*;
import static gr.codebb.arcadeflex.v036.platform.ptrlib.*;

public class kabuki {

    static int bitswap1(int src, int key, int select) {
        if ((select & (1 << ((key >> 0) & 7))) != 0) {
            src = (src & 0xfc) | ((src & 0x01) << 1) | ((src & 0x02) >> 1);
        }
        if ((select & (1 << ((key >> 4) & 7))) != 0) {
            src = (src & 0xf3) | ((src & 0x04) << 1) | ((src & 0x08) >> 1);
        }
        if ((select & (1 << ((key >> 8) & 7))) != 0) {
            src = (src & 0xcf) | ((src & 0x10) << 1) | ((src & 0x20) >> 1);
        }
        if ((select & (1 << ((key >> 12) & 7))) != 0) {
            src = (src & 0x3f) | ((src & 0x40) << 1) | ((src & 0x80) >> 1);
        }

        return src;
    }

    static int bitswap2(int src, int key, int select) {
        if ((select & (1 << ((key >> 12) & 7))) != 0) {
            src = (src & 0xfc) | ((src & 0x01) << 1) | ((src & 0x02) >> 1);
        }
        if ((select & (1 << ((key >> 8) & 7))) != 0) {
            src = (src & 0xf3) | ((src & 0x04) << 1) | ((src & 0x08) >> 1);
        }
        if ((select & (1 << ((key >> 4) & 7))) != 0) {
            src = (src & 0xcf) | ((src & 0x10) << 1) | ((src & 0x20) >> 1);
        }
        if ((select & (1 << ((key >> 0) & 7))) != 0) {
            src = (src & 0x3f) | ((src & 0x40) << 1) | ((src & 0x80) >> 1);
        }

        return src;
    }

    static int bytedecode(int src, int swap_key1, int swap_key2, int xor_key, int select) {
        src = bitswap1(src, swap_key1 & 0xffff, select & 0xff);
        src = ((src & 0x7f) << 1) | ((src & 0x80) >> 7);
        src = bitswap2(src, swap_key1 >> 16, select & 0xff);
        src ^= xor_key;
        src = ((src & 0x7f) << 1) | ((src & 0x80) >> 7);
        src = bitswap2(src, swap_key2 & 0xffff, select >> 8);
        src = ((src & 0x7f) << 1) | ((src & 0x80) >> 7);
        src = bitswap1(src, swap_key2 >> 16, select >> 8);
        return src;
    }

    public static void kabuki_decode(UBytePtr src, UBytePtr dest_op, UBytePtr dest_data,
            int base_addr, int length, int swap_key1, int swap_key2, int addr_key, int xor_key) {
        int A;
        int select;

        for (A = 0; A < length; A++) {
            /* decode opcodes */
            select = (A + base_addr) + addr_key;
            dest_op.write(A, bytedecode(src.read(A), swap_key1, swap_key2, xor_key, select));

            /* decode data */
            select = ((A + base_addr) ^ 0x1fc0) + addr_key + 1;
            dest_data.write(A, bytedecode(src.read(A), swap_key1, swap_key2, xor_key, select));
        }
    }

    static void mitchell_decode(int swap_key1, int swap_key2, int addr_key, int xor_key) {
        int i;
        UBytePtr rom = memory_region(REGION_CPU1);
        int diff = memory_region_length(REGION_CPU1) / 2;

        memory_set_opcode_base(0, new UBytePtr(rom, diff));
        kabuki_decode(rom, new UBytePtr(rom, diff), rom, 0x0000, 0x8000, swap_key1, swap_key2, addr_key, xor_key);
        for (i = 0x10000; i < diff; i += 0x4000) {
            kabuki_decode(new UBytePtr(rom, i), new UBytePtr(rom, i + diff), new UBytePtr(rom, i), 0x8000, 0x4000, swap_key1, swap_key2, addr_key, xor_key);
        }
    }

    public static void mgakuen2_decode() {
        mitchell_decode(0x76543210, 0x01234567, 0xaa55, 0xa5);
    }

    public static void pang_decode() {
        mitchell_decode(0x01234567, 0x76543210, 0x6548, 0x24);
    }

    public static void cworld_decode() {
        mitchell_decode(0x04152637, 0x40516273, 0x5751, 0x43);
    }

    public static void hatena_decode() {
        mitchell_decode(0x45670123, 0x45670123, 0x5751, 0x43);
    }

    public static void spang_decode() {
        mitchell_decode(0x45670123, 0x45670123, 0x5852, 0x43);
    }

    public static void sbbros_decode() {
        mitchell_decode(0x45670123, 0x45670123, 0x2130, 0x12);
    }

    public static void marukin_decode() {
        mitchell_decode(0x54321076, 0x54321076, 0x4854, 0x4f);
    }

    public static void qtono1_decode() {
        mitchell_decode(0x12345670, 0x12345670, 0x1111, 0x11);
    }

    public static void qsangoku_decode() {
        mitchell_decode(0x23456701, 0x23456701, 0x1828, 0x18);
    }

    public static void block_decode() {
        mitchell_decode(0x02461357, 0x64207531, 0x0002, 0x01);
    }
    
    
    static void cps1_decode(int swap_key1,int swap_key2,int addr_key,int xor_key)
    {
    	UBytePtr rom = memory_region(REGION_CPU2);
    	int diff = memory_region_length(REGION_CPU2) / 2;
    
    	memory_set_opcode_base(1,new UBytePtr(rom,diff));
    	kabuki_decode(rom,new UBytePtr(rom,diff),rom,0x0000,0x8000, swap_key1,swap_key2,addr_key,xor_key);
    }
    
    public static void  wof_decode()      { cps1_decode(0x01234567,0x54163072,0x5151,0x51); }
    public static void  dino_decode()     { cps1_decode(0x76543210,0x24601357,0x4343,0x43); }
    public static void  punisher_decode() { cps1_decode(0x67452103,0x75316024,0x2222,0x22); }
    public static void  slammast_decode() { cps1_decode(0x54321076,0x65432107,0x3131,0x19); }
        
}
