package com.ttsandroid.app;

import com.ttsandroid.domain.ChargingGate;
import com.ttsandroid.domain.RenderAudioUseCase;
import com.ttsandroid.domain.TextChunker;
import com.ttsandroid.platform.FakeTtsEngine;
import com.ttsandroid.platform.WavFileAudioOutputWriter;

import java.nio.file.Path;

public final class AppContainer {
    private AppContainer() {
    }

    public static RenderViewModel create(Path outputDirectory, ChargingGate chargingGate) {
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(),
                chargingGate,
                new WavFileAudioOutputWriter(outputDirectory)
        );
        return new RenderViewModel(useCase);
    }
}
