/***************************************************************************

  2612intf.c

  The YM2612 emulator supports up to 2 chips.
  Each chip has the following connections:
  - Status Read / Control Write A
  - Port Read / Data Write A
  - Control Write B
  - Data Write B

***************************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.sound.streams.*;
import static arcadeflex.v036.mame.timer.*;
import static gr.codebb.arcadeflex.v037b7.sound._2612intfH.*;
import static gr.codebb.arcadeflex.v037b7.sound.fm.*;
import static gr.codebb.arcadeflex.v037b7.sound.fmH.*;
import static common.libc.cstdio.sprintf;

public class _2612intf extends snd_interface
{
    
    public _2612intf() {
        this.name = "YM-2612";
        this.sound_num = SOUND_YM3438;
        
        for (int i = 0; i < MAX_2612; i++) {
            Timer[i] = new Object[2];
        }
    }
	
	
/*TODO*///	#ifdef BUILD_YM2612
	
	/* use FM.C with stream system */
	
	static int[] stream = new int[MAX_2612];

	/* Global Interface holder */
	static YM2612interface intf;
	
	static Object[][] Timer=new Object[MAX_2612][2];
	static double[][] lastfired=new double[MAX_2612][2];

	/*------------------------- TM2612 -------------------------------*/
	/* IRQ Handler */
	static FM_IRQHANDLER_Ptr IRQHandler = new FM_IRQHANDLER_Ptr() {
            @Override
            public void handler(int n, int irq) {
                if (intf.YM2203_handler == null) {
                    return;
                }
                if (intf.YM2203_handler[n] != null) {
                    intf.YM2203_handler[n].handler(irq);
                }
            }
        };
        
	
	/* Timer overflow callback from timer.c */
	public static TimerCallbackHandlerPtr TimerCallbackHandlerPtr_2612 = new TimerCallbackHandlerPtr() { public void handler(int param) 
	{
		int n=param&0x7f;
		int c=param>>7;
	
	//	logerror("2612 TimerOver %d\n",c);
		Timer[n][c] = null;
		lastfired[n][c] = timer_get_time();
		YM2612TimerOver(n,c);
	} };
	
	/* TimerHandler from fm.c */
	static FM_TIMERHANDLER_Ptr TimerHandler = new FM_TIMERHANDLER_Ptr() {
            @Override
            public void handler(int n, int c, double count, double stepTime) {
                		if( count == 0 )
		{	/* Reset FM Timer */
			if( Timer[n][c] != null )
			{
	//			logerror("2612 TimerReset %d\n",c);
		 		timer_remove (Timer[n][c]);
				Timer[n][c] = null;
			}
		}
		else
		{	/* Start FM Timer */
			double timeSec = (double)count * stepTime;
	
			if( Timer[n][c] == null )
			{
				double slack;
	
				slack = timer_get_time() - lastfired[n][c];
				/* hackish way to make bstars intro sync without */
				/* breaking sonicwi2 command 0x35 */
				if (slack < 0.000050) slack = 0;
	
	//			logerror("2612 TimerSet %d %f slack %f\n",c,timeSec,slack);
	
				Timer[n][c] = timer_set (timeSec - slack, (c<<7)|n, TimerCallbackHandlerPtr_2612 );
			}
		}
            }
        };
	
	static void FMTimerInit()
	{
		int i;
	
		for( i = 0 ; i < MAX_2612 ; i++ )
			Timer[i][0] = Timer[i][1] = null;
	}
	
	/* update request from fm.c */
	public static void YM2612UpdateRequest(int chip)
	{
		stream_update(stream[chip],100);
	}
	
	/***********************************************************/
	/*    YM2612 (fm4ch type)                                  */
	/***********************************************************/
	public static ShStartHandlerPtr YM2612_sh_start = new ShStartHandlerPtr() { public int handler(MachineSound msound) 
	{
		int i,j;
		int rate = Machine.sample_rate;
		String[] buf=new String[YM2612_NUMBUF];
		String[] name=new String[YM2612_NUMBUF];
	
		intf = (YM2612interface) msound.sound_interface;
		if( intf.num > MAX_2612 ) return 1;
	
		/* FM init */
		/* Timer Handler set */
		FMTimerInit();
		/* stream system initialize */
		for (i = 0;i < intf.num;i++)
		{
		
                        int[] vol = new int[YM2612_NUMBUF];
			/* stream setup */
			for (j = 0 ; j < YM2612_NUMBUF ; j++)
			{
				vol[j] = intf.mixing_level[i];
				name[j] = buf[j];
				buf[j]=sprintf("YM2612(%s) #%d",j < 2 ? "FM" : "ADPCM",i);
			}
			stream[i] = stream_init_multi(YM2612_NUMBUF,
				name,vol,rate,
				i,YM2612UpdateOne);
		}
	
		/**** initialize YM2612 ****/
		if (YM2612Init(intf.num,intf.baseclock,rate,TimerHandler,IRQHandler) == 0)
		  return 0;
		/* error */
		return 1;
	} };
	
	/************************************************/
	/* Sound Hardware Stop							*/
	/************************************************/
	public static ShStopHandlerPtr YM2612_sh_stop = new ShStopHandlerPtr() { public void handler() 
	{
	  YM2612Shutdown();
	} };
	
	/* reset */
	static void YM2612_sh_reset()
	{
		int i;
	
		for (i = 0;i < intf.num;i++)
			YM2612ResetChip(i);
	}
	
	/************************************************/
	/* Status Read for YM2612 - Chip 0				*/
	/************************************************/
	public static ReadHandlerPtr YM2612_status_port_0_A_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	  return YM2612Read(0,0);
	} };
	
	public static ReadHandlerPtr YM2612_status_port_0_B_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
	  return YM2612Read(0,2);
	} };
	
	/************************************************/
	/* Status Read for YM2612 - Chip 1				*/
	/************************************************/
	public static ReadHandlerPtr YM2612_status_port_1_A_r  = new ReadHandlerPtr() { public int handler(int offset) {
	  return YM2612Read(1,0);
	} };
	
	public static ReadHandlerPtr YM2612_status_port_1_B_r  = new ReadHandlerPtr() { public int handler(int offset) {
	  return YM2612Read(1,2);
	} };
	
	/************************************************/
	/* Port Read for YM2612 - Chip 0				*/
	/************************************************/
	public static ReadHandlerPtr YM2612_read_port_0_r  = new ReadHandlerPtr() { public int handler(int offset){
	  return YM2612Read(0,1);
	} };
	
	/************************************************/
	/* Port Read for YM2612 - Chip 1				*/
	/************************************************/
	public static ReadHandlerPtr YM2612_read_port_1_r  = new ReadHandlerPtr() { public int handler(int offset){
	  return YM2612Read(1,1);
	} };
	
	/************************************************/
	/* Control Write for YM2612 - Chip 0			*/
	/* Consists of 2 addresses						*/
	/************************************************/
	public static WriteHandlerPtr YM2612_control_port_0_A_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	  YM2612Write(0,0,data);
	} };
	
	public static WriteHandlerPtr YM2612_control_port_0_B_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	  YM2612Write(0,2,data);
	} };
	
	/************************************************/
	/* Control Write for YM2612 - Chip 1			*/
	/* Consists of 2 addresses						*/
	/************************************************/
	public static WriteHandlerPtr YM2612_control_port_1_A_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	  YM2612Write(1,0,data);
	} };
	
	public static WriteHandlerPtr YM2612_control_port_1_B_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	  YM2612Write(1,2,data);
	} };
	
	/************************************************/
	/* Data Write for YM2612 - Chip 0				*/
	/* Consists of 2 addresses						*/
	/************************************************/
	public static WriteHandlerPtr YM2612_data_port_0_A_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	  YM2612Write(0,1,data);
	} };
	
	public static WriteHandlerPtr YM2612_data_port_0_B_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	  YM2612Write(0,3,data);
	} };
	
	/************************************************/
	/* Data Write for YM2612 - Chip 1				*/
	/* Consists of 2 addresses						*/
	/************************************************/
	public static WriteHandlerPtr YM2612_data_port_1_A_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	  YM2612Write(1,1,data);
	} };
	public static WriteHandlerPtr YM2612_data_port_1_B_w = new WriteHandlerPtr() {public void handler(int offset, int data){
	  YM2612Write(1,3,data);
	} };
	
	/**************** end of file ****************/
	
/*TODO*///	#endif

    @Override
    public int chips_num(MachineSound msound) {
        return ((YM2612interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((YM2612interface) msound.sound_interface).baseclock;
    }

    @Override
    public int start(MachineSound msound) {
        return YM2612_sh_start.handler(msound);
    }

    @Override
    public void stop() {
/*TODO*///        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void update() {
/*TODO*///        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void reset() {
        YM2612_sh_reset();
    }
}
