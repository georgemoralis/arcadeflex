package sound;

import mame.sndintrf.*;
import static mame.sndintrfH.*;
import static sound.vlm5030H.*;
import static mame.driverH.*;
import static mame.sndintrf.*;
import static arcadeflex.libc_old.*;
import static mame.mame.*;
import static sound.fm.*;
import static sound.fmH.*;
import static sound.streams.*;
import static mame.timer.*;
import static arcadeflex.ptrlib.*;
import static mame.common.*;
import static sound.mixer.*;

public class vlm5030 extends snd_interface {

    public vlm5030() {
        this.name = "YM-VLM5030";
        this.sound_num = SOUND_VLM5030;
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;//no functionality expected
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((VLM5030interface) msound.sound_interface).baseclock;
    }

    public static final int IP_SIZE = 20;		/* samples per interpolator */

    public static final int FR_SIZE = 8;		/* interpolator per frame   */

    static VLM5030interface intf;

    static int channel;
    static int schannel;

    static UBytePtr VLM5030_rom;
    static int VLM5030_address_mask;
    static int VLM5030_address;
    static int pin_BSY;
    static int pin_ST;
    static int pin_RST;
    static int latch_data = 0;
    static int sampling_mode;

    static int table_h;

    public static final int PH_RESET = 0;
    public static final int PH_IDLE = 1;
    public static final int PH_SETUP = 2;
    public static final int PH_WAIT = 3;
    public static final int PH_RUN = 4;
    public static final int PH_STOP = 5;
    static int phase;

