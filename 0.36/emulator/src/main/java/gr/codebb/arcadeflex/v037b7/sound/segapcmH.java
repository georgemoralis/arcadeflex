/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.sound;

import static gr.codebb.arcadeflex.common.PtrLib.*;

public class segapcmH {
/*TODO*///	/*********************************************************/
/*TODO*///	/*    SEGA 8bit PCM                                      */
/*TODO*///	/*********************************************************/
/*TODO*///
/*TODO*///	#ifndef __SEGAPCM_H__
/*TODO*///	#define __SEGAPCM_H__


	/************************************************/
        public static final int BANK_256    = (11);
        public static final int BANK_512    = (12);
/*TODO*///	#define   BANK_12M    (13)
	public static final int BANK_MASK7  = (0x70<<16);
/*TODO*///	#define   BANK_MASKF    (0xf0<<16)
/*TODO*///	#define   BANK_MASKF8   (0xf8<<16)
/*TODO*///
        public static final int SEGAPCM_MAX = (16);
/*TODO*///	enum
/*TODO*///	{
        public static final int L_PAN = 0;
        public static final int R_PAN = 1;
        public static final int LR_PAN = 2;
/*TODO*///	};


	public static class SEGAPCM
	{
		UBytePtr  writeram = new UBytePtr(0x1000);

                char[][]  gain = new char[SEGAPCM_MAX][LR_PAN];
		char[]  addr_l = new char[SEGAPCM_MAX];
                char[]  addr_h = new char[SEGAPCM_MAX];
		char[]  bank = new char[SEGAPCM_MAX];
                char[]  end_h = new char[SEGAPCM_MAX];
		char[]  delta_t = new char[SEGAPCM_MAX];

		int[][] vol = new int[SEGAPCM_MAX][LR_PAN];

		int[]   add_addr = new int[SEGAPCM_MAX];
		int[]   step = new int[SEGAPCM_MAX];
		int[]   flag = new int[SEGAPCM_MAX];
		int   bankshift;
		int   bankmask;

		int[] pcmd = new int[SEGAPCM_MAX];
		int[] pcma = new int[SEGAPCM_MAX];
	};

	public static class SEGAPCMinterface
	{
		int  mode;
		int  bank;
		int  region;
		int  volume;
                
                public SEGAPCMinterface(int  mode, int  bank, int  region, int  volume) {
                    this.mode=mode;
                    this.bank=bank;
                    this.region=region;
                    this.volume=volume;
                }
	};

/*TODO*///	enum SEGAPCM_samplerate
/*TODO*///	{
        public static final int SEGAPCM_SAMPLE15K = 0;
        public static final int SEGAPCM_SAMPLE32K = 1;
/*TODO*///	};
/*TODO*///
/*TODO*///	/**************** prottype ****************/
/*TODO*///	int SEGAPCM_sh_start( const struct MachineSound *msound );
/*TODO*///
/*TODO*///	int SEGAPCMInit( const struct MachineSound *msound, int banksize, int mode, UBytePtr inpcm, int volume );
/*TODO*///
/*TODO*///	/************************************************/
/*TODO*///	#endif
/*TODO*///	/**************** end of file ****************/
    
}
