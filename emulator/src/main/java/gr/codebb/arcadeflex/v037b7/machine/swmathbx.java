/*****************************************************************
machine\swmathbx.c

This file is Copyright 1997, Steve Baines.

Release 2.0 (5 August 1997)

See drivers\starwars.c for notes

******************************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.machine;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.commonH.*;
import static gr.codebb.arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.common.*;
import static gr.codebb.arcadeflex.v036.platform.osdepend.logerror;

public class swmathbx
{
	
	public static final int NOP           = 0x00;
	public static final int LAC           = 0x01;
	public static final int READ_ACC      = 0x02;
	public static final int M_HALT        = 0x04;
	public static final int INC_BIC       = 0x08;
	public static final int CLEAR_ACC     = 0x10;
	public static final int LDC           = 0x20;
	public static final int LDB           = 0x40;
	public static final int LDA           = 0x80;
	
/*TODO*///	#define MATHDEBUG 0
	
	static int MPA; /* PROM address counter */
	static int BIC; /* Block index counter  */

	static int PRN; /* Pseudo-random number */
	
	
	/* Store decoded PROM elements */
	static int[] PROM_STR=new int[1024]; /* Storage for instruction strobe only */
	static int[] PROM_MAS=new int[1024]; /* Storage for direct address only */
	static int[] PROM_AM=new int[1024]; /* Storage for address mode select only */
	
	public static InitDriverPtr init_starwars = new InitDriverPtr() { public void handler() 
	{
		int cnt,val;
		UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1));
	
		for (cnt=0;cnt<1024;cnt++)
		{
			/* Translate PROMS into 16 bit code */
			val = 0;
			val = ( val | (( RAM.read(0x0c00+cnt)     ) & 0x000f ) ); /* Set LS nibble */
			val = ( val | (( RAM.read(0x0800+cnt)<< 4 ) & 0x00f0 ) );
			val = ( val | (( RAM.read(0x0400+cnt)<< 8 ) & 0x0f00 ) );
			val = ( val | (( RAM.read(0x0000+cnt)<<12 ) & 0xf000 ) ); /* Set MS nibble */
	
			/* Perform pre-decoding */
			PROM_STR[cnt] = (val>>8) & 0x00ff;
			PROM_MAS[cnt] =  val     & 0x007f;
			PROM_AM[cnt]  = (val>>7) & 0x0001;
	   }
	} };
	
	public static InitMachinePtr init_swmathbox = new InitMachinePtr() { public void handler() 
	{
		MPA = BIC = 0;
		PRN = 0;
	} };
        
        static short ACC, A, B, C;
	
	static void run_mbox()
	{
		
		int RAMWORD=0;
		int MA_byte;
		int tmp;
		int M_STOP=100000; /* Limit on number of instructions allowed before halt */
		int MA;
		int IP15_8, IP7, IP6_0; /* Instruction PROM values */
	
		UBytePtr RAM = new UBytePtr(memory_region(REGION_CPU1));
	
                logerror("Running Mathbox...\n");
	
		while (M_STOP>0)
		{
			IP15_8 = PROM_STR[MPA];
			IP7    = PROM_AM[MPA];
			IP6_0  = PROM_MAS[MPA];
	
/*TODO*///	#if(MATHDEBUG==1)
/*TODO*///	printf("\n(MPA:%x), Strobe: %x, IP7: %d, IP6_0:%x\n",MPA, IP15_8, IP7, IP6_0);
/*TODO*///	printf("(BIC: %x), A: %x, B: %x, C: %x, ACC: %x\n",BIC,A,B,C,ACC);
/*TODO*///	#endif
	
			/* Construct the current RAM address */
			if (IP7==0)
			{
				MA = ((IP6_0 & 3) | ( (BIC & 0x01ff) <<2) ); /* MA10-2 set to BIC8-0 */
			}
			else
			{
				MA = IP6_0;
			}
	
	
			/* Convert RAM offset to eight bit addressing (2kx8 rather than 1k*16)
			and apply base address offset */
	
			MA_byte=0x5000+(MA<<1);
	
			RAMWORD=( (RAM.read(MA_byte+1)&0x00ff) | ((RAM.read(MA_byte)&0x00ff)<<8) );
	
                        logerror("MATH ADDR: %x, CPU ADDR: %x, RAMWORD: %x\n", MA, MA_byte, RAMWORD);
	
			/*
			 * RAMWORD is the sixteen bit Math RAM value for the selected address
			 * MA_byte is the base address of this location as seen by the main CPU
			 * IP is the 16 bit instruction word from the PROM. IP7_0 have already
			 * been used in the address selection stage
			 * IP15_8 provide the instruction strobes
			 */
	
			/* 0x01 - LAC */
			if ((IP15_8 & LAC) != 0)
				ACC = (short) RAMWORD;
	
			/* 0x02 - READ_ACC */
			if ((IP15_8 & READ_ACC) != 0)
			{
				RAM.write(MA_byte+1, (ACC & 0x00ff));
				RAM.write(MA_byte  , (ACC & 0xff00) >> 8);
			}
	
			/* 0x04 - M_HALT */
			if ((IP15_8 & M_HALT) != 0)
				M_STOP = 0;
	
			/* 0x08 - INC_BIC */
			if ((IP15_8 & INC_BIC) != 0)
				BIC = (++BIC) & 0x1ff; /* Restrict to 9 bits */
	
			/* 0x10 - CLEAR_ACC */
			if ((IP15_8 & CLEAR_ACC) != 0)
				ACC = 0;
	
			/* 0x20 - LDC */
			if ((IP15_8 & LDC) != 0)
			{
				C = (short) RAMWORD;
				/* TODO: this next line is accurate to the schematics, but doesn't seem to work right */
				/* ACC=ACC+(  ( (long)((A-B)*C) )>>14  ); */
				/* round the result - this fixes bad trench vectors in Star Wars */
				ACC = (short) (ACC+(  ( (((long)((A-B)*C) )>>13)+1)>>1  ));
			}
	
			/* 0x40 - LDB */
			if ((IP15_8 & LDB) != 0)
				B = (short) RAMWORD;
	
			/* 0x80 - LDA */
			if ((IP15_8 & LDA) != 0)
				A = (short) RAMWORD;
	
			/*
			 * Now update the PROM address counter
			 * Done like this because the top two bits are not part of the counter
			 * This means that each of the four pages should wrap around rather than
			 * leaking from one to another.  It may not matter, but I've put it in anyway
			 */
			tmp = MPA;
			tmp ++;
			MPA = (MPA&0x0300)|(tmp&0x00ff); /* New MPA value */
	
			M_STOP --; /* Decrease count */
		}
	}
	
	/******************************************************/
	/****** Other Starwars Math related handlers **********/
	
	public static ReadHandlerPtr prng_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
