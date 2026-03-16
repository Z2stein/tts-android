package com.ttsandroid.platform;

import com.ttsandroid.domain.RenderStyle;
import com.ttsandroid.domain.TtsEngine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class RealTtsEngine implements TtsEngine {
    public interface RealTtsRuntime {
        void initialize();

        byte[] synthesize(String textChunk, RenderStyle style);
    }

    private final RealTtsRuntime runtime;

    public RealTtsEngine() {
        this(new ChatterboxCommandRuntime());
    }

    public RealTtsEngine(RealTtsRuntime runtime) {
        this.runtime = runtime;
        try {
            this.runtime.initialize();
        } catch (RuntimeException ex) {
            throw new IllegalStateException("RealTtsEngine initialization failed", ex);
        }
    }

    @Override
    public byte[] synthesize(String textChunk, RenderStyle style) {
        try {
            return runtime.synthesize(textChunk, style);
        } catch (RuntimeException ex) {
            throw new RuntimeException("RealTtsEngine synthesis failed", ex);
        }
    }

    static final class ChatterboxCommandRuntime implements RealTtsRuntime {
        private static final String ENV_COMMAND = "CHATTERBOX_TTS_CMD";
        private List<String> command;

        @Override
        public void initialize() {
            String raw = System.getenv(ENV_COMMAND);
            if (raw == null || raw.trim().isEmpty()) {
                throw new IllegalStateException(
                        "Missing " + ENV_COMMAND + " for RealTtsEngine command runtime"
                );
            }
            command = new ArrayList<>(Arrays.asList(raw.trim().split("\\s+")));
        }

        @Override
        public byte[] synthesize(String textChunk, RenderStyle style) {
            if (command == null || command.isEmpty()) {
                throw new IllegalStateException("Real runtime used before initialization");
            }

            List<String> args = new ArrayList<>(command);
            args.add("--style");
            args.add(style.name().toLowerCase());
            args.add("--text");
            args.add(textChunk);

            Process process;
            try {
                process = new ProcessBuilder(args).start();
            } catch (IOException ex) {
                throw new RuntimeException("Failed to launch chatterbox command", ex);
            }

            byte[] stdout;
            String stderr;
            try {
                stdout = readFully(process.getInputStream());
                stderr = new String(readFully(process.getErrorStream()), StandardCharsets.UTF_8);
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    throw new RuntimeException("Chatterbox command failed with exitCode=" + exitCode + " stderr=" + stderr);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Failed to read chatterbox command output", ex);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for chatterbox command", ex);
            }

            if (stdout.length == 0) {
                throw new RuntimeException("Chatterbox command returned empty audio payload");
            }
            return stdout;
        }

        private static byte[] readFully(InputStream input) throws IOException {
            try (input; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
                byte[] chunk = new byte[4096];
                int read;
                while ((read = input.read(chunk)) >= 0) {
                    if (read == 0) {
                        continue;
                    }
                    buffer.write(chunk, 0, read);
                }
                return buffer.toByteArray();
            }
        }
    }
}
