package com.ttsandroid.domain;

import java.util.List;

@FunctionalInterface
public interface AudioOutputWriter {
    String writeWav(List<byte[]> chunkAudio, RenderRequest request);
}
