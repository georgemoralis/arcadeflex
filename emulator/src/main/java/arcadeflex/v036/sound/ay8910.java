/*
 * ported to v0.36
 *
 */
package arcadeflex.v036.sound;

//generic imports
import static arcadeflex.v036.generic.funcPtr.*;
import static gr.codebb.arcadeflex.common.PtrLib.*;
import static common.libc.cstdio.*;
import static gr.codebb.arcadeflex.v036.mame.mame.Machine;
import static gr.codebb.arcadeflex.v036.mame.sndintrf.*;
import static arcadeflex.v036.mame.sndintrfH.*;
import static gr.codebb.arcadeflex.v036.mame.mame.errorlog;
import static gr.codebb.arcadeflex.v036.platform.libc_old.fprintf;
import static arcadeflex.v036.sound.ay8910H.*;
import static gr.codebb.arcadeflex.v037b7.mame.cpuintrf.*;
import static gr.codebb.arcadeflex.v036.sound.mixer.*;
import static arcadeflex.v036.sound.streams.*;

public class ay8910 extends snd_interface {

    public ay8910() {
        sound_num = SOUND_AY8910;
        name = "AY-8910";
        for (int i = 0; i < MAX_8910; i++) {
            AYPSG[i] = new AY8910();
        }
    }

    @Override
    public int chips_num(MachineSound msound) {
        return ((AY8910interface) msound.sound_interface).num;
    }

    @Override
    public int chips_clock(MachineSound msound) {
        return ((AY8910interface) msound.sound_interface).baseclock;
    }

    @Override
    public void stop() {
        //no functionality expected
    }

    @Override
    public void update() {
        //no functionality expected
    }

    @Override
    public void reset() {
        //no functionality expected
    }

    public static final int MAX_OUTPUT = 0x7fff;

    public static final int STEP = 0x8000;

    public static class AY8910 {

        int Channel;
        int SampleRate;
        ReadHandlerPtr PortAread;
        ReadHandlerPtr PortBread;
        WriteHandlerPtr PortAwrite;
        WriteHandlerPtr PortBwrite;
        int register_latch;
        int/*unsigned char*/ u8_Regs[] = new int[16];
        long/*unsigned int*/ u32_UpdateStep;
        int PeriodA, PeriodB, PeriodC, PeriodN, PeriodE;
        int CountA, CountB, CountC, CountN, CountE;
        long/*unsigned int*/ u32_VolA, u32_VolB, u32_VolC, u32_VolE;
        int/*unsigned char*/ u8_EnvelopeA, u8_EnvelopeB, u8_EnvelopeC;
        int/*unsigned char*/ u8_OutputA, u8_OutputB, u8_OutputC, u8_OutputN;
        byte CountEnv;
        int/*unsigned char*/ u8_Hold, u8_Alternate, u8_Attack, u8_Holding;
        int RNG;
        long/*unsigned int*/ u32_VolTable[] = new long[32];
    }
    /* register id's */
    public static final int AY_AFINE = 0;
    public static final int AY_ACOARSE = 1;
    public static final int AY_BFINE = 2;
    public static final int AY_BCOARSE = 3;
    public static final int AY_CFINE = 4;
    public static final int AY_CCOARSE = 5;
    public static final int AY_NOISEPER = 6;
    public static final int AY_ENABLE = 7;
    public static final int AY_AVOL = 8;
    public static final int AY_BVOL = 9;
    public static final int AY_CVOL = 10;
    public static final int AY_EFINE = 11;
    public static final int AY_ECOARSE = 12;
    public static final int AY_ESHAPE = 13;

    public static final int AY_PORTA = 14;
    public static final int AY_PORTB = 15;

    static AY8910[] AYPSG = new AY8910[MAX_8910];

