
/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;
import static mame.driverH.*;
import static mame.memoryH.*;
import static mame.commonH.*;
import static mame.inputport.*;
import static mame.drawgfxH.*;
import static vidhrdw.generic.*;
import static mame.sndintrfH.*;
import static mame.cpuintrf.*;
import static mame.common.*;
import static platform.input.*;
import static mame.inputportH.*;
import static mame.inputH.*;
import static platform.libc.*;
import static platform.libc_old.*;
import static mame.memory.*;
import static mame.mame.*;
import static platform.ptrlib.*;
public class btime
{
	
	
	public static final int BASE  =0xb000;
	
	
	static int protection_command;
	static int protection_status = 0;
	static int protection_value;
	static int protection_ret = 0;
	
	
	public static ReadHandlerPtr mmonkey_protection_r = new ReadHandlerPtr() { public int handler(int offset)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
		int ret = 0;
	
		if      (offset == 0x0000)                      ret = protection_status;
		else if (offset == 0x0e00)                      ret = protection_ret;
		else if (offset >= 0x0d00 && offset <= 0x0d02)  ret = RAM.read(BASE+offset);  /* addition result */
		else if (errorlog != null) fprintf(errorlog, "Unknown protection read.  PC=%04X  Offset=%04X\n", cpu_get_pc(), offset);
	
		return ret;
	} };
	
	
	
	public static WriteHandlerPtr mmonkey_protection_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		UBytePtr RAM = memory_region(REGION_CPU1);
	
	
		if (offset == 0)
		{
			/* protection trigger */
			if (data == 0)
			{
				int i,s1,s2,r;
	
				switch (protection_command)
				{
				case 0:	/* score addition */
	
					s1 = (    1 * (RAM.read(BASE+0x0d00) & 0x0f)) + (    10 * (RAM.read(BASE+0x0d00) >> 4)) +
						 (  100 * (RAM.read(BASE+0x0d01) & 0x0f)) + (  1000 * (RAM.read(BASE+0x0d01) >> 4)) +
						 (10000 * (RAM.read(BASE+0x0d02) & 0x0f)) + (100000 * (RAM.read(BASE+0x0d02) >> 4));
	
					s2 = (    1 * (RAM.read(BASE+0x0d03) & 0x0f)) + (    10 * (RAM.read(BASE+0x0d03) >> 4)) +
						 (  100 * (RAM.read(BASE+0x0d04) & 0x0f)) + (  1000 * (RAM.read(BASE+0x0d04) >> 4)) +
					     (10000 * (RAM.read(BASE+0x0d05) & 0x0f)) + (100000 * (RAM.read(BASE+0x0d05) >> 4));
	
					r = s1 + s2;
	
					RAM.write(BASE+0x0d00,(r % 10));        r /= 10;
					RAM.write(BASE+0x0d00,RAM.read(BASE+0x0d00) | ((r % 10) << 4));  r /= 10;
					RAM.write(BASE+0x0d01,(r % 10));        r /= 10;
					RAM.write(BASE+0x0d01, RAM.read(BASE+0x0d01) | ((r % 10) << 4));  r /= 10;
					RAM.write(BASE+0x0d02,(r % 10));        r /= 10;
					RAM.write(BASE+0x0d02, RAM.read(BASE+0x0d02) | ((r % 10) << 4));
	
					break;
	
				case 1:	/* decryption */
	
					/* Compute return value by searching the decryption table. */
					/* During the search the status should be 2, but we're done */
					/* instanteniously in emulation time */
					for (i = 0; i < 0x100; i++)
					{
						if (RAM.read(BASE+0x0f00+i) == protection_value)
						{
							protection_ret = i;
							break;
						}
					}
					break;
	
				default:
					fprintf(errorlog, "Unemulated protection command=%02X.  PC=%04X\n", protection_command, cpu_get_pc());
					break;
				}
	
				protection_status = 0;
			}
		}
		else if (offset == 0x0c00)                      protection_command = data;
		else if (offset == 0x0e00)                      protection_value = data;
		else if (offset >= 0x0f00)                      RAM.write(BASE+offset,data);   /* decrypt table */
		else if (offset >= 0x0d00 && offset <= 0x0d05)  RAM.write(BASE+offset,data);   /* source table */
		else if (errorlog != null) fprintf(errorlog, "Unknown protection write=%02X.  PC=%04X  Offset=%04X\n", data, cpu_get_pc(), offset);
	} };
}