/*TODO*///	#if(MATHDEBUG==1)
/*TODO*///	printf("prng\n");
/*TODO*///	#endif
		PRN = (int)((PRN+0x2364)^2); /* This is a total bodge for now, but it works!*/
		return (PRN & 0xff);	/* ASG 971002 -- limit to a byte; the 6809 code cares */
	} };
	
	/********************************************************/
	public static WriteHandlerPtr prngclr_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
/*TODO*///	#if(MATHDEBUG==1)
/*TODO*///	printf("prngclr\n");
/*TODO*///	#endif
		PRN=0;
	} };
	
	/********************************************************/
	public static WriteHandlerPtr mw0_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
/*TODO*///	#if(MATHDEBUG==1)
/*TODO*///	printf("mw0: %x\n",data);
/*TODO*///	#endif
		MPA=(data<<2); /* Set starting PROM address */
		run_mbox();   /* and run the Mathbox */
	} };
	
	/********************************************************/
	/* BIC - write high bit */
	public static WriteHandlerPtr mw1_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
/*TODO*///	#if(MATHDEBUG==1)
/*TODO*///	printf("mw1: %x\n",data);
/*TODO*///	#endif
		BIC = (BIC & 0x00ff) | ((data & 0x01)<<8);
	} };
	
	/********************************************************/
	/* BIC - write low byte */
	public static WriteHandlerPtr mw2_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
/*TODO*///	#if(MATHDEBUG==1)
/*TODO*///	printf("mw2: %x\n",data);
/*TODO*///	#endif
		BIC = (BIC & 0x0100) | data;
	} };
	
	/*********************************************************/
	
	/*******************************************************/
	/*   Divider handlers                                  */
	/*******************************************************/
	
	static int RESULT;	/* ASG 971002 */
	static int DIVISOR, DIVIDEND;
	
	/********************************************************/
	public static ReadHandlerPtr reh_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return((RESULT & 0xff00)>>8 );
	} };
	/********************************************************/
	public static ReadHandlerPtr rel_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		return(RESULT & 0x00ff);
	} };
	/********************************************************/
	
	
	public static WriteHandlerPtr swmathbx_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		data &= 0xff;	/* ASG 971002 -- make sure we only get bytes here */
		switch(offset)
		{
	      case 0:
	        mw0_w.handler(0,data);
	        break;
	
	      case 1:
	        mw1_w.handler(0,data);
	        break;
	
	      case 2:
	        mw2_w.handler(0,data);
	        break;
	
	      case 4: /* dvsrh */
	        DIVISOR = ((DIVISOR & 0x00ff) | (data<<8));
			break;
	
	      case 5: /* dvsrl */
	
	        /* Note: Divide is triggered by write to low byte.  This is */
	        /*       dependant on the proper 16 bit write order in the  */
	        /*       6809 emulation (high bytes, then low byte).        */
	        /*       If the Tie fighters look corrupt, he byte order of */
	        /*       the 16 bit writes in the 6809 are backwards        */
	
	        DIVISOR = ((DIVISOR & 0xff00) | (data));
	
			if (DIVIDEND >= 2*DIVISOR)
				RESULT = 0x7fff;
			else
				RESULT = (int)((((long)DIVIDEND<<14)/(long)DIVISOR));
	        break;
	
	      case 6: /* dvddh */
	        DIVIDEND = ((DIVIDEND & 0x00ff) | (data<<8));
	        break;
	
	      case 7: /* dvddl */
	        DIVIDEND = ((DIVIDEND & 0xff00) | (data));
	        break;
	
	      default:
	        break;
		}
	} };
}
