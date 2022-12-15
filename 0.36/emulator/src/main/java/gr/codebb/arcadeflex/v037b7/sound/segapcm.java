/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 

package gr.codebb.arcadeflex.v037b7.sound;
/*********************************************************/
/*    SEGA 16ch 8bit PCM                                 */
/*********************************************************/
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import gr.codebb.arcadeflex.common.PtrLib.ShortPtr;
import gr.codebb.arcadeflex.common.PtrLib.UBytePtr;
import static common.libc.cstring.memset;
import static arcadeflex.v036.mame.common.*;
import arcadeflex.v036.mame.sndintrf.snd_interface;
import static arcadeflex.v036.mame.sndintrf.sound_name;
import static arcadeflex.v036.mame.sndintrfH.*;
import static arcadeflex.v036.mame.mame.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static arcadeflex.v036.sound.streams.*;
import static gr.codebb.arcadeflex.v037b7.sound.segapcmH.*;

public class segapcm  extends snd_interface
{
    
    public segapcm() {
        this.name = "Sega PCM";
        this.sound_num = SOUND_SEGAPCM;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 1;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
        return SEGAPCM_sh_start(msound);
    }

    @Override
    public void stop() {
        SEGAPCM_sh_stop();
    }

    @Override
    public void update() {
        SEGAPCM_sh_update();
    }

    @Override
    public void reset() {
        SEGAPCMResetChip();
    }
/*TODO*///	/*********************************************************/
/*TODO*///	/*    SEGA 16ch 8bit PCM                                 */
/*TODO*///	/*********************************************************/
/*TODO*///
/*TODO*///	#include "driver.h"
/*TODO*///
/*TODO*///	#include <math.h>
/*TODO*///
/*TODO*///	#define  PCM_NORMALIZE

        public static final int SPCM_CENTER             = (0x80);
	public static final int SEGA_SAMPLE_RATE        = (15800);
        public static final int SEGA_SAMPLE_SHIFT       = (5);
        public static final int SEGA_SAMPLE_RATE_OLD    = (15800*2);
        public static final int SEGA_SAMPLE_SHIFT_OLD   = (5);

/*TODO*///	#define  MIN_SLICE    (44/2)

        public static final int PCM_ADDR_SHIFT          = (12);

	static SEGAPCM    spcm = new SEGAPCM();
	static int emulation_rate;
	static int buffer_len;
	static UBytePtr pcm_rom;
	static int  sample_rate, sample_shift;

	static int stream;

	static int SEGAPCM_samples[][] = {
		{ SEGA_SAMPLE_RATE, SEGA_SAMPLE_SHIFT, },
		{ SEGA_SAMPLE_RATE_OLD, SEGA_SAMPLE_SHIFT_OLD, },
	};

/*TODO*///	#if 0
/*TODO*///	static int segapcm_gaintable[] = {
/*TODO*///		0x00,0x02,0x04,0x06,0x08,0x0a,0x0c,0x0e,0x10,0x12,0x14,0x16,0x18,0x1a,0x1c,0x1e,
/*TODO*///		0x20,0x22,0x24,0x26,0x28,0x2a,0x2c,0x2e,0x30,0x32,0x34,0x36,0x38,0x3a,0x3c,0x3e,
/*TODO*///		0x40,0x42,0x44,0x46,0x48,0x4a,0x4c,0x4e,0x50,0x52,0x54,0x56,0x58,0x5a,0x5c,0x5e,
/*TODO*///		0x60,0x62,0x64,0x66,0x68,0x6a,0x6c,0x6e,0x70,0x72,0x74,0x76,0x78,0x7a,0x7c,0x7e,
/*TODO*///
/*TODO*///		0x00,0x02,0x04,0x06,0x08,0x0a,0x0c,0x0e,0x10,0x12,0x14,0x16,0x18,0x1a,0x1c,0x1e,
/*TODO*///		0x20,0x22,0x24,0x26,0x28,0x2a,0x2c,0x2e,0x30,0x32,0x34,0x36,0x38,0x3a,0x3c,0x3e,
/*TODO*///		0x40,0x42,0x44,0x46,0x48,0x4a,0x4c,0x4e,0x50,0x52,0x54,0x56,0x58,0x5a,0x5c,0x5e,
/*TODO*///		0x60,0x62,0x64,0x66,0x68,0x6a,0x6c,0x6e,0x70,0x72,0x74,0x76,0x78,0x7a,0x7c,0x7e,
/*TODO*///
/*TODO*///	#if 0
/*TODO*///		0x00,0x04,0x08,0x0c,0x10,0x14,0x18,0x1c,0x20,0x24,0x28,0x2c,0x30,0x34,0x38,0x3c,
/*TODO*///		0x40,0x42,0x44,0x46,0x48,0x4a,0x4c,0x4e,0x50,0x52,0x54,0x56,0x58,0x5a,0x5c,0x5e,
/*TODO*///		0x60,0x61,0x62,0x63,0x64,0x65,0x66,0x67,0x68,0x69,0x6a,0x6b,0x6c,0x6d,0x6e,0x6f,
/*TODO*///		0x70,0x71,0x72,0x73,0x74,0x75,0x76,0x77,0x78,0x79,0x7a,0x7b,0x7c,0x7d,0x7e,0x7f,
/*TODO*///	#endif
/*TODO*///	#if 0
/*TODO*///		0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f,
/*TODO*///		0x10,0x11,0x12,0x13,0x14,0x15,0x16,0x17,0x18,0x19,0x1a,0x1b,0x1c,0x1d,0x1e,0x1f,
/*TODO*///		0x20,0x21,0x22,0x23,0x24,0x25,0x26,0x27,0x28,0x29,0x2a,0x2b,0x2c,0x2d,0x2e,0x2f,
/*TODO*///		0x30,0x31,0x32,0x33,0x34,0x35,0x36,0x37,0x38,0x39,0x3a,0x3b,0x3c,0x3d,0x3e,0x3f,
/*TODO*///	#endif
/*TODO*///	};
/*TODO*///	#endif
/*TODO*///
/*TODO*///
/*TODO*///	static void SEGAPCMUpdate( int num, INT16 **buffer, int length );


