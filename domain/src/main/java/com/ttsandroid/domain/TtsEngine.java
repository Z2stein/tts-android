package com.ttsandroid.domain;

public interface TtsEngine {
    byte[] synthesize(String textChunk, RenderStyle style);
}
