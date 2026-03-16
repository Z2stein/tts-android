package com.ttsandroid.app;

import com.ttsandroid.domain.ChargingGate;
import com.ttsandroid.domain.RenderAudioUseCase;
import com.ttsandroid.domain.TextChunker;
import com.ttsandroid.domain.TtsEngine;
import com.ttsandroid.platform.FakeTtsEngine;
import com.ttsandroid.platform.RealTtsEngine;
import com.ttsandroid.platform.WavFileAudioOutputWriter;

import java.nio.file.Path;

public final class AppContainer {
    public enum EngineSelection {
        FAKE,
        REAL
    }

    private static final String ENGINE_PROPERTY = "tts.engine";

    private AppContainer() {
    }

    public static RenderViewModel create(Path outputDirectory, ChargingGate chargingGate) {
        return create(outputDirectory, chargingGate, engineSelectionFromProperty());
    }

    public static RenderViewModel create(Path outputDirectory, ChargingGate chargingGate, EngineSelection engineSelection) {
        return create(outputDirectory, chargingGate, selectEngine(engineSelection), engineSelection.name());
    }

    static RenderViewModel create(Path outputDirectory, ChargingGate chargingGate, TtsEngine ttsEngine) {
        return create(outputDirectory, chargingGate, ttsEngine, ttsEngine.getClass().getSimpleName());
    }

    private static RenderViewModel create(Path outputDirectory, ChargingGate chargingGate, TtsEngine ttsEngine, String engineMode) {
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                ttsEngine,
                new TextChunker(),
                chargingGate,
                new WavFileAudioOutputWriter(outputDirectory)
        );
        return new RenderViewModel(useCase, engineMode);
    }

    private static TtsEngine selectEngine(EngineSelection selection) {
        return switch (selection) {
            case FAKE -> new FakeTtsEngine();
            case REAL -> new RealTtsEngine();
        };
    }

    private static EngineSelection engineSelectionFromProperty() {
        String configured = System.getProperty(ENGINE_PROPERTY, "fake");
        if ("real".equalsIgnoreCase(configured)) {
            return EngineSelection.REAL;
        }
        return EngineSelection.FAKE;
    }
}
