//TODO
package sound;

/**
 *
 * @author shadow
 */
import static arcadeflex.libc_old.*;
public class streams {
    
    public static abstract interface StreamInitPtr { public abstract void handler(int param,CharPtr buffer,int length); }
    
    public static void set_RC_filter(int channel,int R1,int R2,int R3,int C)
    {
        
    }
    public static int stream_init(String name,int default_mixing_level,
		int sample_rate,
		int param,StreamInitPtr callback)
    {
        return 0;//todo
    }
    public static void stream_update(int channel,int min_interval)
    {
        //todo
    }
}