	/************************************************/
	/*                                              */
	/************************************************/
	static int SEGAPCM_sh_start( MachineSound msound )
	{
		SEGAPCMinterface intf = (SEGAPCMinterface) msound.sound_interface;
		if (Machine.sample_rate == 0) return 0;
		if( SEGAPCMInit( msound, intf.bank&0x00ffffff, intf.mode, new UBytePtr(memory_region(intf.region)), intf.volume ) != 0)
			return 1;
		return 0;
	}
        
	/************************************************/
	/*                                              */
	/************************************************/
	static void SEGAPCM_sh_stop()
	{
/*TODO*///		SEGAPCMShutdown();
	}
        
	/************************************************/
	/*                                              */
	/************************************************/
	static void SEGAPCM_sh_update()
	{
	}

	/************************************************/
	/*    initial SEGAPCM                           */
	/************************************************/
	static int SEGAPCMInit( MachineSound msound, int banksize, int mode, UBytePtr inpcm, int volume )
	{
		int i;
		int rate = Machine.sample_rate;
		buffer_len = rate / Machine.drv.frames_per_second;
		emulation_rate = buffer_len * Machine.drv.frames_per_second;
		sample_rate = SEGAPCM_samples[mode][0];
		sample_shift = SEGAPCM_samples[mode][1];
		pcm_rom = new UBytePtr(inpcm);

		//printf( "segaPCM in\n" );

		/**** interface init ****/
		spcm.bankshift = banksize&0xffffff;
		if( (banksize>>16) == 0x00 )
		{
			spcm.bankmask = (BANK_MASK7>>16)&0x00ff;	/* default */
		}
		else
		{
			spcm.bankmask = (banksize>>16)&0x00ff;
		}

		for( i = 0; i < SEGAPCM_MAX; i++ )
		{
			spcm.gain[i][L_PAN] = spcm.gain[i][R_PAN] = 0;
			spcm.vol[i][L_PAN] = spcm.vol[i][R_PAN] = 0;
			spcm.addr_l[i] = 0;
			spcm.addr_h[i] = 0;
			spcm.bank[i] = 0;
			spcm.end_h[i] = 0;
			spcm.delta_t[i] = 0x80;
			spcm.flag[i] = 1;
			spcm.add_addr[i] = 0;
			spcm.step[i] = (int)(((float)sample_rate / (float)emulation_rate) * (float)(0x80<<5));
			spcm.pcmd[i] = 0;
		}
		//printf( "segaPCM work init end\n" );

		{
			String[] buf=new String[LR_PAN];
			String[] name = new String[LR_PAN];
			int[]  vol=new int[2];
			name[0] = buf[0];
			name[1] = buf[1];
			buf[0] = sprintf( "%s L", sound_name(msound) );
			buf[1] = sprintf( "%s R", sound_name(msound) );
			vol[0] = (MIXER_PAN_LEFT<<8)  | (volume&0xff);
			vol[1] = (MIXER_PAN_RIGHT<<8) | (volume&0xff);
			stream = stream_init_multi( LR_PAN, name, vol, rate, 0, SEGAPCMUpdate );
		}
		//printf( "segaPCM end\n" );
		return 0;
	}

/*TODO*///	/************************************************/
/*TODO*///	/*    shutdown SEGAPCM                          */
/*TODO*///	/************************************************/
/*TODO*///	void SEGAPCMShutdown( void )
/*TODO*///	{
/*TODO*///	}

