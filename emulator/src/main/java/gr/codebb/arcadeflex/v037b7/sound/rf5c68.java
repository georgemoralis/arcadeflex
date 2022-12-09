/*********************************************************/
/*    ricoh RF5C68(or clone) PCM controller              */
/*********************************************************/

/*
 * ported to v0.37b7
 * using automatic conversion tool v0.01
 */ 
package gr.codebb.arcadeflex.v037b7.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;

import gr.codebb.arcadeflex.common.PtrLib;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static arcadeflex.v036.mame.driverH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;
import static gr.codebb.arcadeflex.v036.sound.mixerH.*;
import static gr.codebb.arcadeflex.v037b7.sound.rf5c68H.*;
import static arcadeflex.v036.sound.streams.*;
import static common.libc.cstring.memset;

public class rf5c68 extends snd_interface {
	
	public static int PCM_NORMALIZE=1;
	
	public static final int RF_L_PAN = 0;
        public static final int RF_R_PAN = 1;
        public static final int RF_LR_PAN = 2;
	
	
	static RF5C68PCM    rpcm = new RF5C68PCM();
	static int  reg_port;
	static int emulation_rate;
	
	static int buffer_len;
	static int stream;
	
	//static unsigned char pcmbuf[0x10000];
	static UBytePtr pcmbuf=null;
	
	static RF5C68interface intf;
	
	static int[] wreg=new int[0x10]; /* write data */
	
	public static final int BASE_SHIFT    = (11+4);
	
	public static final int RF_ON     = (1<<0);
	public static final int RF_START  = (1<<1);
	
        public rf5c68() {
            this.name = "RF5C68";
            this.sound_num = SOUND_RF5C68;            
        }
	
	@Override
        public int chips_num(MachineSound msound) {
            return 0;
        }

        @Override
        public int chips_clock(MachineSound msound) {
            return ((RF5C68interface) msound.sound_interface).clock;
        }

        @Override
        public int start(MachineSound msound) {
            return RF5C68_sh_start(msound);
        }

        @Override
        public void stop() {
            RF5C68_sh_stop.handler();
        }

