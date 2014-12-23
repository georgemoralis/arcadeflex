package sound;

import static mame.driverH.*;

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
        int num;
        int baseclock;
        int[] mixing_level;//[MAX_3812];
        public WriteYmHandlerPtr handler[];//void (*handler[MAX_3812])(int linestate);
    };
    
    public static class Y8950interface
    {
        public Y8950interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler,int[] rom_region,ReadHandlerPtr[] kr,WriteHandlerPtr[] kw,ReadHandlerPtr[] pr ,WriteHandlerPtr[] pw)
        {
            this.num=num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.handler=handler;
            this.rom_region=rom_region;
            this.keyboardread=kr;
            this.keyboardwrite=kw;
            this.portread=pr;
            this.portwrite=pw;
        }
        public Y8950interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler,int[] rom_region)
        {
            this.num=num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.handler=handler;
            this.rom_region=rom_region;
            this.keyboardread=null;
            this.keyboardwrite=null;
            this.portread=null;
            this.portwrite=null;
        }
        
        int num;
    	int baseclock;
    	int[] mixing_level;//[MAX_8950];
    	WriteYmHandlerPtr handler[];//void (*handler[MAX_8950])(int linestate);
    	/* Y8950 */
    	int[] rom_region;//[MAX_8950]; /* delta-T ADPCM ROM region */
    	public ReadHandlerPtr keyboardread[];//int (*keyboardread[MAX_8950])(int offset);
    	public WriteHandlerPtr keyboardwrite[];//void (*keyboardwrite[MAX_8950])(int offset,int data);
    	public ReadHandlerPtr portread[];//int (*portread[MAX_8950])(int offset);
    	public WriteHandlerPtr portwrite[];//void (*portwrite[MAX_8950])(int offset,int data);
    }
    /*TODO*///
    /*TODO*///struct Y8950interface
    /*TODO*///{
    /*TODO*///	int num;
    /*TODO*///	int baseclock;
    /*TODO*///	int mixing_level[MAX_8950];
    /*TODO*///	void (*handler[MAX_8950])(int linestate);
    /*TODO*///	/* Y8950 */
    /*TODO*///	int rom_region[MAX_8950]; /* delta-T ADPCM ROM region */
    /*TODO*///	int (*keyboardread[MAX_8950])(int offset);
    /*TODO*///	void (*keyboardwrite[MAX_8950])(int offset,int data);
    /*TODO*///	int (*portread[MAX_8950])(int offset);
    /*TODO*///	void (*portwrite[MAX_8950])(int offset,int data);
    /*TODO*///};
    /*TODO*///
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
    /*TODO*///#define YM3526_status_port_0_r YM3812_status_port_0_r
    /*TODO*///#define YM3526_control_port_0_w YM3812_control_port_0_w
    /*TODO*///#define YM3526_write_port_0_w YM3812_write_port_0_w
    /*TODO*///#define YM3526_status_port_1_r YM3812_status_port_1_r
    /*TODO*///#define YM3526_control_port_1_w YM3812_control_port_1_w
    /*TODO*///#define YM3526_write_port_1_w YM3812_write_port_1_w
    /*TODO*///int YM3526_sh_start(const struct MachineSound *msound);
    /*TODO*///#define YM3526_sh_stop YM3812_sh_stop
    /*TODO*///#define YM3526_shupdate YM3812_sh_update
    /*TODO*///
    /*TODO*////* Y8950 */
    /*TODO*///#define Y8950_status_port_0_r YM3812_status_port_0_r
    /*TODO*///#define Y8950_control_port_0_w YM3812_control_port_0_w
    /*TODO*///int Y8950_read_port_0_r(int offset);
    /*TODO*///#define Y8950_write_port_0_w YM3812_write_port_0_w
    /*TODO*///#define Y8950_status_port_1_r YM3812_status_port_1_r
    /*TODO*///#define Y8950_control_port_1_w YM3812_control_port_1_w
    /*TODO*///int Y8950_read_port_1_r(int offset);
    /*TODO*///#define Y8950_write_port_1_w YM3812_write_port_1_w
    /*TODO*///int Y8950_sh_start(const struct MachineSound *msound);
    /*TODO*///#define Y8950_sh_stop YM3812_sh_stop
    /*TODO*///#define Y8950_shupdate YM3812_sh_update    
}
