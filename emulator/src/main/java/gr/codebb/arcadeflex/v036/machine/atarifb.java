/***************************************************************************

Atari Football machine

If you have any questions about how this driver works, don't hesitate to
ask.  - Mike Balfour (mab22@po.cwru.edu)
***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package gr.codebb.arcadeflex.v036.machine;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static arcadeflex.v036.mame.common.*;
import static arcadeflex.v036.mame.driverH.*;
import static arcadeflex.v036.mame.memoryH.*;
import static arcadeflex.v036.mame.commonH.*;
import static arcadeflex.v036.mame.inptport.*;
import static arcadeflex.v036.mame.drawgfxH.*;
import static arcadeflex.v036.vidhrdw.generic.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.input.*;
import static arcadeflex.v036.mame.inptportH.*;
import static arcadeflex.v036.mame.inputH.*;
import static gr.codebb.arcadeflex.v036.platform.libc.*;
public class atarifb
{
	
	
	static int CTRLD;
	static int sign_x_1, sign_y_1;
	static int sign_x_2, sign_y_2;
	static int sign_x_3, sign_y_3;
	static int sign_x_4, sign_y_4;
	
	public static WriteHandlerPtr atarifb_out1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		CTRLD = data;
		/* we also need to handle the whistle, hit, and kicker sound lines */
	//	if (errorlog != 0) fprintf (errorlog, "out1_w: %02x\n", data);
	} };
	
	public static WriteHandlerPtr atarifb4_out1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		CTRLD = data;
		coin_counter_w.handler(0, data & 0x80);
		/* we also need to handle the whistle, hit, and kicker sound lines */
	//	if (errorlog != 0) fprintf (errorlog, "out1_w: %02x\n", data);
	} };
	
	public static WriteHandlerPtr soccer_out1_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		/* bit 0 = whistle */
		/* bit 1 = hit */
		/* bit 2 = kicker */
		/* bit 3 = unused */
		/* bit 4 = 2/4 Player LED */
		/* bit 5-6 = trackball CTRL bits */
		/* bit 7 = Rule LED */
		CTRLD = data;
		//osd_led_w (0, (data & 0x10) >> 4);
		//osd_led_w (1, (data & 0x80) >> 7);
	} };
	
	static int counter_x1,counter_y1;
	public static ReadHandlerPtr atarifb_in0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ((CTRLD & 0x20)==0x00)
		{
			int val;
	
			val = (sign_y_2 >> 7) |
				  (sign_x_2 >> 6) |
				  (sign_y_1 >> 5) |
				  (sign_x_1 >> 4) |
				  input_port_0_r.handler(offset);
			return val;
		}
		else
		{
			
			int new_x,new_y;
	
			/* Read player 1 trackball */
			new_x = readinputport(3);
			if (new_x != counter_x1)
			{
				sign_x_1 = (new_x - counter_x1) & 0x80;
				counter_x1 = new_x;
			}
	
			new_y = readinputport(2);
			if (new_y != counter_y1)
			{
				sign_y_1 = (new_y - counter_y1) & 0x80;
				counter_y1 = new_y;
			}
	
			return (((counter_y1 & 0x0f) << 4) | (counter_x1 & 0x0f));
		}
	} };
	
	static int counter_x2,counter_y2;
	public static ReadHandlerPtr atarifb_in2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ((CTRLD & 0x20)==0x00)
		{
			return input_port_1_r.handler(offset);
		}
		else
		{
			
			int new_x,new_y;
	
			/* Read player 2 trackball */
			new_x = readinputport(5);
			if (new_x != counter_x2)
			{
				sign_x_2 = (new_x - counter_x2) & 0x80;
				counter_x2 = new_x;
			}
	
			new_y = readinputport(4);
			if (new_y != counter_y2)
			{
				sign_y_2 = (new_y - counter_y2) & 0x80;
				counter_y2 = new_y;
			}
	
			return (((counter_y2 & 0x0f) << 4) | (counter_x2 & 0x0f));
		}
	} };
	static int counter_x3,counter_y3;
        static int counter_x4,counter_y4;
	public static ReadHandlerPtr atarifb4_in0_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		/* LD1 and LD2 low, return sign bits */
		if ((CTRLD & 0x60)==0x00)
		{
			int val;
	
			val = (sign_x_4 >> 7) |
				  (sign_y_4 >> 6) |
				  (sign_x_2 >> 5) |
				  (sign_y_2 >> 4) |
				  (sign_x_3 >> 3) |
				  (sign_y_3 >> 2) |
				  (sign_x_1 >> 1) |
				  (sign_y_1 >> 0);
			return val;
		}
		else if ((CTRLD & 0x60) == 0x60)
		/* LD1 and LD2 both high, return Team 1 right player (player 1) */
		{
			
			int new_x,new_y;
	
			/* Read player 1 trackball */
			new_x = readinputport(4);
			if (new_x != counter_x3)
			{
				sign_x_1 = (new_x - counter_x3) & 0x80;
				counter_x3 = new_x;
			}
	
			new_y = readinputport(3);
			if (new_y != counter_y3)
			{
				sign_y_1 = (new_y - counter_y3) & 0x80;
				counter_y3 = new_y;
			}
	
			return (((counter_y3 & 0x0f) << 4) | (counter_x3 & 0x0f));
		}
		else if ((CTRLD & 0x60) == 0x40)
		/* LD1 high, LD2 low, return Team 1 left player (player 2) */
		{
			
			int new_x,new_y;
	
			/* Read player 2 trackball */
			new_x = readinputport(6);
			if (new_x != counter_x4)
			{
				sign_x_2 = (new_x - counter_x4) & 0x80;
				counter_x4 = new_x;
			}
	
			new_y = readinputport(5);
			if (new_y != counter_y4)
			{
				sign_y_2 = (new_y - counter_y4) & 0x80;
				counter_y4 = new_y;
			}
	
			return (((counter_y4 & 0x0f) << 4) | (counter_x4 & 0x0f));
		}
	
		else return 0;
	} };
	
	static int counter_x5,counter_y5;
        static int counter_x6,counter_y6;
	public static ReadHandlerPtr atarifb4_in2_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		if ((CTRLD & 0x40)==0x00)
		{
			return input_port_2_r.handler(offset);
		}
		else if ((CTRLD & 0x60) == 0x60)
		/* LD1 and LD2 both high, return Team 2 right player (player 3) */
		{
			
			int new_x,new_y;
	
			/* Read player 3 trackball */
			new_x = readinputport(8);
			if (new_x != counter_x5)
			{
				sign_x_3 = (new_x - counter_x5) & 0x80;
				counter_x5 = new_x;
			}
	
			new_y = readinputport(7);
			if (new_y != counter_y5)
			{
				sign_y_3 = (new_y - counter_y5) & 0x80;
				counter_y5 = new_y;
			}
	
			return (((counter_y5 & 0x0f) << 4) | (counter_x5 & 0x0f));
		}
		else if ((CTRLD & 0x60) == 0x40)
		/* LD1 high, LD2 low, return Team 2 left player (player 4) */
		{
			
			int new_x,new_y;
	
			/* Read player 4 trackball */
			new_x = readinputport(10);
			if (new_x != counter_x6)
			{
				sign_x_4 = (new_x - counter_x6) & 0x80;
				counter_x6 = new_x;
			}
	
			new_y = readinputport(9);
			if (new_y != counter_y6)
			{
				sign_y_4 = (new_y - counter_y6) & 0x80;
				counter_y6 = new_y;
			}
	
			return (((counter_y6 & 0x0f) << 4) | (counter_x6 & 0x0f));
		}
	
		else return 0;
	} };
	
	
}
