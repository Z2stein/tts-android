package com.ttsandroid.app;

import com.ttsandroid.domain.RenderFailure;
import com.ttsandroid.domain.RenderStyle;

public record RenderUiState(
        String engineMode,
        String text,
        RenderStyle style,
        boolean isRendering,
        int completedChunks,
        int totalChunks,
        String outputPath,
        RenderFailure failure,
        String statusMessage
) {
    public static RenderUiState initial() {
        return new RenderUiState("FAKE", "", RenderStyle.NEUTRAL, false, 0, 0, null, null, "Idle");
    }
}