    /* array of PSG's */
    public static void _AYWriteReg(int n, int r, int v) {
        AY8910 PSG = AYPSG[n];
        int old;

        PSG.u8_Regs[r] = v & 0xFF;

        /* A note about the period of tones, noise and envelope: for speed reasons,*/
 /* we count down from the period to 0, but careful studies of the chip     */
 /* output prove that it instead counts up from 0 until the counter becomes */
 /* greater or equal to the period. This is an important difference when the*/
 /* program is rapidly changing the period to modulate the sound.           */
 /* To compensate for the difference, when the period is changed we adjust  */
 /* our internal counter.                                                   */
 /* Also, note that period = 0 is the same as period = 1. This is mentioned */
 /* in the YM2203 data sheets. However, this does NOT apply to the Envelope */
 /* period. In that case, period = 0 is half as period = 1. */
        switch (r) {
            case AY_AFINE:
            case AY_ACOARSE:
                PSG.u8_Regs[AY_ACOARSE] &= 0x0f;
                old = PSG.PeriodA;
                PSG.PeriodA = (int) ((PSG.u8_Regs[AY_AFINE] + 256 * PSG.u8_Regs[AY_ACOARSE]) * PSG.u32_UpdateStep);
                if (PSG.PeriodA == 0) {
                    PSG.PeriodA = (int) PSG.u32_UpdateStep;
                }
                PSG.CountA += PSG.PeriodA - old;
                if (PSG.CountA <= 0) {
                    PSG.CountA = 1;
                }
                break;
            case AY_BFINE:
            case AY_BCOARSE:
                PSG.u8_Regs[AY_BCOARSE] &= 0x0f;
                old = PSG.PeriodB;
                PSG.PeriodB = (int) ((PSG.u8_Regs[AY_BFINE] + 256 * PSG.u8_Regs[AY_BCOARSE]) * PSG.u32_UpdateStep);
                if (PSG.PeriodB == 0) {
                    PSG.PeriodB = (int) PSG.u32_UpdateStep;
                }
                PSG.CountB += PSG.PeriodB - old;
                if (PSG.CountB <= 0) {
                    PSG.CountB = 1;
                }
                break;
            case AY_CFINE:
            case AY_CCOARSE:
                PSG.u8_Regs[AY_CCOARSE] &= 0x0f;
                old = PSG.PeriodC;
                PSG.PeriodC = (int) ((PSG.u8_Regs[AY_CFINE] + 256 * PSG.u8_Regs[AY_CCOARSE]) * PSG.u32_UpdateStep);
                if (PSG.PeriodC == 0) {
                    PSG.PeriodC = (int) PSG.u32_UpdateStep;
                }
                PSG.CountC += PSG.PeriodC - old;
                if (PSG.CountC <= 0) {
                    PSG.CountC = 1;
                }
                break;
            case AY_NOISEPER:
                PSG.u8_Regs[AY_NOISEPER] &= 0x1f;
                old = PSG.PeriodN;
                PSG.PeriodN = (int) (PSG.u8_Regs[AY_NOISEPER] * PSG.u32_UpdateStep);
                if (PSG.PeriodN == 0) {
                    PSG.PeriodN = (int) PSG.u32_UpdateStep;
                }
                PSG.CountN += PSG.PeriodN - old;
                if (PSG.CountN <= 0) {
                    PSG.CountN = 1;
                }
                break;
            case AY_AVOL:
                PSG.u8_Regs[AY_AVOL] &= 0x1f;
                PSG.u8_EnvelopeA = PSG.u8_Regs[AY_AVOL] & 0x10;
                PSG.u32_VolA = PSG.u8_EnvelopeA != 0 ? PSG.u32_VolE : PSG.u32_VolTable[PSG.u8_Regs[AY_AVOL] != 0 ? PSG.u8_Regs[AY_AVOL] * 2 + 1 : 0];
                break;
            case AY_BVOL:
                PSG.u8_Regs[AY_BVOL] &= 0x1f;
                PSG.u8_EnvelopeB = PSG.u8_Regs[AY_BVOL] & 0x10;
                PSG.u32_VolB = PSG.u8_EnvelopeB != 0 ? PSG.u32_VolE : PSG.u32_VolTable[PSG.u8_Regs[AY_BVOL] != 0 ? PSG.u8_Regs[AY_BVOL] * 2 + 1 : 0];
                break;
            case AY_CVOL:
                PSG.u8_Regs[AY_CVOL] &= 0x1f;
                PSG.u8_EnvelopeC = PSG.u8_Regs[AY_CVOL] & 0x10;
                PSG.u32_VolC = PSG.u8_EnvelopeC != 0 ? PSG.u32_VolE : PSG.u32_VolTable[PSG.u8_Regs[AY_CVOL] != 0 ? PSG.u8_Regs[AY_CVOL] * 2 + 1 : 0];
                break;
            case AY_EFINE:
            case AY_ECOARSE:
                old = PSG.PeriodE;
                PSG.PeriodE = (int) (((PSG.u8_Regs[AY_EFINE] + 256 * PSG.u8_Regs[AY_ECOARSE])) * PSG.u32_UpdateStep);
                if (PSG.PeriodE == 0) {
                    PSG.PeriodE = (int) (PSG.u32_UpdateStep / 2);
                }
                PSG.CountE += PSG.PeriodE - old;
                if (PSG.CountE <= 0) {
                    PSG.CountE = 1;
                }
                break;
            case AY_ESHAPE:
                /* envelope shapes:
		C AtAlH
		0 0 x x  \___

		0 1 x x  /___

		1 0 0 0  \\\\

		1 0 0 1  \___

		1 0 1 0  \/\/
		          ___
		1 0 1 1  \

		1 1 0 0  ////
		          ___
		1 1 0 1  /

		1 1 1 0  /\/\

		1 1 1 1  /___

		The envelope counter on the AY-3-8910 has 16 steps. On the YM2149 it
		has twice the steps, happening twice as fast. Since the end result is
		just a smoother curve, we always use the YM2149 behaviour.
                 */
                PSG.u8_Regs[AY_ESHAPE] &= 0x0f;
                PSG.u8_Attack = (PSG.u8_Regs[AY_ESHAPE] & 0x04) != 0 ? 0x1f : 0x00;
                if ((PSG.u8_Regs[AY_ESHAPE] & 0x08) == 0) {
                    /* if Continue = 0, map the shape to the equivalent one which has Continue = 1 */
                    PSG.u8_Hold = 1;
                    PSG.u8_Alternate = PSG.u8_Attack;
                } else {
                    PSG.u8_Hold = PSG.u8_Regs[AY_ESHAPE] & 0x01;
                    PSG.u8_Alternate = PSG.u8_Regs[AY_ESHAPE] & 0x02;
                }
                PSG.CountE = PSG.PeriodE;
                PSG.CountEnv = 0x1f;
                PSG.u8_Holding = 0;
                PSG.u32_VolE = PSG.u32_VolTable[PSG.CountEnv ^ PSG.u8_Attack];
                if (PSG.u8_EnvelopeA != 0) {
                    PSG.u32_VolA = PSG.u32_VolE;
                }
                if (PSG.u8_EnvelopeB != 0) {
                    PSG.u32_VolB = PSG.u32_VolE;
                }
                if (PSG.u8_EnvelopeC != 0) {
                    PSG.u32_VolC = PSG.u32_VolE;
                }
                break;
            case AY_PORTA:
                if ((PSG.u8_Regs[AY_ENABLE] & 0x40) == 0) {
                    if (errorlog != null) {
                        fprintf(errorlog, "warning: write to 8910 #%d Port A set as input\n", n);
                    }
                }
                if (PSG.PortAwrite != null) {
                    PSG.PortAwrite.handler(0, v);
                } else {
                    if (errorlog != null) {
                        fprintf(errorlog, "PC %04x: warning - write %02x to 8910 #%d Port A\n", cpu_get_pc(), v, n);
                    }
                }
                break;
            case AY_PORTB:
                if ((PSG.u8_Regs[AY_ENABLE] & 0x80) == 0) {
                    if (errorlog != null) {
                        fprintf(errorlog, "warning: write to 8910 #%d Port B set as input\n", n);
                    }
                }
                if (PSG.PortBwrite != null) {
                    PSG.PortBwrite.handler(0, v);
                } else {
                    if (errorlog != null) {
                        fprintf(errorlog, "PC %04x: warning - write %02x to 8910 #%d Port B\n", cpu_get_pc(), v, n);
                    }
                }
                break;
        }
    }

