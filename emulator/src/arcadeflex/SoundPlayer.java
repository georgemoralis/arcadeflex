/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package arcadeflex;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 *
 * @author shadow
 */
public class SoundPlayer {

    public static final int MAX_BUFFER_SIZE = 128 * 1024;
    //private DynamicSoundEffectInstance soundInstance;
    private byte[] waveBuffer;
    int/*uint*/ stream_buffer_size;
    SourceDataLine m_line;

    public SoundPlayer(int sampleRate, boolean stereo, int framesPerSecond) {
        //soundInstance = new DynamicSoundEffectInstance(sampleRate, stereo ? AudioChannels.Stereo : AudioChannels.Mono);
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                22050,
                16,
                1,
                2,
                22050,
                false);

        
        stream_buffer_size = (int) (((long) MAX_BUFFER_SIZE * (long) sampleRate) / 22050);
        int wBitsPerSample = 16;
        int nChannels = stereo ? 2 : 1;
        int nBlockAlign = wBitsPerSample * nChannels / 8;
        stream_buffer_size = (int) (stream_buffer_size * nBlockAlign) / 4;
        stream_buffer_size = (int) ((stream_buffer_size * 30) / framesPerSecond);
        stream_buffer_size = (stream_buffer_size / 1024) * 1024;

        waveBuffer = new byte[stream_buffer_size];//soundInstance.GetSampleSizeInBytes(TimeSpan.FromMilliseconds(25))];

        System.out.println("frameSize " + format.getFrameSize());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        //DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Unsupported audio: " + format);
            return;
        }

        try {
            m_line = (SourceDataLine) AudioSystem.getLine(info);
            m_line.open(format);
        } catch (LineUnavailableException lue) {
            System.err.println("Unavailable data line");
            return;
        }

        m_line.start();
        //soundInstance.Play();
    }

    public int GetStreamBufferSize() {
        return stream_buffer_size;
    }

    /*public int GetSampleSizeInBytes(TimeSpan duration) {
        return soundInstance.GetSampleSizeInBytes(duration);
    }*/

    public void Play() {
        m_line.start();
    }

    public void Stop() {
        m_line.stop();
    }

    public void WriteSample(int index, short sample) {
            waveBuffer[index] = (byte) (sample&0xFF);
            waveBuffer[index + 1] = (byte) (sample >> 8);
        //waveBuffer[index] = (byte)(sample >> 8);
        //waveBuffer[index + 1] = (byte)(sample & 0xFF);
        //waveBuffer,  bi, (short)((data[p++]*master_volume/256)));
    }

    public void SubmitBuffer(int offset, int length) {
        m_line.write(waveBuffer, offset, length);
    }

}