        @Override
        public void update() {
/*TODO*///            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void reset() {
/*TODO*///            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
	
	/************************************************/
	/*    RF5C68 start                              */
	/************************************************/
	public static int RF5C68_sh_start( MachineSound msound )
	{
		int i;
		int rate = Machine.sample_rate;
		RF5C68interface inintf = (RF5C68interface) msound.sound_interface;
	
		if (Machine.sample_rate == 0) return 0;
	
		if(pcmbuf==null) pcmbuf=new UBytePtr(0x10000);
		if(pcmbuf==null) return 1;
	
		intf = inintf;
		buffer_len = rate / Machine.drv.frames_per_second;
		emulation_rate = buffer_len * Machine.drv.frames_per_second;
	
		rpcm.clock = intf.clock;
		for( i = 0; i < RF5C68_PCM_MAX; i++ )
		{
			rpcm.env[i] = 0;
			rpcm.pan[i] = 0;
			rpcm.start[i] = 0;
			rpcm.step[i] = 0;
			rpcm.flag[i] = 0;
		}
		for( i = 0; i < 0x10; i++ )  wreg[i] = 0;
		reg_port = 0;
	
		{
			String[] buf = new String[RF_LR_PAN];
			String[] name= new String[RF_LR_PAN];
			int[]  vol = new int[2];
			name[0] = buf[0];
			name[1] = buf[1];
			buf[0] = sprintf( "%s Left", sound_name(msound) );
			buf[1] = sprintf( "%s Right", sound_name(msound) );
			vol[0] = (MIXER_PAN_LEFT<<8)  | (intf.volume&0xff);
			vol[1] = (MIXER_PAN_RIGHT<<8) | (intf.volume&0xff);
	
			stream = stream_init_multi( RF_LR_PAN, name, vol, rate, 0, RF5C68Update );
			if(stream == -1) return 1;
		}

		return 0;
	}
	
	/************************************************/
	/*    RF5C68 stop                               */
	/************************************************/
	public static ShStopHandlerPtr RF5C68_sh_stop = new ShStopHandlerPtr() { public void handler() 
	{
		if(pcmbuf!=null) 
                    pcmbuf=null;
	} };
	
	/************************************************/
	/*    RF5C68 update                             */
	/************************************************/
	
	public static int ILimit(int v, int max, int min) { return v > max ? max : (v < min ? min : v); }
	
	static StreamInitMultiPtr RF5C68Update = new StreamInitMultiPtr() {
            @Override
            public void handler(int num, ShortPtr[] buffer, int length) {
                int i, j, tmp;
		int addr, old_addr;
		int ld, rd;
		ShortPtr[] datap=new ShortPtr[2];
	
		datap[RF_L_PAN] = new ShortPtr(buffer[0]);
		datap[RF_R_PAN] = new ShortPtr(buffer[1]);
	
		memset( datap[RF_L_PAN], 0x00, length );
		memset( datap[RF_R_PAN], 0x00, length );
	
		for( i = 0; i < RF5C68_PCM_MAX; i++ )
		{
			if( (rpcm.flag[i]&(RF_START|RF_ON)) == (RF_START|RF_ON) )
			{
				/**** PCM setup ****/
				addr = (rpcm.addr[i]>>BASE_SHIFT)&0xffff;
				ld = (rpcm.pan[i]&0x0f);
				rd = ((rpcm.pan[i]>>4)&0x0f);
				/*       cen = (ld + rd) / 4; */
				/*       ld = (rpcm.env[i]&0xff) * (ld + cen); */
				/*       rd = (rpcm.env[i]&0xff) * (rd + cen); */
				ld = (rpcm.env[i]&0xff) * ld;
				rd = (rpcm.env[i]&0xff) * rd;
	
				for( j = 0; j < length; j++ )
				{
					old_addr = addr;
					addr = (rpcm.addr[i]>>BASE_SHIFT)&0xffff;
					for(; old_addr <= addr; old_addr++ )
					{
						/**** PCM end check ****/
						if( ((pcmbuf.read(old_addr))&0x00ff) == 0x00ff )
						{
							rpcm.addr[i] = rpcm.loop[i] + ((addr - old_addr)<<BASE_SHIFT);
							addr = (rpcm.addr[i]>>BASE_SHIFT)&0xffff;
							/**** PCM loop check ****/
							if( ((pcmbuf.read(addr))&0x00ff) == 0x00ff )	// this causes looping problems
							{
								rpcm.flag[i] = 0;
								break;
							}
							else
							{
								old_addr = addr;
							}
						}
/*TODO*///	#ifdef PCM_NORMALIZE
						tmp = rpcm.pcmd[i];
						rpcm.pcmd[i] = (pcmbuf.read(old_addr)&0x7f) * (-1 + (2 * ((pcmbuf.read(old_addr)>>7)&0x01)));
						rpcm.pcma[i] = (tmp - rpcm.pcmd[i]) / 2;
						rpcm.pcmd[i] += rpcm.pcma[i];
/*TODO*///	#endif
					}
					rpcm.addr[i] += rpcm.step[i];
					if( rpcm.flag[i] == 0 )  break; /* skip PCM */
/*TODO*///	#ifndef PCM_NORMALIZE
/*TODO*///					if( pcmbuf[addr]&0x80 )
/*TODO*///					{
/*TODO*///						rpcm.pcmx[0][i] = ((signed int)(pcmbuf[addr]&0x7f))*ld;
/*TODO*///						rpcm.pcmx[1][i] = ((signed int)(pcmbuf[addr]&0x7f))*rd;
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						rpcm.pcmx[0][i] = ((signed int)(-(pcmbuf[addr]&0x7f)))*ld;
/*TODO*///						rpcm.pcmx[1][i] = ((signed int)(-(pcmbuf[addr]&0x7f)))*rd;
/*TODO*///					}
/*TODO*///	#else
					rpcm.pcmx[0][i] = rpcm.pcmd[i] * ld;
					rpcm.pcmx[1][i] = rpcm.pcmd[i] * rd;
                                        
                                        datap[RF_L_PAN].write(j, (short) ILimit( ((datap[RF_L_PAN].read(j)) + ((rpcm.pcmx[0][i])>>4)) , 32767, -32768 ));
					datap[RF_R_PAN].write(j, (short) ILimit( ((datap[RF_R_PAN].read(j)) + ((rpcm.pcmx[1][i])>>4)), 32767, -32768 ));
				}
			}
		}
                
                buffer[0]=new ShortPtr(datap[RF_L_PAN]);
		buffer[1]=new ShortPtr(datap[RF_R_PAN]);
            }
        };
        
	/************************************************/
	/*    RF5C68 write register                     */
	/************************************************/
	public static WriteHandlerPtr RF5C68_reg_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int  i;
		int  val;
	
		wreg[offset] = data;			/* stock write data */
		/**** set PCM registers ****/
		if( (wreg[0x07]&0x40) != 0 )  reg_port = wreg[0x07]&0x07;	/* select port # */
	
		switch( offset )
		{
			case 0x00:
				rpcm.env[reg_port] = data;		/* set env. */
				break;
			case 0x01:
				rpcm.pan[reg_port] = data;		/* set pan */
				break;
			case 0x02:
			case 0x03:
				/**** address step ****/
				val = (((((int)wreg[0x03])<<8)&0xff00) | (((int)wreg[0x02])&0x00ff));
				rpcm.step[reg_port] = (int)(
				(
				( 28456.0 / (float)emulation_rate ) *
				( val / (float)(0x0800) ) *
				(rpcm.clock / 8000000.0) *
				(1<<BASE_SHIFT)
				)
				);
				break;
			case 0x04:
			case 0x05:
				/**** loop address ****/
				rpcm.loop[reg_port] = ((((wreg[0x05])<<8)&0xff00)|((wreg[0x04])&0x00ff))<<(BASE_SHIFT);
				break;
			case 0x06:
				/**** start address ****/
				rpcm.start[reg_port] = ((wreg[0x06])&0x00ff)<<(BASE_SHIFT + 8);
				rpcm.addr[reg_port] = rpcm.start[reg_port];
				break;
			case 0x07:
				if( (data&0xc0) == 0xc0 )
				{
					i = data&0x07;		/* register port */
					rpcm.pcmx[0][i] = 0;
					rpcm.pcmx[1][i] = 0;
					rpcm.flag[i] |= RF_START;
				}
				break;
	
			case 0x08:
				/**** pcm on/off ****/
				for( i = 0; i < RF5C68_PCM_MAX; i++ )
				{
					if( (data&(1<<i)) == 0 )
					{
						rpcm.flag[i] |= RF_ON;	/* PCM on */
					}
					else
					{
						rpcm.flag[i] &= ~(RF_ON); /* PCM off */
					}
				}
				break;
		}
	} };
	
	/************************************************/
	/*    RF5C68 read memory                        */
	/************************************************/
	public static ReadHandlerPtr RF5C68_r  = new ReadHandlerPtr() { public int handler(int offset)
	{
		int  bank;
		bank = ((wreg[0x07]&0x0f))<<(8+4);
		return pcmbuf.read(bank + offset);
	} };
	/************************************************/
	/*    RF5C68 write memory                       */
	/************************************************/
	public static WriteHandlerPtr RF5C68_w = new WriteHandlerPtr() {public void handler(int offset, int data)
	{
		int  bank;
		bank = ((wreg[0x07]&0x0f))<<(8+4);
		pcmbuf.write(bank + offset, data);
	} };
	
	
	/**************** end of file ****************/
}
