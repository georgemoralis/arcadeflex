/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package sndhrdw;

public class dkong
{
	
	static int walk = 0; /* used to determine if dkongjr is walking or climbing? */
	
	public static WriteHandlerPtr dkong_sh_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		if (data != 0)
			cpu_cause_interrupt(1,I8039_EXT_INT);
	} };
	
	
	public static WriteHandlerPtr dkong_sh1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int state[8];
	
		if (state[offset] != data)
		{
			if (data != 0)
				sample_start (offset, offset, 0);
	
			state[offset] = data;
		}
	} };
	
	
	
	
	public static WriteHandlerPtr dkongjr_sh_death_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int death = 0;
	
		if (death != data)
		{
			if (data != 0)
				sample_stop (7);
				sample_start (6, 4, 0);
	
	
			death = data;
		}
	} };
	
	public static WriteHandlerPtr dkongjr_sh_drop_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int drop = 0;
	
		if (drop != data)
		{
	
	
			if (data != 0)
				sample_start (7, 5, 0);
	
			drop = data;
		}
	} };
	
	public static WriteHandlerPtr dkongjr_sh_roar_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int roar = 0;
		if (roar != data)
		{
			if (data != 0)
				sample_start (7,2,0);
			roar = data;
		}
	} };
	
	public static WriteHandlerPtr dkongjr_sh_jump_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int jump = 0;
	
		if (jump != data)
		{
			if (data != 0)
				sample_start (6,0,0);
	
	
				jump = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr dkongjr_sh_land_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int land = 0;
	
		if (land != data)
		{
			if (data != 0)
				sample_stop (7) ;
				sample_start (4,1,0);
	
				land = data;
		}
	} };
	
	
	
	public static WriteHandlerPtr dkongjr_sh_climb_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int climb = 0;
	
		if (climb != data)
		{
			if (data && walk == 0)
			{
				sample_start (3,3,0);
			}
			else if (data && walk == 1)
			{
				sample_start (3,6,0);
			}
				climb = data;
		}
	} };
	
	
	public static WriteHandlerPtr dkongjr_sh_snapjaw_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		static int snapjaw = 0;
	
		if (snapjaw != data)
		{
			if (data != 0)
				sample_stop (7) ;
				sample_start (4,7,0);
	
			snapjaw = data;
		}
	} };
	
	
	public static WriteHandlerPtr dkongjr_sh_walk_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
	
	
		if (walk != data )
		{
				walk = data;
		}
	} };
}
