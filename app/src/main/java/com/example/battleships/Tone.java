package com.example.battleships;
//Vic: This is forked code!

import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class Tone
{
    // originally from http://marblemice.blogspot.com/2010/04/generate-and-play-tone-in-android.html
    // and modified by Steve Pomeroy <steve@staticfree.info>
    private double duration; // seconds
    private double sampleRate;
    private double numSamples = duration * sampleRate;
    private double sample[];
    private double freqOfTone; // hz

    private byte generatedSnd[];

    public Tone(double pSampleRate, double pFrequency, double pDuration)
    {
        this.sampleRate = pSampleRate;
        this.freqOfTone = pFrequency;
        this.duration = pDuration;
        this.numSamples = this.duration * this.sampleRate;
        this.sample = new double[(int)numSamples];
        this.generatedSnd = new byte[(int)(2 * numSamples)];
        this.generate();
    }

    public void generate(){
        // fill out the array
        for (int i = 0; i < numSamples; ++i) {
            sample[i] = Math.sin(2 * Math.PI * i / (sampleRate/freqOfTone));
        }

        // convert to 16 bit pcm sound array
        // assumes the sample buffer is normalised.
        int idx = 0;
        for (final double dVal : sample) {
            // scale to maximum amplitude
            final short val = (short) ((dVal * 32767));
            // in 16 bit wav PCM, first byte is the low order byte
            generatedSnd[idx++] = (byte) (val & 0x00ff);
            generatedSnd[idx++] = (byte) ((val & 0xff00) >>> 8);

        }
    }

    public void play(boolean soundOn)
    {
        if(soundOn)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                            (int) sampleRate, AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT, generatedSnd.length,
                            AudioTrack.MODE_STATIC);
                    audioTrack.write(generatedSnd, 0, generatedSnd.length);
                    audioTrack.play();
                    try
                    {
                        Thread.sleep((long) (duration * 1000.00) + 100);
                    } catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    audioTrack.stop();
                    audioTrack.flush();
                    audioTrack.release();
                }
            }).start();
        }
    }
}