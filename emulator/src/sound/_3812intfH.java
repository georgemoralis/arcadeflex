package sound;

import static mame.driverH.*;

public class _3812intfH {
    
    /*TODO*///#define MAX_3812 2
    /*TODO*///#define MAX_8950 MAX_3812
    /*TODO*///

    public static class YM3812interface
    {
        public YM3812interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler)
        {
            this.num =num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.handler=handler;
        }
        int num;
        int baseclock;
        int[] mixing_level;//[MAX_3812];
        public WriteYmHandlerPtr handler[];//void (*handler[MAX_3812])(int linestate);
    };
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
    /*TODO*////* YM3812 */
    /*TODO*///int YM3812_status_port_0_r(int offset);
    /*TODO*///void YM3812_control_port_0_w(int offset,int data);
    /*TODO*///void YM3812_write_port_0_w(int offset,int data);
    /*TODO*///int YM3812_status_port_1_r(int offset);
    /*TODO*///void YM3812_control_port_1_w(int offset,int data);
    /*TODO*///void YM3812_write_port_1_w(int offset,int data);
    /*TODO*///
    /*TODO*///int YM3812_sh_start(const struct MachineSound *msound);
    /*TODO*///void YM3812_sh_stop(void);
    /*TODO*///void YM3812_sh_reset(void);
    /*TODO*///
    /*TODO*////* YM3526 */
    public static class YM3526interface
    {
        public YM3526interface(int num,int baseclock,int[] mixing_level,WriteYmHandlerPtr []handler)
        {
            this.num =num;
            this.baseclock=baseclock;
            this.mixing_level=mixing_level;
            this.handler=handler;
        }
        int num;
        int baseclock;
        int[] mixing_level;//[MAX_3812];
        public WriteYmHandlerPtr handler[];//void (*handler[MAX_3812])(int linestate);
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
