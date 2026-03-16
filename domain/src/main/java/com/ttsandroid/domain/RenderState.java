package com.ttsandroid.domain;

public sealed interface RenderState permits RenderState.Idle, RenderState.Running, RenderState.Success, RenderState.Canceled, RenderState.Failed {
    record Idle() implements RenderState {
    }

    record Running(int completedChunks, int totalChunks) implements RenderState {
    }

    record Success(int totalChunks, int totalAudioBytes, String outputPath) implements RenderState {
    }

    record Canceled() implements RenderState {
    }

    record Failed(RenderFailure reason) implements RenderState {
    }
}
