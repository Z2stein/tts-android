package com.ttsandroid.app;

import com.ttsandroid.domain.AudioOutputWriter;
import com.ttsandroid.domain.RenderAudioUseCase;
import com.ttsandroid.domain.RenderFailure;
import com.ttsandroid.domain.RenderRequest;
import com.ttsandroid.domain.RenderStyle;
import com.ttsandroid.domain.TtsEngine;
import com.ttsandroid.domain.TextChunker;
import com.ttsandroid.platform.FakeTtsEngine;
import com.ttsandroid.platform.RealTtsEngine;
import com.ttsandroid.platform.WavFileAudioOutputWriter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public final class AppTestRunner {
    public static void main(String[] args) throws Exception {
        testEndToEndSuccess();
        testProgressVisible();
        testCancelStopsRender();
        testExactlyOneWavFileCreated();
        testOutputPathReturned();
        testFailurePathEngineAndWriter();
        testRealEngineSubstitutionThroughUseCaseContract();
        testRealEngineInitializationFailureSurfacesEarly();
        testRealEngineRenderFailureMapsToUiFailure();
        System.out.println("All app tests passed.");
    }

    private static void testEndToEndSuccess() throws Exception {
        Path dir = Files.createTempDirectory("m3-success-");
        RenderViewModel vm = AppContainer.create(dir, () -> true);

        vm.onTextChanged("Dies ist ein deutscher Testtext. Er ist lang genug für mehrere Chunks.");
        vm.onStyleSelected(RenderStyle.EXPRESSIVE);
        vm.onGenerateClicked();

        waitFor(() -> !vm.getState().isRendering(), 2000);

        RenderUiState state = vm.getState();
        assertEquals("Success", state.statusMessage(), "success message");
        assertTrue(state.outputPath() != null, "output path set");
        assertTrue(Files.exists(Path.of(state.outputPath())), "wav exists");
        vm.shutdown();
    }

    private static void testProgressVisible() throws Exception {
        Path dir = Files.createTempDirectory("m3-progress-");
        AtomicBoolean runningStateObserved = new AtomicBoolean(false);

        TtsEngine slowEngine = (chunk, style) -> {
            sleep(60);
            return new FakeTtsEngine(32).synthesize(chunk, style);
        };

        RenderAudioUseCase useCase = new RenderAudioUseCase(
                slowEngine,
                new TextChunker(10),
                () -> true,
                new WavFileAudioOutputWriter(dir, "progress.wav")
        );
        RenderViewModel vm = new RenderViewModel(useCase);

        vm.onTextChanged("eins zwei drei vier fünf sechs sieben acht neun zehn.");
        vm.onGenerateClicked();

        waitFor(() -> {
            RenderUiState state = vm.getState();
            boolean seen = state.isRendering() && state.totalChunks() > 0;
            if (seen) {
                runningStateObserved.set(true);
            }
            return seen;
        }, 2000);
        waitFor(() -> !vm.getState().isRendering(), 2000);

        assertTrue(runningStateObserved.get(), "progress/running state observed");
        vm.shutdown();
    }

    private static void testCancelStopsRender() throws Exception {
        Path dir = Files.createTempDirectory("m3-cancel-");
        TtsEngine slowEngine = (chunk, style) -> {
            sleep(120);
            return new byte[32];
        };

        RenderAudioUseCase useCase = new RenderAudioUseCase(
                slowEngine,
                new TextChunker(5),
                () -> true,
                new WavFileAudioOutputWriter(dir, "cancel.wav")
        );
        RenderViewModel vm = new RenderViewModel(useCase);

        vm.onTextChanged("aa bb cc dd ee ff gg hh");
        vm.onGenerateClicked();
        waitFor(() -> vm.getState().isRendering(), 1000);
        vm.onCancelClicked();
        waitFor(() -> !vm.getState().isRendering(), 2000);

        RenderUiState state = vm.getState();
        assertEquals("Canceled", state.statusMessage(), "cancel status");
        assertTrue(state.outputPath() == null, "no output path on cancel");
        vm.shutdown();
    }

    private static void testExactlyOneWavFileCreated() throws Exception {
        Path dir = Files.createTempDirectory("m3-onefile-");
        RenderViewModel vm = AppContainer.create(dir, () -> true);

        vm.onTextChanged("hallo welt. hallo welt. hallo welt.");
        vm.onGenerateClicked();
        waitFor(() -> !vm.getState().isRendering(), 2000);

        List<Path> wavFiles = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".wav")).forEach(wavFiles::add);
        }

        assertEquals(1, wavFiles.size(), "one wav file only");
        vm.shutdown();
    }

    private static void testOutputPathReturned() throws Exception {
        Path dir = Files.createTempDirectory("m3-path-");
        String fileName = "artifact.wav";

        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(),
                () -> true,
                new WavFileAudioOutputWriter(dir, fileName)
        );
        RenderViewModel vm = new RenderViewModel(useCase);

        vm.onTextChanged("pfad test");
        vm.onGenerateClicked();
        waitFor(() -> !vm.getState().isRendering(), 2000);

        String expected = dir.resolve(fileName).toString();
        assertEquals(expected, vm.getState().outputPath(), "output path");
        vm.shutdown();
    }

    private static void testFailurePathEngineAndWriter() throws Exception {
        RenderAudioUseCase engineFailUseCase = new RenderAudioUseCase(
                (chunk, style) -> {
                    throw new RuntimeException("engine fail");
                },
                new TextChunker(),
                () -> true,
                (chunkAudio, request) -> "unused.wav"
        );
        RenderViewModel engineVm = new RenderViewModel(engineFailUseCase);
        engineVm.onTextChanged("engine fail");
        engineVm.onGenerateClicked();
        waitFor(() -> !engineVm.getState().isRendering(), 2000);
        assertEquals(RenderFailure.ENGINE_ERROR, engineVm.getState().failure(), "engine failure state");
        engineVm.shutdown();

        AudioOutputWriter writerThrows = (chunks, request) -> {
            throw new RuntimeException("writer fail");
        };
        RenderAudioUseCase writerFailUseCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(),
                () -> true,
                writerThrows
        );
        RenderViewModel writerVm = new RenderViewModel(writerFailUseCase);
        writerVm.onTextChanged("writer fail");
        writerVm.onGenerateClicked();
        waitFor(() -> !writerVm.getState().isRendering(), 2000);
        assertEquals(RenderFailure.OUTPUT_WRITE_ERROR, writerVm.getState().failure(), "writer failure state");
        writerVm.shutdown();
    }

    private static void testRealEngineSubstitutionThroughUseCaseContract() throws Exception {
        Path fakeDir = Files.createTempDirectory("m5-fake-sub-");
        RenderViewModel fakeVm = AppContainer.create(fakeDir, () -> true, AppContainer.EngineSelection.FAKE);
        fakeVm.onTextChanged("Substitution mit Fake Engine.");
        fakeVm.onGenerateClicked();
        waitFor(() -> !fakeVm.getState().isRendering(), 2000);
        assertEquals("Success", fakeVm.getState().statusMessage(), "fake selection still succeeds");
        fakeVm.shutdown();

        Path realDir = Files.createTempDirectory("m5-real-sub-");
        RealTtsEngine.RealTtsRuntime runtime = new RealTtsEngine.RealTtsRuntime() {
            @Override
            public void initialize() {
            }

            @Override
            public byte[] synthesize(String textChunk, RenderStyle style) {
                return (style.name() + textChunk).getBytes();
            }
        };
        RenderViewModel realVm = AppContainer.create(realDir, () -> true, new RealTtsEngine(runtime));
        realVm.onTextChanged("Substitution mit Real Engine.");
        realVm.onStyleSelected(RenderStyle.EXPRESSIVE);
        realVm.onGenerateClicked();
        waitFor(() -> !realVm.getState().isRendering(), 2000);
        assertEquals("Success", realVm.getState().statusMessage(), "real engine succeeds behind same use case flow");
        realVm.shutdown();
    }

    private static void testRealEngineInitializationFailureSurfacesEarly() {
        RealTtsEngine.RealTtsRuntime failingRuntime = new RealTtsEngine.RealTtsRuntime() {
            @Override
            public void initialize() {
                throw new RuntimeException("init boom");
            }

            @Override
            public byte[] synthesize(String textChunk, RenderStyle style) {
                return new byte[8];
            }
        };

        try {
            new RealTtsEngine(failingRuntime);
            throw new AssertionError("expected RealTtsEngine init failure");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("initialization failed"), "init failure message");
        }
    }

    private static void testRealEngineRenderFailureMapsToUiFailure() throws Exception {
        Path dir = Files.createTempDirectory("m5-real-render-fail-");

        RealTtsEngine.RealTtsRuntime runtime = new RealTtsEngine.RealTtsRuntime() {
            @Override
            public void initialize() {
            }

            @Override
            public byte[] synthesize(String textChunk, RenderStyle style) {
                throw new RuntimeException("render boom");
            }
        };

        RenderViewModel vm = AppContainer.create(dir, () -> true, new RealTtsEngine(runtime));
        vm.onTextChanged("Dieser Lauf muss fehlschlagen.");
        vm.onGenerateClicked();
        waitFor(() -> !vm.getState().isRendering(), 2000);
        assertEquals(RenderFailure.ENGINE_ERROR, vm.getState().failure(), "real engine render failure mapped to ENGINE_ERROR");
        vm.shutdown();
    }

    private static void waitFor(Check check, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (check.evaluate()) {
                return;
            }
            Thread.sleep(10);
        }
        throw new AssertionError("Timeout after " + timeoutMs + "ms");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected=" + expected + " actual=" + actual);
        }
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private interface Check {
        boolean evaluate() throws Exception;
    }
}
