/*
 *  this file should be fully compatible for 0.36
 */
package gr.codebb.arcadeflex.v036.sound;

public class mixerH 
{
    public static final int MIXER_MAX_CHANNELS =16;

    /*
      When you allocate a channel, you pass a default mixing level setting.
      The mixing level is in the range 0-100, and is passed down to the OS dependant
      code. A channel playing at 100% has full access to the complete dynamic range
      of the sound card. When more than one channel is playing, clipping may occur
      so the levels have to decreased to avoid that, and adjusted to get the correct
      balance.

      By default, channels play on both speakers. They can also be directed to only
      one speaker. Note that in that case the sound will be perceived by the
      listener at half intensity, since it is coming from only one speaker.
      Use the MIXER() macro to select which speaker the channel should go to. E.g.
      mixer_allocate_channel(MIXER(50,MIXER_PAN_LEFT));

      The MIXER() macro uses 16 bits because the YM3012_VOL() macro stuffs two
      MIXER() values for left and right channel into a long.
    */
    public static final int MIXER_PAN_CENTER  =0;
    public static final int MIXER_PAN_LEFT    =1;
    public static final int MIXER_PAN_RIGHT   =2;
    public static final int MIXER(int level,int pan){ return ((level & 0xff) | ((pan & 0x03) << 8));}

    public static final int MIXER_GAIN_1x  =0;
    public static final int MIXER_GAIN_2x  =1;
    public static final int MIXER_GAIN_4x  =2;
    public static final int MIXER_GAIN_8x  =3;
    public static final int MIXERG(int level,int gain,int pan){ return ((level & 0xff) | ((gain & 0x03) << 10) | ((pan & 0x03) << 8)); }

    public static final int MIXER_GET_LEVEL(int mixing_level) { return ((mixing_level) & 0xff); }
    public static final int MIXER_GET_PAN(int mixing_level)   { return (((mixing_level) >> 8) & 0x03); }
    public static final int MIXER_GET_GAIN(int mixing_level)  { return (((mixing_level) >> 10) & 0x03); }
}
