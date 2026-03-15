package com.ttsandroid.domain;

import java.util.List;
import java.util.function.Consumer;

public final class RenderAudioUseCase {
    private final TtsEngine ttsEngine;
    private final TextChunker textChunker;
    private final ChargingGate chargingGate;

    private volatile boolean canceled;

    public RenderAudioUseCase(TtsEngine ttsEngine, TextChunker textChunker, ChargingGate chargingGate) {
        this.ttsEngine = ttsEngine;
        this.textChunker = textChunker;
        this.chargingGate = chargingGate;
    }

    public void cancel() {
        this.canceled = true;
    }

    public RenderState execute(RenderRequest request, Consumer<RenderState> onState) {
        canceled = false;
        List<String> chunks = textChunker.chunk(request.text());

        if (chunks.isEmpty()) {
            RenderState.Failed failed = new RenderState.Failed(RenderFailure.EMPTY_TEXT);
            onState.accept(failed);
            return failed;
        }

        if (!chargingGate.isCharging()) {
            RenderState.Failed failed = new RenderState.Failed(RenderFailure.NOT_CHARGING);
            onState.accept(failed);
            return failed;
        }

        int totalBytes = 0;
        onState.accept(new RenderState.Running(0, chunks.size()));

        for (int i = 0; i < chunks.size(); i++) {
            if (canceled) {
                RenderState.Canceled canceledState = new RenderState.Canceled();
                onState.accept(canceledState);
                return canceledState;
            }

            if (!chargingGate.isCharging()) {
                RenderState.Failed failed = new RenderState.Failed(RenderFailure.NOT_CHARGING);
                onState.accept(failed);
                return failed;
            }

            byte[] audioBytes;
            try {
                audioBytes = ttsEngine.synthesize(chunks.get(i), request.style());
            } catch (RuntimeException ex) {
                RenderState.Failed failed = new RenderState.Failed(RenderFailure.ENGINE_ERROR);
                onState.accept(failed);
                return failed;
            }

            totalBytes += audioBytes.length;
            onState.accept(new RenderState.Running(i + 1, chunks.size()));
        }

        RenderState.Success success = new RenderState.Success(chunks.size(), totalBytes);
        onState.accept(success);
        return success;
    }

    public RenderState execute(RenderRequest request) {
        return execute(request, ignored -> {});
    }
}
