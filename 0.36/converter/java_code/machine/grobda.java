/***************************************************************************

  machine.c

  Functions to emulate general aspects of the machine (RAM, ROM, interrupts,
  I/O ports)

***************************************************************************/

/*
 * ported to v0.36
 * using automatic conversion tool v0.10
 *
 *
 *
 */ 
package machine;

public class grobda
{
	
	unsigned char *grobda_snd_sharedram;
	unsigned char *grobda_spriteram;
	unsigned char *grobda_customio_1,*grobda_customio_2;
	static int int_enable_1, int_enable_2;
	static int credits, coincounter1, coincounter2;
	
	void grobda_init_machine( void )
	{
	    int_enable_1 = int_enable_2 = 1;
	    credits = coincounter1 = coincounter2 = 0;
		cpu_set_halt_line(1, CLEAR_LINE);
	}
	
	/* memory handlers */
	public static ReadHandlerPtr grobda_spriteram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return grobda_spriteram[offset];
	} };
	
	public static WriteHandlerPtr grobda_spriteram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    grobda_spriteram[offset] = data;
	} };
	
	public static ReadHandlerPtr grobda_snd_sharedram_r = new ReadHandlerPtr() { public int handler(int offset)
	{
	    return grobda_snd_sharedram[offset];
	} };
	
	public static WriteHandlerPtr grobda_snd_sharedram_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    grobda_snd_sharedram[offset] = data;
	} };
	
	/* irq control functions */
	public static WriteHandlerPtr grobda_interrupt_ctrl_1_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    int_enable_1 = offset;
	} };
	
	public static WriteHandlerPtr grobda_interrupt_ctrl_2_w = new WriteHandlerPtr() { public void handler(int offset, int data){
	    int_enable_2 = offset;
	} };
	
	int grobda_interrupt_1( void ) {
		if (int_enable_1 != 0)
	        return interrupt();
	    else
	        return ignore_interrupt();
	}
	
	int grobda_interrupt_2( void ){
	    if (int_enable_2 != 0)
	        return interrupt();
	    else
	        return ignore_interrupt();
	}
	
	public static WriteHandlerPtr grobda_cpu2_enable_w = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
		cpu_set_halt_line(1, offset ? CLEAR_LINE : ASSERT_LINE);
	} };
	
	/************************************************************************************
	*																					*
	*           Grobda custom I/O chips (preliminary)									*
	*																					*
	************************************************************************************/
	
	public static WriteHandlerPtr grobda_customio_w_1 = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    grobda_customio_1[offset] = data;
	} };
	
	public static WriteHandlerPtr grobda_customio_w_2 = new WriteHandlerPtr() { public void handler(int offset, int data)
	{
	    grobda_customio_2[offset] = data;
	} };
	
	static int credmoned [] = { 1, 1, 1, 1, 2, 2, 3, 4 };
	static int monedcred [] = { 3, 4, 2, 1, 1, 3, 1, 1 };
	
	public static ReadHandlerPtr grobda_customio_r_1 = new ReadHandlerPtr() { public int handler(int offset)
	{
	    int mode, val, temp1, temp2;
	
	    mode = grobda_customio_1[8];
	    if (mode == 3)	/* normal mode */
	    {
	        switch (offset)
	        {
	            case 0:     /* Coin slots */
	            {
	                static int lastval;
	
	                val = (readinputport( 2 ) >> 4) & 0x03;
	                temp1 = readinputport( 0 ) & 0x07;
	                temp2 = (readinputport( 0 ) >> 5) & 0x07;
	
	                /* bit 0 is a trigger for the coin slot 1 */
	                if ((val & 1) && ((val ^ lastval) & 1))
	                {
	                    coincounter1++;
	                    if (coincounter1 >= credmoned[temp1])
	                    {
	                        credits += monedcred [temp1];
	                        coincounter1 -= credmoned [temp1];
	                    }
	                }
	                /* bit 1 is a trigger for the coin slot 2 */
	                if ((val & 2) && ((val ^ lastval) & 2))
	                {
	                    coincounter2++;
	                    if (coincounter2 >= credmoned[temp2])
	                    {
	                        credits += monedcred [temp2];
	                        coincounter2 -= credmoned [temp2];
	                    }
	                }
	
	                if (credits > 99)
	                    credits = 99;
	
	                return lastval = val;
	            }
	                break;
	            case 1:
	            {
	                static int lastval;
	
	                val = readinputport( 2 ) & 0x03;
	                temp1 = readinputport( 0 ) & 0x07;
	                temp2 = (readinputport( 0 ) >> 5) & 0x07;
	
	                /* bit 0 is a trigger for the 1 player start */
	                if ((val & 1) && ((val ^ lastval) & 1))
	                {
	                    if (credits > 0)
	                        credits--;
	                    else
	                        val &= ~1;   /* otherwise you can start with no credits! */
	                }
	                /* bit 1 is a trigger for the 2 player start */
	                if ((val & 2) && ((val ^ lastval) & 2))
	                {
	                    if (credits >= 2)
	                        credits -= 2;
	                    else
	                        val &= ~2;   /* otherwise you can start with no credits! */
	                }
	                return lastval = val;
	            }
	                break;
	            case 2:
	                return (credits / 10);      /* high BCD of credits */
	                break;
	            case 3:
	                return (credits % 10);      /* low BCD of credits */
	                break;
	            case 4:
	                return (readinputport( 3 ) & 0x0f);   /* 1P controls */
	                break;
	            case 5:
	                return (readinputport( 4 ) & 0x03);   /* 1P button 1 */
	                break;
	            default:
	                return 0x0f;
	        }
	    }
	    else if (mode == 5)  /* IO tests chip 1 */
	    {
	        switch (offset)
	        {
	            case 2:
	                return 0x0f;
	                break;
				case 6:
					return 0x0c;
	            default:
	                return grobda_customio_1[offset];
	        }
	    }
	    else if (mode == 1)	/* test mode controls */
	    {
	        switch (offset)
	        {
				case 4:
					return (readinputport( 2 ) & 0x03);	/* start 1 & 2 */
					break;
				case 5:
					return (readinputport( 3 ) &0x0f);	/* 1P controls */
					break;
				case 7:
					return (readinputport( 4 ) & 0x03);	/* 1P button 1 */
					break;
				default:
					return grobda_customio_1[offset];
	        }
	    }
	    return grobda_customio_1[offset];
	} };
	
	public static ReadHandlerPtr grobda_customio_r_2 = new ReadHandlerPtr() { public int handler(int offset)
	{
	    int val, mode;
	
	    mode = grobda_customio_2[8];
	    if (mode == 8)  /* IO tests chip 2 */
	    {
	        switch (offset)
	        {
	            case 0:
	                return 0x06;
	                break;
	            case 1:
	                return 0x09;
	                break;
	            default:
	                return grobda_customio_2[offset];
	        }
	    }
	    else if (mode == 9)
	    {
	        switch (offset)
	        {
	            case 0:
	                val = (readinputport( 1 ) & 0x03);			/* lives */
					val |= (readinputport( 0 ) & 0x18) >> 1;	/* rank */
	                break;
	            case 1:
	                val = (readinputport( 2 ) >> 6) & 0x01;		/* attrack mode sound */
					val |= (readinputport( 1 ) >> 1) & 0x02;	/* select mode */
					val |= (readinputport( 1 ) & 0xc0) >> 4;	/* bonus life */
	                break;
				case 2:
	                val = (readinputport( 0 ) & 0x07) << 1;		/* coinage A */
	                break;
				case 4:
	                val = (readinputport( 0 ) & 0xe0) >> 5;		/* coinage B */
	                break;
	            case 6:
	                val = readinputport( 1 ) & 0x08;			/* test mode */
					val |= (readinputport( 4 ) & 0x20) >> 5;	/* 1P button 2 */
	                break;
	            default:
	                val = 0x0f;
	        }
	        return val;
	    }
		else
			return grobda_customio_2[offset];
	} };
}