	/************************************************/
	/*    reset SEGAPCM                             */
	/************************************************/
	static void SEGAPCMResetChip()
	{
		int i;
		for( i = 0; i < SEGAPCM_MAX; i++ )
		{
			spcm.gain[i][L_PAN] = spcm.gain[i][R_PAN] = 0;
			spcm.vol[i][L_PAN] = spcm.vol[i][R_PAN] = 0;
			spcm.addr_l[i] = 0;
			spcm.addr_h[i] = 0;
			spcm.bank[i] = 0;
			spcm.end_h[i] = 0;
			spcm.delta_t[i] = 0;
			spcm.flag[i] = 1;
			spcm.add_addr[i] = 0;
			spcm.step[i] = (int)(((float)sample_rate / (float)emulation_rate) * (float)(0x80<<5));
		}
	}

	/************************************************/
	/*    update SEGAPCM                            */
	/************************************************/

	static int ILimit(int v, int max, int min) { return v > max ? max : (v < min ? min : v); }

	static StreamInitMultiPtr SEGAPCMUpdate = new StreamInitMultiPtr() {
            @Override
            public void handler(int param, ShortPtr[] buffer, int length) {
                int i, j;
		int addr, old_addr, end_addr, end_check_addr;
		UBytePtr pcm_buf=new UBytePtr();
		int  lv, rv;
		ShortPtr[]  datap=new ShortPtr[2];
		int tmp;

		if( Machine.sample_rate == 0 ) return;
		if( pcm_rom == null )    return;
                pcm_rom.offset=0;

		datap[0] = new ShortPtr(buffer[0]);
		datap[1] = new ShortPtr(buffer[1]);

		memset( datap[0], 0x00, length );
		memset( datap[1], 0x00, length );

		for( i = 0; i < SEGAPCM_MAX; i++ )
		{

			if( spcm.flag[i] == 2)

			{

				spcm.flag[i]=0;

				spcm.add_addr[i] = (( (((int)spcm.addr_h[i]<<8)&0xff00) |

					  (spcm.addr_l[i]&0x00ff) ) << PCM_ADDR_SHIFT) &0x0ffff000;

			}
			if( spcm.flag[i] == 0 )
			{
				lv = spcm.vol[i][L_PAN];   rv = spcm.vol[i][R_PAN];
				if(lv==0 && rv==0) continue;

				pcm_buf = new UBytePtr(pcm_rom, (((int)spcm.bank[i]&spcm.bankmask)<<spcm.bankshift));
				addr = (spcm.add_addr[i]>>PCM_ADDR_SHIFT)&0x0000ffff;
				end_addr = (((spcm.end_h[i]<<8)&0xff00) + 0x00ff);

				if(spcm.end_h[i] < spcm.addr_h[i]) end_addr+=0x10000;

				for( j = 0; j < length; j++ )
				{
					old_addr = addr;
					/**** make address ****/

					end_check_addr = (spcm.add_addr[i]>>PCM_ADDR_SHIFT);
					addr = end_check_addr&0x0000ffff;
					for(; old_addr <= addr; old_addr++ )
					{
						/**** end address check ****/
						if( end_check_addr >= end_addr )
						{
							if((spcm.writeram.read(i*8+0x86) & 0x02) != 0)
							{
								spcm.flag[i] = 1;
								spcm.writeram.write(i*8+0x86, (spcm.writeram.read(i*8+0x86)&0xfe)|1);
								break;
							}
							else
							{ /* Loop */
								spcm.add_addr[i] = (( (((int)spcm.addr_h[i]<<8)&0xff00) |
										(spcm.addr_l[i]&0x00ff) ) << PCM_ADDR_SHIFT) &0x0ffff000;
							}
						}
/*TODO*///	#ifdef PCM_NORMALIZE
						tmp = spcm.pcmd[i];
						spcm.pcmd[i] = (new UBytePtr(pcm_buf, old_addr).read() - SPCM_CENTER);
						spcm.pcma[i] = (tmp - spcm.pcmd[i]) / 2;
						spcm.pcmd[i] += spcm.pcma[i];
/*TODO*///	#endif
					}
					spcm.add_addr[i] += spcm.step[i];
					if( spcm.flag[i] == 1 )  break;
/*TODO*///	#ifndef PCM_NORMALIZE
/*TODO*///					*(datap[0] + j) = ILimit( (int)*(datap[0] + j) + ((int)(*(pcm_buf + addr) - SPCM_CENTER)*lv), 32767, -32768 );
/*TODO*///					*(datap[1] + j) = ILimit( (int)*(datap[1] + j) + ((int)(*(pcm_buf + addr) - SPCM_CENTER)*rv), 32767, -32768 );
/*TODO*///	#else
					datap[0].write(j, (short) ILimit( datap[0].read(j) + (spcm.pcmd[i] * lv), 32767, -32768 ));
					datap[1].write(j, (short) ILimit( datap[1].read(j) + (spcm.pcmd[i] * rv), 32767, -32768 ));
/*TODO*///	#endif
				}
				/**** end of length ****/
			}
			/**** end flag check ****/
		}
		/**** pcm channel end ****/
                buffer[0] = new ShortPtr(datap[0]);
		buffer[1] = new ShortPtr(datap[1]);
            }
        };
        
