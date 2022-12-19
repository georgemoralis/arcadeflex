/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sound;

public class _2203intf
{
	
	
	static int stream[MAX_2203];
	
	static const struct YM2203interface *intf;
	
	static void *Timer[MAX_2203][2];
	
	/* IRQ Handler */
	static void IRQHandler(int n,int irq)
	{
		if(intf.handler[n]) intf.handler[n](irq);
	}
	
	/* Timer overflow callback from timer.c */
	public static timer_callback timer_callback_2203 = new timer_callback() { public void handler(int param) 
	{
		int n=param&0x7f;
		int c=param>>7;
	
		Timer[n][c] = 0;
		YM2203TimerOver(n,c);
	} };
	
	/* update request from fm.c */
	void YM2203UpdateRequest(int chip)
	{
		stream_update(stream[chip],0);
	}
	
	#if 0
	/* update callback from stream.c */
	static void YM2203UpdateCallback(int chip,void *buffer,int length)
	{
		YM2203UpdateOne(chip,buffer,length);
	}
	#endif
	
	/* TimerHandler from fm.c */
	static void TimerHandler(int n,int c,int count,double stepTime)
	{
		if( count == 0 )
		{	/* Reset FM Timer */
			if( Timer[n][c] )
			{
		 		timer_remove (Timer[n][c]);
				Timer[n][c] = 0;
			}
		}
		else
		{	/* Start FM Timer */
			double timeSec = (double)count * stepTime;
	
			if( Timer[n][c] == 0 )
			{
				Timer[n][c] = timer_set (timeSec , (c<<7)|n, timer_callback_2203 );
			}
		}
	}
	
	static void FMTimerInit( void )
	{
		int i;
	
		for( i = 0 ; i < MAX_2203 ; i++ )
		{
			Timer[i][0] = Timer[i][1] = 0;
		}
	}
	
	public static ShStartPtr YM2203_sh_start = new ShStartPtr() { public int handler(MachineSound msound) 
	{
		int i;
	
		if (AY8910_sh_start(msound)) return 1;
	
		intf = msound.sound_interface;
	
		/* Timer Handler set */
		FMTimerInit();
		/* stream system initialize */
		for (i = 0;i < intf.num;i++)
		{
			int volume;
			char name[20];
			sprintf(name,"%s #%d FM",sound_name(msound),i);
			volume = intf.mixing_level[i]>>16; /* high 16 bit */
			stream[i] = stream_init(name,volume,Machine.sample_rate,i,YM2203UpdateOne/*YM2203UpdateCallback*/);
		}
		/* Initialize FM emurator */
		if (YM2203Init(intf.num,intf.baseclock,Machine.sample_rate,TimerHandler,IRQHandler) == 0)
		{
			/* Ready */
			return 0;
		}
		/* error */
		/* stream close */
		return 1;
	} };
	
	public static ShStopPtr YM2203_sh_stop = new ShStopPtr() { public void handler() 
	{
		YM2203Shutdown();
	} };
	
	void YM2203_sh_reset(void)
	{
		int i;
	
		for (i = 0;i < intf.num;i++)
			YM2203ResetChip(i);
	}
	
	
	
	public static ReadHandlerPtr YM2203_status_port_0_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(0,0); } };
	public static ReadHandlerPtr YM2203_status_port_1_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(1,0); } };
	public static ReadHandlerPtr YM2203_status_port_2_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(2,0); } };
	public static ReadHandlerPtr YM2203_status_port_3_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(3,0); } };
	public static ReadHandlerPtr YM2203_status_port_4_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(4,0); } };
	
	public static ReadHandlerPtr YM2203_read_port_0_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(0,1); } };
	public static ReadHandlerPtr YM2203_read_port_1_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(1,1); } };
	public static ReadHandlerPtr YM2203_read_port_2_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(2,1); } };
	public static ReadHandlerPtr YM2203_read_port_3_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(3,1); } };
	public static ReadHandlerPtr YM2203_read_port_4_r  = new ReadHandlerPtr() { public int handler(int offset) { return YM2203Read(4,1); } };
	
	public static WriteHandlerPtr YM2203_control_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(0,0,data);
	} };
	public static WriteHandlerPtr YM2203_control_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(1,0,data);
	} };
	public static WriteHandlerPtr YM2203_control_port_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(2,0,data);
	} };
	public static WriteHandlerPtr YM2203_control_port_3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(3,0,data);
	} };
	public static WriteHandlerPtr YM2203_control_port_4_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(4,0,data);
	} };
	
	public static WriteHandlerPtr YM2203_write_port_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(0,1,data);
	} };
	public static WriteHandlerPtr YM2203_write_port_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(1,1,data);
	} };
	public static WriteHandlerPtr YM2203_write_port_2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(2,1,data);
	} };
	public static WriteHandlerPtr YM2203_write_port_3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(3,1,data);
	} };
	public static WriteHandlerPtr YM2203_write_port_4_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		YM2203Write(4,1,data);
	} };
}
