package gr.codebb.arcadeflex.v037b7.sound;
//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstring.*;
import static arcadeflex.v036.mame.common.*;
import arcadeflex.v036.mame.sndintrf.snd_interface;
import static arcadeflex.v036.mame.sndintrf.sound_name;
import static arcadeflex.v036.mame.mame.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.platform.libc_old.sprintf;
import static gr.codebb.arcadeflex.v036.sound.mixerH.MIXER;
import static gr.codebb.arcadeflex.v036.sound.mixerH.MIXER_PAN_LEFT;
import static gr.codebb.arcadeflex.v036.sound.mixerH.MIXER_PAN_RIGHT;
import arcadeflex.v036.sound.streams.StreamInitMultiPtr;
import static arcadeflex.v036.sound.streams.stream_init_multi;
import static gr.codebb.arcadeflex.v037b7.sound.qsoundH.*;

public class qsound extends snd_interface {

    public qsound() {
        this.sound_num = SOUND_QSOUND;
        this.name = "QSound";
    }

    @Override
    public int chips_num(MachineSound msound) {
        return 0;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((QSound_interface) msound.sound_interface).clock;
    }
    public static final int LENGTH_DIV = 1;
    public static final int QSOUND_CLOCKDIV = 166;/* Clock divider */
    public static final int QSOUND_CHANNELS = 16;

    public static class QSOUND_CHANNEL {

        int bank;
        /* bank (x16)	*/
        int address;
        /* start address */
        int pitch;
        /* pitch */
        int reg3;
        /* unknown (always 0x8000) */
        int loop;
        /* loop address */
        int end;
        /* end address */
        int vol;
        /* master volume */
        int pan;
        /* Pan value */
        int reg9;
        /* unknown */
 /* Work variables */
        int key;
        /* Key on / key off */
        int lvol;
        /* left volume */
        int rvol;
        /* right volume */
        int lastdt;
        /* last sample value */
        int offset;
        /* current offset counter */
    }
    /* Private variables */
    static QSound_interface intf;
    /* Interface  */
    static int qsound_stream;
    /* Audio stream */
    static QSOUND_CHANNEL[] qsound_channel = new QSOUND_CHANNEL[QSOUND_CHANNELS];
    static int qsound_data;
    /* register latch data */
    static BytePtr qsound_sample_rom;
    /* Q sound sample ROM */

    static int[] qsound_pan_table = new int[33];/* Pan volume table */
    static float qsound_frq_ratio;/* Frequency ratio */

    @Override
    public int start(MachineSound msound) {
     	int i;

	if (Machine.sample_rate == 0) return 0;

	intf = (QSound_interface) msound.sound_interface;

	qsound_sample_rom = new BytePtr(memory_region(intf.region));

	for (int _i=0 ; _i<16 ; _i++)
                    qsound_channel[_i] = new QSOUND_CHANNEL();

	qsound_frq_ratio = ((float)intf.clock / (float)QSOUND_CLOCKDIV) /
						(float) Machine.sample_rate;
	qsound_frq_ratio *= 16.0;

	/* Create pan table */
	for (i=0; i<33; i++)
	{
		qsound_pan_table[i]=(int)((256/Math.sqrt(32)) * Math.sqrt(i));
	}
	
		/* Allocate stream */
        int CHANNELS= 2;
        String[] buf=new String[CHANNELS];
			String[] name=new String[CHANNELS];
			int[]  vol=new int[2];
			name[0] = buf[0];
			name[1] = buf[1];
			buf[0]=sprintf( "%s L", sound_name(msound) );
			buf[1]=sprintf( "%s R", sound_name(msound) );
			vol[0]=MIXER(intf.mixing_level[0], MIXER_PAN_LEFT);
			vol[1]=MIXER(intf.mixing_level[1], MIXER_PAN_RIGHT);
		qsound_stream = stream_init_multi(
			CHANNELS,
			name,
			vol,
			Machine.sample_rate,
			0,
			qsound_update );
	
	return 0;
    }

    @Override
    public void stop() {
        if (Machine.sample_rate == 0) {
            return;
        }
    }

