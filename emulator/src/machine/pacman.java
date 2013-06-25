
package machine;

import static mame.driverH.*;
import static arcadeflex.libc.*;
import static mame.common.*;
import static mame.commonH.*;

public class pacman {
   public static int speedcheat = 0;	/* a well known hack allows to make Pac Man run at four times */
   					/* his usual speed. When we start the emulation, we check if the */
   					/* hack can be applied, and set this flag accordingly. */
   
   public static InitMachinePtr pacman_init_machine = new InitMachinePtr() { public void handler()
   {
       UBytePtr RAM = memory_region(REGION_CPU1);

   	/* check if the loaded set of ROMs allows the Pac Man speed hack */
   	if ((RAM.read(0x180b) == 0xbe && RAM.read(0x1ffd) == 0x00) ||
   			(RAM.read(0x180b) == 0x01 && RAM.read(0x1ffd) == 0xbd))
   		speedcheat = 1;
   	else
   		speedcheat = 0;
   }};
   
   public static InterruptPtr pacman_interrupt = new InterruptPtr() { public int handler()
   {
        throw new UnsupportedOperationException("Unsupported pacman_interrupt");
   /*TODO*///	unsigned char *RAM = memory_region(REGION_CPU1);
   /*TODO*///
   /*TODO*///
   /*TODO*///	/* speed up cheat */
   /*TODO*///	if (speedcheat)
   /*TODO*///	{
   /*TODO*///		if (readinputport(4) & 1)	/* check status of the fake dip switch */
   /*TODO*///		{
   /*TODO*///			/* activate the cheat */
   /*TODO*///			RAM[0x180b] = 0x01;
   /*TODO*///			RAM[0x1ffd] = 0xbd;
   /*TODO*///		}
   /*TODO*///		else
   /*TODO*///		{
   /*TODO*///			/* remove the cheat */
   /*TODO*///			RAM[0x180b] = 0xbe;
   /*TODO*///			RAM[0x1ffd] = 0x00;
   /*TODO*///		}
   /*TODO*///	}
   /*TODO*///
   /*TODO*///	return interrupt();
   }};
   /*TODO*///   
}