    /* write a register on AY8910 chip number 'n' */
    public static void AYWriteReg(int chip, int r, int v) {
        AY8910 PSG = AYPSG[chip];

        if (r > 15) {
            return;
        }
        if (r < 14) {
            if (r == AY_ESHAPE || PSG.u8_Regs[r] != v) {
                /* update the output buffer before changing the register */
                stream_update(PSG.Channel, 0);
            }
        }

        _AYWriteReg(chip, r, v);
    }

    public static int/*unsigned char*/ AYReadReg(int n, int r) {
        AY8910 PSG = AYPSG[n];

        if (r > 15) {
            return 0;
        }

        switch (r) {
            case AY_PORTA:
                if ((PSG.u8_Regs[AY_ENABLE] & 0x40) != 0) {
                    if (errorlog != null) {
                        fprintf(errorlog, "warning: read from 8910 #%d Port A set as output\n", n);
                    }
                }
                if (PSG.PortAread != null) {
                    PSG.u8_Regs[AY_PORTA] = PSG.PortAread.handler(0) & 0xFF;
                } else {
                    if (errorlog != null) {
                        fprintf(errorlog, "PC %04x: warning - read 8910 #%d Port A\n", cpu_get_pc(), n);
                    }
                }
                break;
            case AY_PORTB:
                if ((PSG.u8_Regs[AY_ENABLE] & 0x80) != 0) {
                    if (errorlog != null) {
                        fprintf(errorlog, "warning: read from 8910 #%d Port B set as output\n", n);
                    }
                }
                if (PSG.PortBread != null) {
                    PSG.u8_Regs[AY_PORTB] = PSG.PortBread.handler(0) & 0xFF;
                } else {
                    if (errorlog != null) {
                        fprintf(errorlog, "PC %04x: warning - read 8910 #%d Port B\n", cpu_get_pc(), n);
                    }
                }
                break;
        }
        return PSG.u8_Regs[r] & 0xFF;
    }

