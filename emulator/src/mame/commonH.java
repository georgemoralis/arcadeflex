/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mame;

/**
 *
 * @author shadow
 */
public class commonH {
    /*rom definations from old arcadeflex to be cleaned */
 	public static class RomModule
	{
		public RomModule(String n, int o, int s) { name = n; offset = o; length = s; };//function without CRC
		public String name;	/* name of the file to load */
		public int offset;			/* offset to load it to */
		public int length;			/* length of the file */
                public int crc;                   /* our custom checksum */
                public RomModule(String n, int o, int s,int c) { name = n; offset = o; length = s; crc=c;};//new definition for when we support checksum...
	};
        /* there are some special cases for the above. name, offset and size all set to 0 */
        /* mark the end of the aray. If name is 0 and the others aren't, that means "continue */
        /* reading the previous from from this address". If length is 0 and offset is not 0, */
        /* that marks the start of a new memory region. Confused? Well, don't worry, just use */
        /* the macros below. */
 
public static final int ROMFLAG_MASK=          0xf8000000;           /* 5 bits worth of flags in the high nibble */        
/* Masks for individual ROMs */
public static final int ROMFLAG_ALTERNATE     =0x80000000;           /* Alternate bytes, either even or odd, or nibbles, low or high */
public static final int ROMFLAG_WIDE          =0x40000000;          /* 16-bit ROM; may need byte swapping */
public static final int ROMFLAG_SWAP          =0x20000000;          /* 16-bit ROM with bytes in wrong order */
public static final int ROMFLAG_NIBBLE        =0x10000000;           /* Nibble-wide ROM image */
public static final int ROMFLAG_QUAD          =0x08000000;           /* 32-bit data arranged as 4 interleaved 8-bit roms */



public static final int REGIONFLAG_MASK=			0xf8000000;
public static final int REGIONFLAG_DISPOSE=		0x80000000;           /* Dispose of this region when done */
public static final int REGIONFLAG_SOUNDONLY=	0x40000000;           /* load only if sound emulation is turned on */

public static final int 	REGION_INVALID = 0x80;
public static final int 	REGION_CPU1=0x81;
public static final int 	REGION_CPU2=0x82;
public static final int 	REGION_CPU3=0x83;
public static final int 	REGION_CPU4=0x84;
public static final int 	REGION_CPU5=0x85;
public static final int 	REGION_CPU6=0x86;
public static final int 	REGION_CPU7=0x87;
public static final int 	REGION_CPU8=0x88;
public static final int 	REGION_GFX1=0x89;
public static final int 	REGION_GFX2=0x8A;
public static final int 	REGION_GFX3=0x8B;
public static final int 	REGION_GFX4=0x8C;
public static final int 	REGION_GFX5=0x8D;
public static final int 	REGION_GFX6=0x8E;
public static final int 	REGION_GFX7=0x8F;
public static final int 	REGION_GFX8=0x90;
public static final int 	REGION_PROMS=0x91;
public static final int 	REGION_SOUND1=0x92;
public static final int 	REGION_SOUND2=0x93;
public static final int 	REGION_SOUND3=0x94;
public static final int 	REGION_SOUND4=0x95;
public static final int 	REGION_SOUND5=0x96;
public static final int 	REGION_SOUND6=0x97;
public static final int 	REGION_SOUND7=0x98;
public static final int 	REGION_SOUND8=0x99;
public static final int 	REGION_USER1=0x9A;
public static final int 	REGION_USER2=0x9B;
public static final int 	REGION_USER3=0x9C;
public static final int 	REGION_USER4=0x9D;
public static final int 	REGION_USER5=0x9E;
public static final int 	REGION_USER6=0x9F;
public static final int 	REGION_USER7=0xA0;
public static final int 	REGION_USER8=0xA1;
public static final int 	REGION_MAX=0xA2;
                
        static int TEMP_MODULE_SIZE=50;//TODO i don't like that but how else?
        static RomModule[] tempmodule = new RomModule[TEMP_MODULE_SIZE]; 
        static int curpos=0;
        static RomModule[] rommodule_macro=null;
        /* start of memory region */
        public static void ROM_REGION(int offset,int type)
        {
           tempmodule[curpos]=new RomModule( null, offset, 0,type );
           curpos++;
        }
        /* ROM to load */
        public static void ROM_LOAD(String name,int offset,int size,int crc)
        {
            tempmodule[curpos]=new RomModule( name,offset,size,crc);
            curpos++;
        }

        /* continue loading the previous ROM to a new address */
        public static void ROM_CONTINUE(int offset,int length)
        {
             tempmodule[curpos]=new RomModule( null,offset,length,0);
             curpos++;
        }
        /* restart loading the previous ROM to a new address */
        public static void ROM_RELOAD(int offset,int length)
        {
            tempmodule[curpos]=new RomModule( "-1",offset,length,0);
            curpos++;
        }
        /* load the ROM at even/odd addresses. Useful with 16 bit games */
        
        public static void ROM_LOAD_EVEN(String name,int offset,int length,int checksum) 
        { 
            tempmodule[curpos]=new RomModule(name, offset & ~1, length | 0x80000000, checksum);
            curpos++;
        }
        public static void ROM_LOAD_ODD(String name,int offset,int length,int checksum) 
        { 
            tempmodule[curpos]=new RomModule(name, offset |  1, length | 0x80000000, checksum);
            curpos++;
        }
        /* end of table */
        public static void ROM_END()
        {
            tempmodule[curpos]=new RomModule( null, 0, 0,0 );
            curpos++;
            rommodule_macro=null;
            rommodule_macro=new RomModule[curpos];
            System.arraycopy(tempmodule, 0, rommodule_macro, 0, curpos);
            curpos=0;//reset curpos
            tempmodule=null;
            tempmodule=new RomModule[TEMP_MODULE_SIZE];//reset tempmodule
        }   
}