	/************************************************/
	/*    wrtie register SEGAPCM                    */
	/************************************************/
	public static WriteHandlerPtr SEGAPCMWriteReg = new WriteHandlerPtr() {
            @Override
            public void handler(int r, int v) {
                int rate;
		int  lv, rv, cen;

		int channel = (r>>3)&0x0f;

		spcm.writeram.write(r&0x07ff, (char)v);		/* write value data */

		switch( (r&0x87) )
		{
			case 0x00:
			case 0x01:
			case 0x84:  case 0x85:
			case 0x87:
				break;

			case 0x02:
				spcm.gain[channel][L_PAN] = (char) (v&0xff);
	remake_vol:
				lv = spcm.gain[channel][L_PAN];   rv = spcm.gain[channel][R_PAN];
				cen = (lv + rv) / 4;
	//			spcm.vol[channel][L_PAN] = (lv + cen)<<1;
	//			spcm.vol[channel][R_PAN] = (rv + cen)<<1;
				spcm.vol[channel][L_PAN] = (lv + cen)*9/5;	// too much clipping
				spcm.vol[channel][R_PAN] = (rv + cen)*9/5;
				break;
			case 0x03:
				spcm.gain[channel][R_PAN] = (char) (v&0xff);
				//goto remake_vol;
                                lv = spcm.gain[channel][L_PAN];   rv = spcm.gain[channel][R_PAN];
				cen = (lv + rv) / 4;
	//			spcm.vol[channel][L_PAN] = (lv + cen)<<1;
	//			spcm.vol[channel][R_PAN] = (rv + cen)<<1;
				spcm.vol[channel][L_PAN] = (lv + cen)*9/5;	// too much clipping
				spcm.vol[channel][R_PAN] = (rv + cen)*9/5;
				break;

			case 0x04:
				spcm.addr_l[channel]= (char) v;
				break;
			case 0x05:
				spcm.addr_h[channel]= (char) v;
				break;
			case 0x06:
				spcm.end_h[channel]= (char) v;
				break;
			case 0x07:
				spcm.delta_t[channel]= (char) v;
				rate = (v&0x00ff)<<sample_shift;
				spcm.step[channel] = (int)(((float)sample_rate / (float)emulation_rate) * (float)rate);
				break;
			case 0x86:
				spcm.bank[channel] = (char) v;
				if(( v&1 ) != 0)    spcm.flag[channel] = 1; /* stop D/A */
				else
				{
					/**** start D/A ****/
	//				spcm.flag[channel] = 0;
					spcm.flag[channel] = 2;

	//				spcm.add_addr[channel] = (( (((int)spcm.addr_h[channel]<<8)&0xff00) |
	//					  (spcm.addr_l[channel]&0x00ff) ) << PCM_ADDR_SHIFT) &0x0ffff000;
					spcm.pcmd[channel] = 0;
				}
				break;
			/*
			default:
				printf( "unknown %d = %02x : %02x\n", channel, r, v );
				break;
			*/
		}
            }
        };
        
	/************************************************/
	/*    read register SEGAPCM                     */
	/************************************************/
	public static ReadHandlerPtr SEGAPCMReadReg = new ReadHandlerPtr() {
            @Override
            public int handler(int r) {
                return spcm.writeram.read(r&0x07ff);		/* read value data */
            }
        };
        
/*TODO*///	/**************** end of file ****************/

}
