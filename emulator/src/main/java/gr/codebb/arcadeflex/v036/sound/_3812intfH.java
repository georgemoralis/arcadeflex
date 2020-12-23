package gr.codebb.arcadeflex.v036.sound;

import static gr.codebb.arcadeflex.v036.mame.driverH.*;

public class _3812intfH {
    
    public static final int MAX_3812 =2;
    public static final int MAX_8950 =MAX_3812;

    public static class YM3812interface
    {
        public YM3812interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler)
        {
            this.num =num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.handler=handler;
        }
        public YM3812interface(int num,int baseclock,int[] mixing_level)
        {
            this.num =num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.handler=null;
        }
        public int num;
        public int baseclock;
        public int[] mixing_level;//[MAX_3812];
        public WriteYmHandlerPtr handler[];//void (*handler[MAX_3812])(int linestate);
    };
    
    public static class Y8950interface extends YM3812interface
    {
        public Y8950interface(int num, int baseclock, int[] mixing_level, WriteYmHandlerPtr[] handler) {
            super(num, baseclock, mixing_level, handler);
        }
        
        public Y8950interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler,int[] rom_region,ReadHandlerPtr[] kr,WriteHandlerPtr[] kw,ReadHandlerPtr[] pr ,WriteHandlerPtr[] pw)
        {
            super(num,baseclock,mixing_level,handler);
            this.rom_region=rom_region;
            this.keyboardread=kr;
            this.keyboardwrite=kw;
            this.portread=pr;
            this.portwrite=pw;
        }
        public Y8950interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler,int[] rom_region)
        {
            super(num,baseclock,mixing_level,handler);
            this.rom_region=rom_region;
            this.keyboardread=null;
            this.keyboardwrite=null;
            this.portread=null;
            this.portwrite=null;
        }
    	/* Y8950 */
    	int[] rom_region;//[MAX_8950]; /* delta-T ADPCM ROM region */
    	public ReadHandlerPtr keyboardread[];//int (*keyboardread[MAX_8950])(int offset);
    	public WriteHandlerPtr keyboardwrite[];//void (*keyboardwrite[MAX_8950])(int offset,int data);
    	public ReadHandlerPtr portread[];//int (*portread[MAX_8950])(int offset);
    	public WriteHandlerPtr portwrite[];//void (*portwrite[MAX_8950])(int offset,int data);
    }
    /* YM3526 */
    public static class YM3526interface extends YM3812interface
    {
        public YM3526interface(int num, int baseclock, int[] mixing_level, WriteYmHandlerPtr[] handler) {
            super(num, baseclock, mixing_level, handler);
        }

        public YM3526interface(int num, int baseclock, int[] mixing_level) {
            super(num, baseclock, mixing_level);
        }
    };
}
