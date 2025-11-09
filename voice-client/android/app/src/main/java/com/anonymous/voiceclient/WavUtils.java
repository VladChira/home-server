package com.anonymous.voiceclient;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class WavUtils {

    /**
     * Turn a single PCM buffer into a WAV byte[] (16-bit PCM mono).
     */
    public static byte[] toWav(short[] pcmData, int sampleRate) throws IOException {
        int numChannels = 1;
        int byteRate = sampleRate * numChannels * 2; // 16-bit

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // RIFF header
        out.write("RIFF".getBytes());
        out.write(intToLittleEndian(36 + pcmData.length * 2)); // file size - 8
        out.write("WAVE".getBytes());

        // fmt chunk
        out.write("fmt ".getBytes());
        out.write(intToLittleEndian(16)); // Subchunk1Size
        out.write(shortToLittleEndian((short) 1)); // PCM
        out.write(shortToLittleEndian((short) numChannels));
        out.write(intToLittleEndian(sampleRate));
        out.write(intToLittleEndian(byteRate));
        out.write(shortToLittleEndian((short) (numChannels * 2))); // block align
        out.write(shortToLittleEndian((short) 16)); // bits per sample

        // data chunk
        out.write("data".getBytes());
        out.write(intToLittleEndian(pcmData.length * 2));

        // pcm data
        ByteBuffer bb = ByteBuffer.allocate(pcmData.length * 2).order(ByteOrder.LITTLE_ENDIAN);
        for (short s : pcmData) {
            bb.putShort(s);
        }
        out.write(bb.array());

        return out.toByteArray();
    }

    /**
     * Stitch multiple short[] chunks into one.
     */
    public static short[] concatPcmChunks(List<short[]> chunks) {
        int total = 0;
        for (short[] c : chunks) {
            total += c.length;
        }
        short[] all = new short[total];
        int pos = 0;
        for (short[] c : chunks) {
            System.arraycopy(c, 0, all, pos, c.length);
            pos += c.length;
        }
        return all;
    }

    /**
     * Write the stitched WAV to app cache and return the absolute path.
     */
    public static String writeUtteranceToCache(Context ctx, List<short[]> chunks, int sampleRate)
            throws IOException {

        short[] pcmAll = concatPcmChunks(chunks);
        byte[] wav = toWav(pcmAll, sampleRate);

        File cacheDir = ctx.getCacheDir(); // e.g. /data/user/0/your.app/cache
        File outFile = new File(cacheDir, "utter_" + System.currentTimeMillis() + ".wav");

        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(wav);
            fos.flush();
        }

        return outFile.getAbsolutePath();
    }

    private static byte[] intToLittleEndian(int val) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(val).array();
    }

    private static byte[] shortToLittleEndian(short val) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(val).array();
    }
}