    /* these contain data describing the current and previous voice frames */
    static /*unsigned short*/ char old_energy = 0;
    static /*unsigned short*/ char old_pitch = 0;
    static int old_k[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    static /*unsigned short*/ char new_energy = 0;
    static /*unsigned short*/ char new_pitch = 0;
    static int new_k[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /* these are all used to contain the current state of the sound generation */
    static /*unsigned short*/ char current_energy = 0;
    static /*unsigned short*/ char current_pitch = 0;
    static int current_k[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    static /*unsigned short*/ char target_energy = 0;
    static /*unsigned short*/ char target_pitch = 0;
    static int target_k[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    static int interp_count = 0;       /* number of interp periods (0-7) */

    static int sample_count = 0;       /* sample number within interp (0-19) */

    static int pitch_count = 0;

    static int u[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    static int x[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    /* ROM Tables */
    /* This is the energy lookup table */
    /* !!!!!!!!!! preliminary !!!!!!!!!! */
    static /*unsigned short*/ char[] energytable = new char[0x20];

    /* This is the pitch lookup table */
    static int pitchtable[]
            = {
                0, /* 0     : random mode */
                22, /* 1     : start=22    */
                23, 24, 25, 26, 27, 28, 29, 30, /*  2- 9 : 1step       */
                32, 34, 36, 38, 40, 42, 44, 46, /* 10-17 : 2step       */
                50, 54, 58, 62, 66, 70, 74, 78, /* 18-25 : 4step       */
                86, 94, 102, 110, 118, /* 26-30 : 8step       */
                255 /* 31    : only one time ?? */};

    /* These are the reflection coefficient lookup tables */
    /* 2's comp. */

    /* !!!!!!!!!! preliminary !!!!!!!!!! */

    /* 7bit */
    public static final int K1_RANGE = 0x6000;
    /* 4bit */
    public static final int K2_RANGE = 0x4000;
    public static final int K3_RANGE = 0x6000;
    public static final int K4_RANGE = 0x4000;
    /* 3bit */
    public static final int K5_RANGE = 0x6000;
    public static final int K6_RANGE = 0x6000;
    public static final int K7_RANGE = 0x5000;
    public static final int K8_RANGE = 0x4000;
    public static final int K9_RANGE = 0x5000;
    public static final int K10_RANGE = 0x4000;

    static int[] k1table = new int[0x80];
    static int[] k2table = new int[0x10];
    static int[] k3table = new int[0x10];
    static int[] k4table = new int[0x10];
    static int[] k5table = new int[0x08];
    static int[] k6table = new int[0x08];
    static int[] k7table = new int[0x08];
    static int[] k8table = new int[0x08];
    static int[] k9table = new int[0x08];
    static int[] k10table = new int[0x08];

    /* chirp table */
    static /*unsigned char*/ int chirptable[]
            = {
                0xff * 9 / 10,
                0xff * 7 / 10,
                0xff * 5 / 10,
                0xff * 4 / 10, /* non digital filter ? */
                0xff * 3 / 10,
                0xff * 3 / 10,
                0xff * 1 / 10,
                0xff * 1 / 10,
                0xff * 1 / 10,
                0xff * 1 / 10,
                0xff * 1 / 10,
                0xff * 1 / 10
            };

    /* interpolation coefficients */
    static int interp_coeff[] = {
        //8, 8, 8, 4, 4, 2, 2, 1
        8, 8, 8, 4, 4, 2, 2, 1
    };
    /* check sample file */

    static int check_samplefile(int num) {
        if (Machine.samples == null) {
            return 0;
        }
        if (Machine.samples.total <= num) {
            return 0;
        }
        if (Machine.samples.sample[num] == null) {
            return 0;
        }
        /* sample file is found */
        return 1;
    }
    /*TODO*///
/*TODO*///static int get_bits(int sbit,int bits)
/*TODO*///{
/*TODO*///	int offset = VLM5030_address + (sbit>>3);
/*TODO*///	int data;
/*TODO*///
/*TODO*///	data = VLM5030_rom[offset&VLM5030_address_mask] |
/*TODO*///	       (((int)VLM5030_rom[(offset+1)&VLM5030_address_mask])<<8);
/*TODO*///	data >>= sbit;
/*TODO*///	data &= (0xff>>(8-bits));
/*TODO*///
/*TODO*///	return data;
/*TODO*///}
/*TODO*///
/*TODO*////* get next frame */
/*TODO*///static int parse_frame (void)
/*TODO*///{
/*TODO*///	unsigned char cmd;
/*TODO*///
/*TODO*///	/* remember previous frame */
/*TODO*///	old_energy = new_energy;
/*TODO*///	old_pitch = new_pitch;
/*TODO*///	memcpy( old_k , new_k , sizeof(old_k) );
/*TODO*///	/* command byte check */
/*TODO*///	cmd = VLM5030_rom[VLM5030_address&VLM5030_address_mask];
/*TODO*///	if( cmd & 0x01 )
/*TODO*///	{	/* extend frame */
/*TODO*///		new_energy = new_pitch = 0;
/*TODO*///		memset( new_k , 0 , sizeof(new_k));
/*TODO*///		VLM5030_address++;
/*TODO*///		if( cmd & 0x02 )
/*TODO*///		{	/* end of speech */
/*TODO*///			if(errorlog) fprintf(errorlog,"VLM5030 %04X end \n",VLM5030_address );
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		else
/*TODO*///		{	/* silent frame */
/*TODO*///			int nums = ( (cmd>>2)+1 )*2;
/*TODO*///			if(errorlog) fprintf(errorlog,"VLM5030 %04X silent %d frame\n",VLM5030_address,nums );
/*TODO*///			return nums * FR_SIZE;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* normal frame */
/*TODO*///
/*TODO*///	new_pitch  = pitchtable[get_bits( 1,5)];
/*TODO*///	new_energy = energytable[get_bits( 6,5)] >> 6;
/*TODO*///
/*TODO*///	/* 10 K's */
/*TODO*///	new_k[9] = k10table[get_bits(11,3)];
/*TODO*///	new_k[8] = k9table[get_bits(14,3)];
/*TODO*///	new_k[7] = k8table[get_bits(17,3)];
/*TODO*///	new_k[6] = k7table[get_bits(20,3)];
/*TODO*///	new_k[5] = k6table[get_bits(23,3)];
/*TODO*///	new_k[4] = k5table[get_bits(26,3)];
/*TODO*///	new_k[3] = k4table[get_bits(29,4)];
/*TODO*///	new_k[2] = k3table[get_bits(33,4)];
/*TODO*///	new_k[1] = k2table[get_bits(37,4)];
/*TODO*///	new_k[0] = k1table[get_bits(41,7)];
/*TODO*///
/*TODO*///	VLM5030_address+=6;
/*TODO*///	if(errorlog) fprintf(errorlog,"VLM5030 %04X voice \n",VLM5030_address );
/*TODO*///	return FR_SIZE;
/*TODO*///}
/*TODO*///
/*TODO*////* decode and buffering data */
    public static StreamInitPtr vlm5030_update_callback = new StreamInitPtr() {
        public void handler(int chip, UShortPtr buffer, int length) {
            /*TODO*///	int buf_count=0;
/*TODO*///	int interp_effect;
/*TODO*///
/*TODO*///	/* running */
/*TODO*///	if( phase == PH_RUN )
/*TODO*///	{
/*TODO*///		/* playing speech */
/*TODO*///		while (length > 0)
/*TODO*///		{
/*TODO*///			int current_val;
/*TODO*///
/*TODO*///			/* check new interpolator or  new frame */
/*TODO*///			if( sample_count == 0 )
/*TODO*///			{
/*TODO*///				sample_count = IP_SIZE;
/*TODO*///				/* interpolator changes */
/*TODO*///				if ( interp_count == 0 )
/*TODO*///				{
/*TODO*///					/* change to new frame */
/*TODO*///					interp_count = parse_frame(); /* with change phase */
/*TODO*///					if ( interp_count == 0 )
/*TODO*///					{
/*TODO*///						sample_count = 160; /* end -> stop time */
/*TODO*///						phase = PH_STOP;
/*TODO*///						goto phase_stop; /* continue to stop phase */
/*TODO*///					}
/*TODO*///					/* Set old target as new start of frame */
/*TODO*///					current_energy = old_energy;
/*TODO*///					current_pitch = old_pitch;
/*TODO*///					memcpy( current_k , old_k , sizeof(current_k) );
/*TODO*///					/* is this a zero energy frame? */
/*TODO*///					if (current_energy == 0)
/*TODO*///					{
/*TODO*///						/*printf("processing frame: zero energy\n");*/
/*TODO*///						target_energy = 0;
/*TODO*///						target_pitch = current_pitch;
/*TODO*///						memcpy( target_k , current_k , sizeof(target_k) );
/*TODO*///					}
/*TODO*///					else
/*TODO*///					{
/*TODO*///						/*printf("processing frame: Normal\n");*/
/*TODO*///						/*printf("*** Energy = %d\n",current_energy);*/
/*TODO*///						/*printf("proc: %d %d\n",last_fbuf_head,fbuf_head);*/
/*TODO*///						target_energy = new_energy;
/*TODO*///						target_pitch = new_pitch;
/*TODO*///						memcpy( target_k , new_k , sizeof(target_k) );
/*TODO*///					}
/*TODO*///				}
/*TODO*///				/* next interpolator */
/*TODO*///				/* Update values based on step values */
/*TODO*///				/*printf("\n");*/
/*TODO*///				interp_effect = (int)(interp_coeff[(FR_SIZE-1) - (interp_count%FR_SIZE)]);
/*TODO*///
/*TODO*///				current_energy += (target_energy - current_energy) / interp_effect;
/*TODO*///				if (old_pitch != 0)
/*TODO*///					current_pitch += (target_pitch - current_pitch) / interp_effect;
/*TODO*///				/*printf("*** Energy = %d\n",current_energy);*/
/*TODO*///				current_k[0] += (target_k[0] - current_k[0]) / interp_effect;
/*TODO*///				current_k[1] += (target_k[1] - current_k[1]) / interp_effect;
/*TODO*///				current_k[2] += (target_k[2] - current_k[2]) / interp_effect;
/*TODO*///				current_k[3] += (target_k[3] - current_k[3]) / interp_effect;
/*TODO*///				current_k[4] += (target_k[4] - current_k[4]) / interp_effect;
/*TODO*///				current_k[5] += (target_k[5] - current_k[5]) / interp_effect;
/*TODO*///				current_k[6] += (target_k[6] - current_k[6]) / interp_effect;
/*TODO*///				current_k[7] += (target_k[7] - current_k[7]) / interp_effect;
/*TODO*///				current_k[8] += (target_k[8] - current_k[8]) / interp_effect;
/*TODO*///				current_k[9] += (target_k[9] - current_k[9]) / interp_effect;
/*TODO*///				interp_count --;
/*TODO*///			}
/*TODO*///			/* calcrate digital filter */
/*TODO*///			if (old_energy == 0)
/*TODO*///			{
/*TODO*///				/* generate silent samples here */
/*TODO*///				current_val = 0x00;
/*TODO*///			}
/*TODO*///			else if (old_pitch == 0)
/*TODO*///			{
/*TODO*///				/* generate unvoiced samples here */
/*TODO*///				int randvol = (rand () % 10);
/*TODO*///				current_val = (randvol * current_energy) / 10;
/*TODO*///			}
/*TODO*///			else
/*TODO*///			{
/*TODO*///				/* generate voiced samples here */
/*TODO*///				if (pitch_count < sizeof (chirptable))
/*TODO*///					current_val = (chirptable[pitch_count] * current_energy) / 256;
/*TODO*///				else
/*TODO*///					current_val = 0x00;
/*TODO*///			}
/*TODO*///
/*TODO*///			/* Lattice filter here */
/*TODO*///			u[10] = current_val;
/*TODO*///			u[9] = u[10] - ((current_k[9] * x[9]) / 32768);
/*TODO*///			u[8] = u[ 9] - ((current_k[8] * x[8]) / 32768);
/*TODO*///			u[7] = u[ 8] - ((current_k[7] * x[7]) / 32768);
/*TODO*///			u[6] = u[ 7] - ((current_k[6] * x[6]) / 32768);
/*TODO*///			u[5] = u[ 6] - ((current_k[5] * x[5]) / 32768);
/*TODO*///			u[4] = u[ 5] - ((current_k[4] * x[4]) / 32768);
/*TODO*///			u[3] = u[ 4] - ((current_k[3] * x[3]) / 32768);
/*TODO*///			u[2] = u[ 3] - ((current_k[2] * x[2]) / 32768);
/*TODO*///			u[1] = u[ 2] - ((current_k[1] * x[1]) / 32768);
/*TODO*///			u[0] = u[ 1] - ((current_k[0] * x[0]) / 32768);
/*TODO*///
/*TODO*///			x[9] = x[8] + ((current_k[8] * u[8]) / 32768);
/*TODO*///			x[8] = x[7] + ((current_k[7] * u[7]) / 32768);
/*TODO*///			x[7] = x[6] + ((current_k[6] * u[6]) / 32768);
/*TODO*///			x[6] = x[5] + ((current_k[5] * u[5]) / 32768);
/*TODO*///			x[5] = x[4] + ((current_k[4] * u[4]) / 32768);
/*TODO*///			x[4] = x[3] + ((current_k[3] * u[3]) / 32768);
/*TODO*///			x[3] = x[2] + ((current_k[2] * u[2]) / 32768);
/*TODO*///			x[2] = x[1] + ((current_k[1] * u[1]) / 32768);
/*TODO*///			x[1] = x[0] + ((current_k[0] * u[0]) / 32768);
/*TODO*///			x[0] = u[0];
/*TODO*///			/* clipping, buffering */
/*TODO*///			if (u[0] > 511)
/*TODO*///				buffer[buf_count] = 127<<8;
/*TODO*///			else if (u[0] < -512)
/*TODO*///				buffer[buf_count] = -128<<8;
/*TODO*///			else
/*TODO*///				buffer[buf_count] = u[0] << 6;
/*TODO*///			buf_count++;
/*TODO*///
/*TODO*///			/* sample count */
/*TODO*///			sample_count--;
/*TODO*///			/* pitch */
/*TODO*///			pitch_count++;
/*TODO*///			if (pitch_count >= current_pitch )
/*TODO*///				pitch_count = 0;
/*TODO*///			/* size */
/*TODO*///			length--;
/*TODO*///		}
/*TODO*////*		return;*/
/*TODO*///	}
/*TODO*///	/* stop phase */
/*TODO*///phase_stop:
/*TODO*///	switch( phase )
/*TODO*///	{
/*TODO*///	case PH_SETUP:
/*TODO*///		sample_count -= length;
/*TODO*///		if( sample_count <= 0 )
/*TODO*///		{
/*TODO*///			if(errorlog) fprintf(errorlog,"VLM5030 BSY=H\n" );
/*TODO*///			/* pin_BSY = 1; */
/*TODO*///			phase = PH_WAIT;
/*TODO*///		}
/*TODO*///		break;
/*TODO*///	case PH_STOP:
/*TODO*///		sample_count -= length;
/*TODO*///		if( sample_count <= 0 )
/*TODO*///		{
/*TODO*///			if(errorlog) fprintf(errorlog,"VLM5030 BSY=L\n" );
/*TODO*///			pin_BSY = 0;
/*TODO*///			phase = PH_IDLE;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	/* silent buffering */
/*TODO*///	while (length > 0)
/*TODO*///	{
/*TODO*///		buffer[buf_count++] = 0x00;
/*TODO*///		length--;
/*TODO*///	}
        }
    };


    /* realtime update */
    public static void VLM5030_update() {
        /*TODO*///	if( !sampling_mode )
/*TODO*///	{
/*TODO*///		/* docode mode */
/*TODO*///		stream_update(channel,0);
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		/* sampling mode (check  busy flag) */
/*TODO*///		if( pin_ST == 0 && pin_BSY == 1 )
/*TODO*///		{
/*TODO*///			if( !mixer_is_sample_playing(schannel) )
/*TODO*///				pin_BSY = 0;
/*TODO*///		}
/*TODO*///	}
    }

    /* set speech rom address */
    public static void VLM5030_set_rom(UBytePtr speech_rom) {
        VLM5030_rom = new UBytePtr(speech_rom);
    }

    /*TODO*////* get BSY pin level */
/*TODO*///int VLM5030_BSY(void)
/*TODO*///{
/*TODO*///	VLM5030_update();
/*TODO*///	return pin_BSY;
/*TODO*///}
/*TODO*///
/* latch contoll data */
    public static WriteHandlerPtr VLM5030_data_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            latch_data = data;
        }
    };
    /* set RST pin level : reset / set table address A8-A15 */

    public static void VLM5030_RST(int pin) {
        if (pin_RST != 0) {
            if (pin == 0) {	/* H -> L : latch high address table */

                pin_RST = 0;
                /*			table_h = latch_data * 256; */
                table_h = 0;
            }
        } else {
            if (pin == 0) {	/* L -> H : reset chip */

                pin_RST = 1;
                if (pin_BSY != 0) {
                    if (sampling_mode != 0) {
                        /*TODO*///mixer_stop_sample( schannel );
                        throw new UnsupportedOperationException("Unsupported");
                    }
                    phase = PH_RESET;
                    pin_BSY = 0;
                }
            }
        }
    }

    /* set VCU pin level : ?? unknown */
    public static void VLM5030_VCU(int pin) {
        /* unknown */
        /*	intf->vcu = pin; */
        return;
    }

    /* set ST pin level  : set table address A0-A7 / start speech */
    public static void VLM5030_ST(int pin) {
        int table = table_h | latch_data;

        if (pin_ST != pin) {
            /* pin level is change */
            if (pin == 0) {	/* H -> L */

                pin_ST = 0;
                /* start speech */

                if (Machine.sample_rate == 0) {
                    pin_BSY = 0;
                    return;
                }
                /* set play mode samplingfile or emulate */
                sampling_mode = check_samplefile(table / 2);
                if (sampling_mode == 0) {
                    VLM5030_update();

                    if (errorlog != null) {
                        fprintf(errorlog, "VLM5030 %02X start adr=%04X\n", table / 2, VLM5030_address);
                    }

                    /* docode mode */
                    VLM5030_address = (((int) VLM5030_rom.read(table & VLM5030_address_mask)) << 8)
                            | VLM5030_rom.read((table + 1) & VLM5030_address_mask);
                    /* reset process status */
                    interp_count = sample_count = 0;
                    /* clear filter */
                    /* after 3 sampling start */
                    phase = PH_RUN;
                } else {
                    throw new UnsupportedOperationException("Unsupported");
                    /*TODO*///				/* sampling mode */
/*TODO*///				int num = table>>1;
/*TODO*///
/*TODO*///				mixer_play_sample(schannel,
/*TODO*///					Machine->samples->sample[num]->data,
/*TODO*///					Machine->samples->sample[num]->length,
/*TODO*///					Machine->samples->sample[num]->smpfreq,
/*TODO*///					0);
                }
            } else {	/* L -> H */

                pin_ST = 1;
                /* setup speech , BSY on after 30ms? */
                phase = PH_SETUP;
                sample_count = 1; /* wait time for busy on */

                pin_BSY = 1; /* */

            }
        }
    }

    /* start VLM5030 with sound rom              */
    /* speech_rom == 0 -> use sampling data mode */
    @Override
    public int start(MachineSound msound) {
        int emulation_rate;

        intf = (VLM5030interface) msound.sound_interface;

        /*TODO*///	Machine->samples = readsamples(intf->samplenames,Machine->gamedrv->name);
        emulation_rate = intf.baseclock / 440;
        pin_BSY = pin_RST = pin_ST = 0;
        phase = PH_IDLE;
        /*	VLM5030_VCU(intf->vcu); */

        VLM5030_rom = memory_region(intf.memory_region);
        /* memory size */
        if (intf.memory_size == 0) {
            VLM5030_address_mask = memory_region_length(intf.memory_region) - 1;
        } else {
            VLM5030_address_mask = intf.memory_size - 1;
        }

        channel = stream_init("VLM5030", intf.volume, emulation_rate /* Machine->sample_rate */,
                0, vlm5030_update_callback);
        if (channel == -1) {
            return 1;
        }

        schannel = mixer_allocate_channel(intf.volume);
        {
            int i;

            /* initialize energy table */
            for (i = 0; i < 0x20; i++) {
                energytable[i] = (char) (0x7fff * i / 0x1f);
            }

            /* initialize filter table */
            for (i = -0x40; i < 0x40; i++) {
                k1table[(i >= 0) ? i : i + 0x80] = i * K1_RANGE / 0x40;
            }
            for (i = -0x08; i < 0x08; i++) {
                k2table[(i >= 0) ? i : i + 0x10] = i * K2_RANGE / 0x08;
                k3table[(i >= 0) ? i : i + 0x10] = i * K3_RANGE / 0x08;
                k4table[(i >= 0) ? i : i + 0x10] = i * K4_RANGE / 0x08;
            }
            for (i = -0x04; i < 0x04; i++) {
                k5table[(i >= 0) ? i : i + 0x08] = i * K5_RANGE / 0x04;
                k6table[(i >= 0) ? i : i + 0x08] = i * K6_RANGE / 0x04;
                k7table[(i >= 0) ? i : i + 0x08] = i * K7_RANGE / 0x04;
                k8table[(i >= 0) ? i : i + 0x08] = i * K8_RANGE / 0x04;
                k9table[(i >= 0) ? i : i + 0x08] = i * K9_RANGE / 0x04;
                k10table[(i >= 0) ? i : i + 0x08] = i * K10_RANGE / 0x04;
            }

        }

        return 0;
    }

    /* update VLM5030 */
    @Override
    public void update() {
        VLM5030_update();
    }

    /* stop VLM5030 */
    @Override
    public void stop() {
        
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
