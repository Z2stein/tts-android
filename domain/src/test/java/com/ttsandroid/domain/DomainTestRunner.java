package com.ttsandroid.domain;

import java.util.ArrayList;
import java.util.List;

public final class DomainTestRunner {
    public static void main(String[] args) {
        testChunkingSentenceBoundaries();
        testChunkingLongSentence();
        testChunkingBlankInput();
        testChargingGateBlocksRendering();
        testProgressAndSuccess();
        testCancelHandling();
        testErrorHandling();
        testOutputWriterErrorHandling();
        testExactlyOneWavWrite();
        testEmptyInputFailure();
        System.out.println("All domain tests passed.");
    }

    private static void testChunkingSentenceBoundaries() {
        TextChunker chunker = new TextChunker(30);
        List<String> chunks = chunker.chunk("Eins. Zwei drei. Vier fünf sechs.");
        assertEquals(List.of("Eins. Zwei drei.", "Vier fünf sechs."), chunks, "sentence chunking");
    }

    private static void testChunkingLongSentence() {
        TextChunker chunker = new TextChunker(10);
        List<String> chunks = chunker.chunk("alpha beta gamma delta");
        assertEquals(List.of("alpha beta", "gamma", "delta"), chunks, "long segment split");
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 10, "chunk length <= 10");
        }
    }

    private static void testChunkingBlankInput() {
        TextChunker chunker = new TextChunker();
        assertEquals(List.of(), chunker.chunk("  \n\t  "), "blank input");
    }

    private static void testChargingGateBlocksRendering() {
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(20),
                () -> false,
                new RecordingWriter()
        );

        List<RenderState> states = new ArrayList<>();
        RenderState result = useCase.execute(new RenderRequest("Hallo Welt.", RenderStyle.NEUTRAL), states::add);

        assertEquals(new RenderState.Failed(RenderFailure.NOT_CHARGING), result, "not charging result");
        assertEquals(List.of(new RenderState.Failed(RenderFailure.NOT_CHARGING)), states, "not charging states");
    }

    private static void testProgressAndSuccess() {
        RecordingWriter writer = new RecordingWriter();
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(12),
                () -> true,
                writer
        );

        List<RenderState> states = new ArrayList<>();
        RenderState result = useCase.execute(
                new RenderRequest("Eins zwei. Drei vier.", RenderStyle.EXPRESSIVE),
                states::add
        );

        assertEquals(new RenderState.Success(2, 20, "fake.wav"), result, "success result");
        assertEquals(List.of(
                new RenderState.Running(0, 2),
                new RenderState.Running(1, 2),
                new RenderState.Running(2, 2),
                new RenderState.Success(2, 20, "fake.wav")
        ), states, "progress states");
        assertEquals(1, writer.writeCalls, "writer called once");
    }

    private static void testCancelHandling() {
        RenderAudioUseCase[] ref = new RenderAudioUseCase[1];
        RecordingWriter writer = new RecordingWriter();
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine((chunk, style) -> ref[0].cancel()),
                new TextChunker(6),
                () -> true,
                writer
        );
        ref[0] = useCase;

        List<RenderState> states = new ArrayList<>();
        RenderState result = useCase.execute(new RenderRequest("aa bb cc dd", RenderStyle.NEUTRAL), states::add);

        assertEquals(new RenderState.Canceled(), result, "cancel result");
        assertEquals(List.of(
                new RenderState.Running(0, 2),
                new RenderState.Running(1, 2),
                new RenderState.Canceled()
        ), states, "cancel states");
        assertEquals(0, writer.writeCalls, "writer not called after cancel");
    }

    private static void testErrorHandling() {
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(true, null),
                new TextChunker(20),
                () -> true,
                new RecordingWriter()
        );
        RenderState result = useCase.execute(new RenderRequest("Hallo Welt.", RenderStyle.NEUTRAL));

        assertEquals(new RenderState.Failed(RenderFailure.ENGINE_ERROR), result, "engine error");
    }

    private static void testOutputWriterErrorHandling() {
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(20),
                () -> true,
                (chunks, request) -> {
                    throw new RuntimeException("write boom");
                }
        );

        RenderState result = useCase.execute(new RenderRequest("Hallo Welt.", RenderStyle.NEUTRAL));

        assertEquals(new RenderState.Failed(RenderFailure.OUTPUT_WRITE_ERROR), result, "writer error");
    }

    private static void testExactlyOneWavWrite() {
        RecordingWriter writer = new RecordingWriter();
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(8),
                () -> true,
                writer
        );

        RenderState result = useCase.execute(new RenderRequest("aa bb cc dd ee ff", RenderStyle.NEUTRAL));

        assertTrue(result instanceof RenderState.Success, "success expected");
        assertEquals(1, writer.writeCalls, "exactly one write call");
    }

    private static void testEmptyInputFailure() {
        RenderAudioUseCase useCase = new RenderAudioUseCase(
                new FakeTtsEngine(),
                new TextChunker(),
                () -> true,
                new RecordingWriter()
        );
        RenderState result = useCase.execute(new RenderRequest("\n\t", RenderStyle.NEUTRAL));

        assertEquals(new RenderState.Failed(RenderFailure.EMPTY_TEXT), result, "empty text");
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

    private interface SynthesizeCallback {
        void onSynthesize(String textChunk, RenderStyle style);
    }

    private static final class FakeTtsEngine implements TtsEngine {
        private final boolean shouldThrow;
        private final SynthesizeCallback callback;

        private FakeTtsEngine() {
            this(false, null);
        }

        private FakeTtsEngine(SynthesizeCallback callback) {
            this(false, callback);
        }

        private FakeTtsEngine(boolean shouldThrow, SynthesizeCallback callback) {
            this.shouldThrow = shouldThrow;
            this.callback = callback;
        }

        @Override
        public byte[] synthesize(String textChunk, RenderStyle style) {
            if (callback != null) {
                callback.onSynthesize(textChunk, style);
            }
            if (shouldThrow) {
                throw new RuntimeException("boom");
            }
            return new byte[textChunk.length()];
        }
    }

    private static final class RecordingWriter implements AudioOutputWriter {
        private int writeCalls;

        @Override
        public String writeWav(List<byte[]> chunkAudio, RenderRequest request) {
            writeCalls++;
            return "fake.wav";
        }
    }
}