    public static void AY8910Write(int chip, int a, int data) {
        AY8910 PSG = AYPSG[chip];

        if ((a & 1) != 0) {
            /* Data port */
            AYWriteReg(chip, PSG.register_latch, data);
        } else {
            /* Register port */
            PSG.register_latch = data & 0x0f;
        }
    }

    public static int AY8910Read(int chip) {
        AY8910 PSG = AYPSG[chip];

        return AYReadReg(chip, PSG.register_latch);
    }

    /* AY8910 interface */
    public static ReadHandlerPtr AY8910_read_port_0_r = (int offset) -> AY8910Read(0);
    public static ReadHandlerPtr AY8910_read_port_1_r = (int offset) -> AY8910Read(1);
    public static ReadHandlerPtr AY8910_read_port_2_r = (int offset) -> AY8910Read(2);
    public static ReadHandlerPtr AY8910_read_port_3_r = (int offset) -> AY8910Read(3);
    public static ReadHandlerPtr AY8910_read_port_4_r = (int offset) -> AY8910Read(4);

    public static WriteHandlerPtr AY8910_control_port_0_w = (int offset, int data) -> {
        AY8910Write(0, 0, data);
    };
    public static WriteHandlerPtr AY8910_control_port_1_w = (int offset, int data) -> {
        AY8910Write(1, 0, data);
    };
    public static WriteHandlerPtr AY8910_control_port_2_w = (int offset, int data) -> {
        AY8910Write(2, 0, data);
    };
    public static WriteHandlerPtr AY8910_control_port_3_w = (int offset, int data) -> {
        AY8910Write(3, 0, data);
    };
    public static WriteHandlerPtr AY8910_control_port_4_w = (int offset, int data) -> {
        AY8910Write(4, 0, data);
    };
    public static WriteHandlerPtr AY8910_write_port_0_w = (int offset, int data) -> {
        AY8910Write(0, 1, data);
    };
    public static WriteHandlerPtr AY8910_write_port_1_w = (int offset, int data) -> {
        AY8910Write(1, 1, data);
    };
    public static WriteHandlerPtr AY8910_write_port_2_w = (int offset, int data) -> {
        AY8910Write(2, 1, data);
    };
    public static WriteHandlerPtr AY8910_write_port_3_w = (int offset, int data) -> {
        AY8910Write(3, 1, data);
    };
    public static WriteHandlerPtr AY8910_write_port_4_w = (int offset, int data) -> {
        AY8910Write(4, 1, data);
    };
    public static StreamInitMultiPtr AY8910Update = new StreamInitMultiPtr() {
        public void handler(int chip, ShortPtr[] buffer, int length) {
            AY8910 PSG = AYPSG[chip];
            ShortPtr buf1, buf2, buf3;
            int outn;

            buf1 = buffer[0];
            buf2 = buffer[1];
            buf3 = buffer[2];


            /* The 8910 has three outputs, each output is the mix of one of the three */
 /* tone generators and of the (single) noise generator. The two are mixed */
 /* BEFORE going into the DAC. The formula to mix each channel is: */
 /* (ToneOn | ToneDisable) & (NoiseOn | NoiseDisable). */
 /* Note that this means that if both tone and noise are disabled, the output */
 /* is 1, not 0, and can be modulated changing the volume. */
 /* If the channels are disabled, set their output to 1, and increase the */
 /* counter, if necessary, so they will not be inverted during this update. */
 /* Setting the output to 1 is necessary because a disabled channel is locked */
 /* into the ON state (see above); and it has no effect if the volume is 0. */
 /* If the volume is 0, increase the counter, but don't touch the output. */
            if ((PSG.u8_Regs[AY_ENABLE] & 0x01) != 0) {
                if (PSG.CountA <= length * STEP) {
                    PSG.CountA += length * STEP;
                }
                PSG.u8_OutputA = 1;
            } else if (PSG.u8_Regs[AY_AVOL] == 0) {
                /* note that I do count += length, NOT count = length + 1. You might think */
 /* it's the same since the volume is 0, but doing the latter could cause */
 /* interferencies when the program is rapidly modulating the volume. */
                if (PSG.CountA <= length * STEP) {
                    PSG.CountA += length * STEP;
                }
            }
            if ((PSG.u8_Regs[AY_ENABLE] & 0x02) != 0) {
                if (PSG.CountB <= length * STEP) {
                    PSG.CountB += length * STEP;
                }
                PSG.u8_OutputB = 1;
            } else if (PSG.u8_Regs[AY_BVOL] == 0) {
                if (PSG.CountB <= length * STEP) {
                    PSG.CountB += length * STEP;
                }
            }
            if ((PSG.u8_Regs[AY_ENABLE] & 0x04) != 0) {
                if (PSG.CountC <= length * STEP) {
                    PSG.CountC += length * STEP;
                }
                PSG.u8_OutputC = 1;
            } else if (PSG.u8_Regs[AY_CVOL] == 0) {
                if (PSG.CountC <= length * STEP) {
                    PSG.CountC += length * STEP;
                }
            }

            /* for the noise channel we must not touch OutputN - it's also not necessary */
 /* since we use outn. */
            if ((PSG.u8_Regs[AY_ENABLE] & 0x38) == 0x38) /* all off */ {
                if (PSG.CountN <= length * STEP) {
                    PSG.CountN += length * STEP;
                }
            }

            outn = (PSG.u8_OutputN | PSG.u8_Regs[AY_ENABLE]);


            /* buffering loop */
            while (length != 0) {
                int vola, volb, volc;
                int left;


                /* vola, volb and volc keep track of how long each square wave stays */
 /* in the 1 position during the sample period. */
                vola = volb = volc = 0;

                left = STEP;
                do {
                    int nextevent;

                    if (PSG.CountN < left) {
                        nextevent = PSG.CountN;
                    } else {
                        nextevent = left;
                    }

                    if ((outn & 0x08) != 0) {
                        if (PSG.u8_OutputA != 0) {
                            vola += PSG.CountA;
                        }
                        PSG.CountA -= nextevent;
                        /* PeriodA is the half period of the square wave. Here, in each */
 /* loop I add PeriodA twice, so that at the end of the loop the */
 /* square wave is in the same status (0 or 1) it was at the start. */
 /* vola is also incremented by PeriodA, since the wave has been 1 */
 /* exactly half of the time, regardless of the initial position. */
 /* If we exit the loop in the middle, OutputA has to be inverted */
 /* and vola incremented only if the exit status of the square */
 /* wave is 1. */
                        while (PSG.CountA <= 0) {
                            PSG.CountA += PSG.PeriodA;
                            if (PSG.CountA > 0) {
                                PSG.u8_OutputA = (PSG.u8_OutputA ^ 1) & 0xFF;
                                if (PSG.u8_OutputA != 0) {
                                    vola += PSG.PeriodA;
                                }
                                break;
                            }
                            PSG.CountA += PSG.PeriodA;
                            vola += PSG.PeriodA;
                        }
                        if (PSG.u8_OutputA != 0) {
                            vola -= PSG.CountA;
                        }
                    } else {
                        PSG.CountA -= nextevent;
                        while (PSG.CountA <= 0) {
                            PSG.CountA += PSG.PeriodA;
                            if (PSG.CountA > 0) {
                                PSG.u8_OutputA = (PSG.u8_OutputA ^ 1) & 0xFF;
                                break;
                            }
                            PSG.CountA += PSG.PeriodA;
                        }
                    }

                    if ((outn & 0x10) != 0) {
                        if (PSG.u8_OutputB != 0) {
                            volb += PSG.CountB;
                        }
                        PSG.CountB -= nextevent;
                        while (PSG.CountB <= 0) {
                            PSG.CountB += PSG.PeriodB;
                            if (PSG.CountB > 0) {
                                PSG.u8_OutputB = (PSG.u8_OutputB ^ 1) & 0xFF;
                                if (PSG.u8_OutputB != 0) {
                                    volb += PSG.PeriodB;
                                }
                                break;
                            }
                            PSG.CountB += PSG.PeriodB;
                            volb += PSG.PeriodB;
                        }
                        if (PSG.u8_OutputB != 0) {
                            volb -= PSG.CountB;
                        }
                    } else {
                        PSG.CountB -= nextevent;
                        while (PSG.CountB <= 0) {
                            PSG.CountB += PSG.PeriodB;
                            if (PSG.CountB > 0) {
                                PSG.u8_OutputB = (PSG.u8_OutputB ^ 1) & 0xFF;
                                break;
                            }
                            PSG.CountB += PSG.PeriodB;
                        }
                    }

                    if ((outn & 0x20) != 0) {
                        if (PSG.u8_OutputC != 0) {
                            volc += PSG.CountC;
                        }
                        PSG.CountC -= nextevent;
                        while (PSG.CountC <= 0) {
                            PSG.CountC += PSG.PeriodC;
                            if (PSG.CountC > 0) {
                                PSG.u8_OutputC = (PSG.u8_OutputC ^ 1) & 0xFF;
                                if (PSG.u8_OutputC != 0) {
                                    volc += PSG.PeriodC;
                                }
                                break;
                            }
                            PSG.CountC += PSG.PeriodC;
                            volc += PSG.PeriodC;
                        }
                        if (PSG.u8_OutputC != 0) {
                            volc -= PSG.CountC;
                        }
                    } else {
                        PSG.CountC -= nextevent;
                        while (PSG.CountC <= 0) {
                            PSG.CountC += PSG.PeriodC;
                            if (PSG.CountC > 0) {
                                PSG.u8_OutputC = (PSG.u8_OutputC ^ 1) & 0xFF;
                                break;
                            }
                            PSG.CountC += PSG.PeriodC;
                        }
                    }

                    PSG.CountN -= nextevent;
                    if (PSG.CountN <= 0) {
                        /* Is noise output going to change? */
                        if (((PSG.RNG + 1) & 2) != 0) /* (bit0^bit1)? */ {
                            PSG.u8_OutputN = (~PSG.u8_OutputN) & 0xFF;
                            outn = (PSG.u8_OutputN | PSG.u8_Regs[AY_ENABLE]);
                        }

                        /* The Random Number Generator of the 8910 is a 17-bit shift */
 /* register. The input to the shift register is bit0 XOR bit2 */
 /* (bit0 is the output). */

 /* The following is a fast way to compute bit 17 = bit0^bit2. */
 /* Instead of doing all the logic operations, we only check */
 /* bit 0, relying on the fact that after two shifts of the */
 /* register, what now is bit 2 will become bit 0, and will */
 /* invert, if necessary, bit 16, which previously was bit 18. */
                        if ((PSG.RNG & 1) != 0) {
                            PSG.RNG ^= 0x28000;
                        }
                        PSG.RNG >>= 1;
                        PSG.CountN += PSG.PeriodN;
                    }

                    left -= nextevent;
                } while (left > 0);

                /* update envelope */
                if (PSG.u8_Holding == 0) {
                    PSG.CountE -= STEP;
                    if (PSG.CountE <= 0) {
                        do {
                            PSG.CountEnv--;
                            PSG.CountE += PSG.PeriodE;
                        } while (PSG.CountE <= 0);

                        /* check envelope current position */
                        if (PSG.CountEnv < 0) {
                            if (PSG.u8_Hold != 0) {
                                if (PSG.u8_Alternate != 0) {
                                    PSG.u8_Attack = (PSG.u8_Attack ^ 0x1f) & 0xFF;
                                }
                                PSG.u8_Holding = 1;
                                PSG.CountEnv = 0;
                            } else {
                                /* if CountEnv has looped an odd number of times (usually 1), */
 /* invert the output. */
                                if (PSG.u8_Alternate != 0 && (PSG.CountEnv & 0x20) != 0) {
                                    PSG.u8_Attack = (PSG.u8_Attack ^ 0x1f) & 0xFF;
                                };

                                PSG.CountEnv &= 0x1f;
                            }
                        }

                        PSG.u32_VolE = PSG.u32_VolTable[PSG.CountEnv ^ PSG.u8_Attack];
                        /* reload volume */
                        if (PSG.u8_EnvelopeA != 0) {
                            PSG.u32_VolA = PSG.u32_VolE;
                        }
                        if (PSG.u8_EnvelopeB != 0) {
                            PSG.u32_VolB = PSG.u32_VolE;
                        }
                        if (PSG.u8_EnvelopeC != 0) {
                            PSG.u32_VolC = PSG.u32_VolE;
                        }
                    }
                }
                buf1.writeinc((short) ((vola * PSG.u32_VolA) / STEP));
                buf2.writeinc((short) ((volb * PSG.u32_VolB) / STEP));
                buf3.writeinc((short) ((volc * PSG.u32_VolC) / STEP));

                length--;
            }
        }
    };

