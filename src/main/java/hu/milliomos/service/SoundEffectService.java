package hu.milliomos.service;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundEffectService {
    private static final float SAMPLE_RATE = 44100f;
    private boolean muted = false;

    private final ExecutorService soundExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "studio-sound-thread");
        t.setDaemon(true);
        return t;
    });

    public void playTick() {
        playSequence(new int[]{880}, new int[]{35}, 0.18);
    }

    public void playSelect() {
        playSequence(new int[]{440, 554}, new int[]{90, 140}, 0.45);
    }

    public void playCorrect() {
        playSequence(new int[]{523, 659, 784}, new int[]{110, 110, 260}, 0.55);
    }

    public void playWrong() {
        playSequence(new int[]{311, 233}, new int[]{220, 420}, 0.60);
    }

    public void playLifeline() {
        playSequence(new int[]{659, 880}, new int[]{80, 140}, 0.45);
    }

    public void playWalkAway() {
        playSequence(new int[]{392, 494, 587}, new int[]{110, 110, 280}, 0.55);
    }

    public void playVictory() {
        playSequence(new int[]{523, 659, 784, 1046}, new int[]{140, 140, 140, 450}, 0.65);
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    private void playSequence(int[] frequencies, int[] durationsMs, double volume) {
        if (muted) return;
        soundExecutor.submit(() -> {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            try (SourceDataLine line = AudioSystem.getSourceDataLine(format)) {
                line.open(format);
                line.start();

                for (int i = 0; i < frequencies.length; i++) {
                    byte[] toneBuffer = synthesizeTone(frequencies[i], durationsMs[i], volume);
                    line.write(toneBuffer, 0, toneBuffer.length);
                }
                line.drain();
            } catch (Exception ignored) {
            }
        });
    }

    private byte[] synthesizeTone(int freqHz, int durationMs, double volume) {
        int totalSamples = (int) ((durationMs / 1000.0) * SAMPLE_RATE);
        byte[] buffer = new byte[totalSamples * 2];
        int fadeSamples = Math.max(1, totalSamples / 8);

        for (int i = 0; i < totalSamples; i++) {
            double angle = 2.0 * Math.PI * i * freqHz / SAMPLE_RATE;
            double wave = (0.75 * Math.sin(angle)) + (0.25 * Math.sin(2.0 * angle));

            double envelope = 1.0;
            if (i < fadeSamples) {
                envelope = (double) i / fadeSamples;
            } else if (i > totalSamples - fadeSamples) {
                envelope = (double) (totalSamples - i) / fadeSamples;
            }

            short sample = (short) (wave * envelope * volume * Short.MAX_VALUE);
            buffer[2 * i] = (byte) (sample & 0xFF);
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }
        return buffer;
    }
}