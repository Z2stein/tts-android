package com.ttsandroid.platform;

import com.ttsandroid.domain.RenderStyle;
import com.ttsandroid.domain.TtsEngine;

import java.nio.charset.StandardCharsets;

public final class FakeTtsEngine implements TtsEngine {
    private final int minBytesPerChunk;

    public FakeTtsEngine() {
        this(64);
    }

    public FakeTtsEngine(int minBytesPerChunk) {
        this.minBytesPerChunk = Math.max(8, minBytesPerChunk);
    }

    @Override
    public byte[] synthesize(String textChunk, RenderStyle style) {
        byte[] input = (style.name() + ":" + textChunk).getBytes(StandardCharsets.UTF_8);
        int targetLength = Math.max(minBytesPerChunk, input.length * 2);
        byte[] output = new byte[targetLength];
        for (int i = 0; i < targetLength; i++) {
            int seed = input[i % input.length] & 0xFF;
            output[i] = (byte) ((seed + (i * 17)) & 0x7F);
        }
        return output;
    }
}
