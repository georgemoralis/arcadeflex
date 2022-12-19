
/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package machine;

public class _8254pit
{
	//
	
	
	
	void pit8254_init (pit8254_interface *intf)
	{
	
	}
	
	void pit8254_w (int which, int offset, int data)
	{
		switch (offset)
		{
			case 0:
				logerror("PIT8254#%d write %d to timer1\n",which, data);
				break;
			case 1:
				logerror("PIT8254#%d write %d to timer2\n",which, data);
				break;
			case 2:
				logerror("PIT8254#%d write %d to timer3\n",which, data);
				break;
			case 3:
				{
					int sc=(data>>6)&3;
					int rw=(data>>4)&3;
					int mode=(data>>1)&0x07;
					int bcd=data&0x01;
					logerror("PIT8254#%d write %02x to control : ", which, data);
					logerror("*** SC=%d RW=%d MODE=%d BCD=%d\n",sc, rw, mode, bcd);
				}
	
				break;
		}
	}
	
	int pit8254_r (int which, int offset)
	{
		switch (offset)
		{
			case 0:
				logerror("PIT8254#%d read from timer1\n", which);
	
				break;
			case 1:
				logerror("PIT8254#%d read from timer2\n", which);
				break;
			case 2:
				logerror("PIT8254#%d read from timer3\n", which);
				break;
			case 3:
				logerror("PIT8254#%d read from control\n", which);
				break;
		}
	
		return 0;
	}
	
	/*
	Port handler wrappers.
	*/
	
	public static WriteHandlerPtr pit8254_0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(0, offset, data);
	} };
	
	public static WriteHandlerPtr pit8254_0_counter1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(0, 0, data);
	} };
	
	public static WriteHandlerPtr pit8254_0_counter2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(0, 1, data);
	} };
	
	public static WriteHandlerPtr pit8254_0_counter3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(0, 2, data);
	} };
	
	public static WriteHandlerPtr pit8254_0_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(0, 3, data);
	} };
	
	public static WriteHandlerPtr pit8254_1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(1, 0, data);
	} };
	
	public static WriteHandlerPtr pit8254_1_counter1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(1, 0, data);
	} };
	
	public static WriteHandlerPtr pit8254_1_counter2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(1, 1, data);
	} };
	
	public static WriteHandlerPtr pit8254_1_counter3_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(1, 2, data);
	} };
	
	public static WriteHandlerPtr pit8254_1_control_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		pit8254_w(1, 3, data);
	} };
	
	
	public static ReadHandlerPtr pit8254_0_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(0, offset);
	} };
	
	public static ReadHandlerPtr pit8254_0_counter1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(0, 0);
	} };
	
	public static ReadHandlerPtr pit8254_0_counter2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(0, 1);
	} };
	
	public static ReadHandlerPtr pit8254_0_counter3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(0, 2);
	} };
	
	public static ReadHandlerPtr pit8254_0_control_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(0, 3);
	} };
	
	public static ReadHandlerPtr pit8254_1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(1, offset);
	} };
	
	public static ReadHandlerPtr pit8254_1_counter1_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(1, 0);
	} };
	
	public static ReadHandlerPtr pit8254_1_counter2_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(1, 1);
	} };
	
	public static ReadHandlerPtr pit8254_1_counter3_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(1, 2);
	} };
	public static ReadHandlerPtr pit8254_1_control_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return pit8254_r(1, 3);
	} };
}
