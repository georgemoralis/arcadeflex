/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gr.codebb.arcadeflex.v036.platform;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

/**
 *
 * @author shadow
 */
public class SoundPlayer {

    public static final int MAX_BUFFER_SIZE = 128 * 1024;
    //private DynamicSoundEffectInstance soundInstance;
    private byte[] waveBuffer;

    //public byte[] toFile;
    int/*uint*/ stream_buffer_size;
    SourceDataLine m_line;

    public SoundPlayer(int sampleRate, int stereo, int framesPerSecond) {
        System.out.println(stereo);
        //soundInstance = new DynamicSoundEffectInstance(sampleRate, stereo ? AudioChannels.Stereo : AudioChannels.Mono);
        AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                22050,
                16,
                (stereo != 0 ? 2 : 1),
                (stereo != 0 ? 4 : 2),
                22050,
                false);

        stream_buffer_size = (int) (((long) MAX_BUFFER_SIZE * (long) sampleRate) / 22050);
        int wBitsPerSample = 16;
        int nChannels = stereo != 0 ? 2 : 1;
        int nBlockAlign = wBitsPerSample * nChannels / 8;
        stream_buffer_size = (int) (stream_buffer_size * nBlockAlign) / 4;
        stream_buffer_size = (int) ((stream_buffer_size * 30) / framesPerSecond);
        stream_buffer_size = (stream_buffer_size / 1024) * 1024;

        waveBuffer = new byte[stream_buffer_size];//soundInstance.GetSampleSizeInBytes(TimeSpan.FromMilliseconds(25))];

        //toFile = new byte[0];
        System.out.println("frameSize " + format.getFrameSize());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        //DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        if (!AudioSystem.isLineSupported(info)) {
            System.err.println("Unsupported audio: " + format);
            return;
        }

        try {
            m_line = (SourceDataLine) AudioSystem.getLine(info);
            if (MainStream.inst == null) {
                m_line.open(format);
            }
        } catch (LineUnavailableException lue) {
            System.err.println("Unavailable data line");
            return;
        }

        if (MainStream.inst == null) {
            m_line.start();
        }
        //soundInstance.Play();
    }

    public int GetStreamBufferSize() {
        return stream_buffer_size;
    }

    /*public int GetSampleSizeInBytes(TimeSpan duration) {
        return soundInstance.GetSampleSizeInBytes(duration);
    }*/
    public void Play() {
        if (MainStream.inst == null) {
            m_line.start();
        }
    }

    public void Stop() {
        if (MainStream.inst == null) {
            m_line.stop();
        }
    }

    public void WriteSample(int index, short sample) {
        waveBuffer[index] = (byte) (sample & 0xFF);
        waveBuffer[index + 1] = (byte) (sample >> 8);

        //waveBuffer[index] = (byte)(sample >> 8);
        //waveBuffer[index + 1] = (byte)(sample & 0xFF);
        //waveBuffer,  bi, (short)((data[p++]*master_volume/256)));
    }

    public void SubmitBuffer(int offset, int length) {
        if (waveBuffer != null) {
            if (MainStream.inst == null) {
                m_line.write(waveBuffer, offset, length);
            } else {
                MainStream.inst.callback.broadcastAudio(waveBuffer, offset, length);
            }
        }
    }

    /*
    public void save_old(String filename, int offset, int length) {
        byte[] newf = new byte[toFile.length + length];

        for (int i = 0; i < toFile.length; i++) {
            newf[i] = toFile[i];
        }
        int dst = 0;
        for (int i = offset; i < length; i++) {
            newf[toFile.length + dst] = waveBuffer[i];
            dst++;
        }
        // now save the file
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(newf);
            AudioInputStream ais = new AudioInputStream(bais, format, newf.length / format.getFrameSize());

            if (filename.endsWith(".wav") || filename.endsWith(".WAV")) {
                AudioSystem.write(ais, AudioFileFormat.Type.WAVE, new File(filename));
            } else if (filename.endsWith(".au") || filename.endsWith(".AU")) {
                AudioSystem.write(ais, AudioFileFormat.Type.AU, new File(filename));
            } else {
                throw new IllegalArgumentException("unsupported audio format: '" + filename + "'");
            }
        } catch (IOException ioe) {
            throw new IllegalArgumentException("unable to save file '" + filename + "'", ioe);
        }
        toFile = new byte[newf.length];
        for (int i = 0; i < toFile.length; i++) {
            toFile[i] = newf[i];
        }
        System.out.println("sp: " + toFile.length);
    }
     */
}