    public static void AY8910_set_clock(int chip, int clock) {
        AY8910 PSG = AYPSG[chip];

        /* the step clock for the tone and noise generators is the chip clock    */
 /* divided by 8; for the envelope generator of the AY-3-8910, it is half */
 /* that much (clock/16), but the envelope of the YM2149 goes twice as    */
 /* fast, therefore again clock/8.                                        */
 /* Here we calculate the number of steps which happen during one sample  */
 /* at the given sample rate. No. of events = sample rate / (clock/8).    */
 /* STEP is a multiplier used to turn the fraction into a fixed point     */
 /* number.                                                               */
        PSG.u32_UpdateStep = ((long) ((double) STEP * PSG.SampleRate * 8) / clock) & 0xFFFFFFFFL;
    }

    public static void AY8910_set_volume(int chip, int channel, int volume) {
        AY8910 PSG = AYPSG[chip];
        int ch;

        for (ch = 0; ch < 3; ch++) {
            if (channel == ch || channel == ALL_8910_CHANNELS) {
                mixer_set_volume(PSG.Channel + ch, volume);
            }
        }
    }

    public static void build_mixer_table(int chip) {
        AY8910 PSG = AYPSG[chip];
        int i;
        double out;


        /* calculate the volume->voltage conversion table */
 /* The AY-3-8910 has 16 levels, in a logarithmic scale (3dB per step) */
 /* The YM2149 still has 16 levels for the tone generators, but 32 for */
 /* the envelope generator (1.5dB per step). */
        out = MAX_OUTPUT;
        for (i = 31; i > 0; i--) {
            PSG.u32_VolTable[i] = (int) (out + 0.5);
            /* round to nearest */

            out /= 1.188502227;
            /* = 10 ^ (1.5/20) = 1.5dB */
        }
        PSG.u32_VolTable[0] = 0;
    }

