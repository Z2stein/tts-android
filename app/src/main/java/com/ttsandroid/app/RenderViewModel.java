package com.ttsandroid.app;

import com.ttsandroid.domain.RenderAudioUseCase;
import com.ttsandroid.domain.RenderFailure;
import com.ttsandroid.domain.RenderRequest;
import com.ttsandroid.domain.RenderState;
import com.ttsandroid.domain.RenderStyle;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RenderViewModel {
    private final RenderAudioUseCase useCase;
    private final ExecutorService executor;

    private volatile RenderUiState state;

    public RenderViewModel(RenderAudioUseCase useCase) {
        this.useCase = useCase;
        this.executor = Executors.newSingleThreadExecutor();
        this.state = RenderUiState.initial();
    }

    public RenderUiState getState() {
        return state;
    }

    public void onTextChanged(String text) {
        state = new RenderUiState(text, state.style(), false, 0, 0, null, null, "Idle");
    }

    public void onStyleSelected(RenderStyle style) {
        state = new RenderUiState(state.text(), style, false, 0, 0, null, null, "Idle");
    }

    public void onGenerateClicked() {
        if (state.isRendering()) {
            return;
        }

        RenderRequest request = new RenderRequest(state.text(), state.style());
        state = new RenderUiState(state.text(), state.style(), true, 0, 0, null, null, "Rendering...");

        executor.submit(() -> {
            RenderState finalState = useCase.execute(request, this::onUseCaseState);
            if (finalState instanceof RenderState.Success success) {
                state = new RenderUiState(
                        state.text(),
                        state.style(),
                        false,
                        success.totalChunks(),
                        success.totalChunks(),
                        success.outputPath(),
                        null,
                        "Success"
                );
            } else if (finalState instanceof RenderState.Canceled) {
                state = new RenderUiState(state.text(), state.style(), false, 0, 0, null, null, "Canceled");
            } else if (finalState instanceof RenderState.Failed failed) {
                state = new RenderUiState(state.text(), state.style(), false, 0, 0, null, failed.reason(), mapFailure(failed.reason()));
            }
        });
    }

    public void onCancelClicked() {
        useCase.cancel();
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    private void onUseCaseState(RenderState renderState) {
        if (renderState instanceof RenderState.Running running) {
            state = new RenderUiState(
                    state.text(),
                    state.style(),
                    true,
                    running.completedChunks(),
                    running.totalChunks(),
                    null,
                    null,
                    "Rendering..."
            );
        }
    }

    private String mapFailure(RenderFailure failure) {
        return switch (failure) {
            case EMPTY_TEXT -> "Input is empty";
            case NOT_CHARGING -> "Device is not charging";
            case ENGINE_ERROR -> "Engine error";
            case OUTPUT_WRITE_ERROR -> "Output write error";
        };
    }
}