    public static WriteHandlerPtr qsound_data_h_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            qsound_data = (qsound_data & 0xff) | (data << 8);
        }
    };
    public static WriteHandlerPtr qsound_data_l_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            qsound_data = (qsound_data & 0xff00) | data;
        }
    };
    public static WriteHandlerPtr qsound_cmd_w = new WriteHandlerPtr() {
        public void handler(int offset, int data) {
            qsound_set_command(data, qsound_data);
        }
    };
    public static ReadHandlerPtr qsound_status_r = new ReadHandlerPtr() {
        public int handler(int offset) {
            /* Port ready bit (0x80 if ready) */
            return 0x80;
        }
    };

    static void qsound_set_command(int data, int value) {
        int ch = 0, reg = 0;
        if (data < 0x80) {
            ch = data >> 3;
            reg = data & 0x07;
        } else {
            if (data < 0x90) {
                ch = data - 0x80;
                reg = 8;
            } else {
                if (data >= 0xba && data < 0xca) {
                    ch = data - 0xba;
                    reg = 9;
                } else {
                    /* Unknown registers */
                    ch = 99;
                    reg = 99;
                }
            }
        }

        switch (reg) {
            case 0:
                /* Bank */
                ch = (ch + 1) & 0x0f;
                /* strange ... */
                qsound_channel[ch].bank = (value & 0x7f) << 16;
                qsound_channel[ch].bank /= LENGTH_DIV;
                break;
            case 1:
                /* start */
                qsound_channel[ch].address = value;
                qsound_channel[ch].address /= LENGTH_DIV;
                break;
            case 2:
                /* pitch */
                qsound_channel[ch].pitch = (int) ((float) value * qsound_frq_ratio);
                qsound_channel[ch].pitch /= LENGTH_DIV;
                if (value == 0) {
                    /* Key off */
                    qsound_channel[ch].key = 0;
                }
                break;
            case 3:
                /* unknown */
                qsound_channel[ch].reg3 = value;
                break;
            case 4:
                /* loop offset */
                qsound_channel[ch].loop = value / LENGTH_DIV;
                break;
            case 5:
                /* end */
                qsound_channel[ch].end = value / LENGTH_DIV;
                break;
            case 6:
                /* master volume */
                if (value == 0) {
                    /* Key off */
                    qsound_channel[ch].key = 0;
                } else if (qsound_channel[ch].key == 0) {
                    /* Key on */
                    qsound_channel[ch].key = 1;
                    qsound_channel[ch].offset = 0;
                    qsound_channel[ch].lastdt = 0;
                }
                qsound_channel[ch].vol = value;
                break;

            case 7:
                /* unused */
                break;
            case 8: {
                int pandata = (value - 0x10) & 0x3f;
                if (pandata > 32) {
                    pandata = 32;
                }
                qsound_channel[ch].rvol = qsound_pan_table[pandata];
                qsound_channel[ch].lvol = qsound_pan_table[32 - pandata];
                qsound_channel[ch].pan = value;
            }
            break;
            case 9:
                qsound_channel[ch].reg9 = value;
                break;
        }
    }

    static StreamInitMultiPtr qsound_update = new StreamInitMultiPtr() {
        @Override
        public void handler(int offset, ShortPtr[] buffer, int length) {
            int i, j;
            int rvol, lvol, count;
            //QSOUND_CHANNEL pC = qsound_channel[0];
            int pc_ptr=0;
            BytePtr pST;
            ShortPtr[] datap=new ShortPtr[2];

            if (Machine.sample_rate == 0) {
                return;
            }

            datap[0] = buffer[0];
            datap[1] = buffer[1];
            memset(datap[0], 0x00, length * 2);
            memset(datap[1], 0x00, length * 2);

            for (i = 0; i < QSOUND_CHANNELS; i++) {
                if (qsound_channel[pc_ptr].key!=0) {
                    ShortPtr pOutL = datap[0];
                    ShortPtr pOutR = datap[1];
                    pST = new BytePtr(qsound_sample_rom , qsound_channel[pc_ptr].bank);
                    rvol = (qsound_channel[pc_ptr].rvol * qsound_channel[pc_ptr].vol) >> (8 * LENGTH_DIV);
                    lvol = (qsound_channel[pc_ptr].lvol * qsound_channel[pc_ptr].vol) >> (8 * LENGTH_DIV);

                    for (j = length - 1; j >= 0; j--) {
                        count = (qsound_channel[pc_ptr].offset) >> 16;
                        qsound_channel[pc_ptr].offset &= 0xffff;
                        if (count!=0) {
                            qsound_channel[pc_ptr].address += count;
                            if (qsound_channel[pc_ptr].address >= qsound_channel[pc_ptr].end) {
                                if (qsound_channel[pc_ptr].loop==0) {
                                    /* Reached the end of a non-looped sample */
                                    qsound_channel[pc_ptr].key = 0;
                                    break;
                                }
                                /* Reached the end, restart the loop */
                                qsound_channel[pc_ptr].address = (qsound_channel[pc_ptr].end - qsound_channel[pc_ptr].loop) & 0xffff;
                            }
                            qsound_channel[pc_ptr].lastdt = pST.read(qsound_channel[pc_ptr].address);
                        }

                        pOutL.write((short)(pOutL.read() + ((qsound_channel[pc_ptr].lastdt * lvol) >> 6)));
                        pOutR.write((short)(pOutR.read() + ((qsound_channel[pc_ptr].lastdt * rvol) >> 6)));
                        pOutL.inc();
                        pOutR.inc();
                        qsound_channel[pc_ptr].offset += qsound_channel[pc_ptr].pitch;
                    }
                }
                pc_ptr++;
            }
        }
    };

    @Override
    public void update() {
        //no fuctionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

}