    public static void AY8910_reset(int chip) {
        int i;
        AY8910 PSG = AYPSG[chip];

        PSG.register_latch = 0;
        PSG.RNG = 1;
        PSG.u8_OutputA = 0;
        PSG.u8_OutputB = 0;
        PSG.u8_OutputC = 0;
        PSG.u8_OutputN = 0xff;
        for (i = 0; i < AY_PORTA; i++) {
            _AYWriteReg(chip, i, 0);
            /* AYWriteReg() uses the timer system; we cannot */
        }
        /* call it at this time because the timer system */
 /* has not been initialized. */
    }

    static int AY8910_init(MachineSound msound, int chip,
            int clock, int volume, int sample_rate,
            ReadHandlerPtr portAread, ReadHandlerPtr portBread,
            WriteHandlerPtr portAwrite, WriteHandlerPtr portBwrite) {

        AY8910 PSG = AYPSG[chip];
        String[] name = new String[3];
        int[] vol = new int[3];
        //memset(PSG,0,sizeof(struct AY8910));
        PSG.SampleRate = sample_rate;
        PSG.PortAread = portAread;
        PSG.PortBread = portBread;
        PSG.PortAwrite = portAwrite;
        PSG.PortBwrite = portBwrite;
        for (int i = 0; i < 3; i++) {
            vol[i] = volume;
            name[i] = sprintf("%s #%d Ch %c", sound_name(msound), chip, 'A' + i);
        }
        PSG.Channel = stream_init_multi(3, name, vol, sample_rate, chip, AY8910Update);

        if (PSG.Channel == -1) {
            return 1;
        }

        AY8910_set_clock(chip, clock);
        AY8910_reset(chip);
        return 0;
    }

    @Override
    public int start(MachineSound msound) {
        int chip;
        AY8910interface intf = (AY8910interface) msound.sound_interface;

        for (chip = 0; chip < intf.num; chip++) {
            if (AY8910_init(msound, chip, intf.baseclock,
                    intf.mixing_level[chip] & 0xffff,
                    Machine.sample_rate,
                    intf.portAread[chip], intf.portBread[chip],
                    intf.portAwrite[chip], intf.portBwrite[chip]) != 0) {
                return 1;
            }
            build_mixer_table(chip);
        }
        return 0;
    }
}
