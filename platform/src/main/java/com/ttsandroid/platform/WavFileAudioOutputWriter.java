package com.ttsandroid.platform;

import com.ttsandroid.domain.AudioOutputWriter;
import com.ttsandroid.domain.RenderRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public final class WavFileAudioOutputWriter implements AudioOutputWriter {
    private final Path outputDirectory;
    private final String fileName;

    public WavFileAudioOutputWriter(Path outputDirectory) {
        this(outputDirectory, "render-output.wav");
    }

    public WavFileAudioOutputWriter(Path outputDirectory, String fileName) {
        this.outputDirectory = outputDirectory;
        this.fileName = fileName;
    }

    @Override
    public String writeWav(List<byte[]> chunkAudio, RenderRequest request) {
        try {
            Files.createDirectories(outputDirectory);
            byte[] pcm = flatten(chunkAudio);
            byte[] wav = buildWav(pcm, 16_000, 1, 16);
            Path path = outputDirectory.resolve(fileName);
            Files.write(path, wav, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
            return path.toString();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to write wav", ex);
        }
    }

    private byte[] flatten(List<byte[]> chunks) {
        int total = 0;
        for (byte[] chunk : chunks) {
            total += chunk.length;
        }
        byte[] out = new byte[total];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, out, offset, chunk.length);
            offset += chunk.length;
        }
        return out;
    }

    private byte[] buildWav(byte[] pcm8Bit, int sampleRate, int channels, int bitsPerSample) {
        byte[] pcm16 = new byte[pcm8Bit.length * 2];
        for (int i = 0; i < pcm8Bit.length; i++) {
            pcm16[i * 2] = pcm8Bit[i];
            pcm16[i * 2 + 1] = 0;
        }

        int dataSize = pcm16.length;
        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int chunkSize = 36 + dataSize;

        byte[] header = new byte[44];
        writeAscii(header, 0, "RIFF");
        writeIntLE(header, 4, chunkSize);
        writeAscii(header, 8, "WAVE");
        writeAscii(header, 12, "fmt ");
        writeIntLE(header, 16, 16);
        writeShortLE(header, 20, (short) 1);
        writeShortLE(header, 22, (short) channels);
        writeIntLE(header, 24, sampleRate);
        writeIntLE(header, 28, byteRate);
        writeShortLE(header, 32, (short) blockAlign);
        writeShortLE(header, 34, (short) bitsPerSample);
        writeAscii(header, 36, "data");
        writeIntLE(header, 40, dataSize);

        byte[] wav = new byte[header.length + pcm16.length];
        System.arraycopy(header, 0, wav, 0, header.length);
        System.arraycopy(pcm16, 0, wav, header.length, pcm16.length);
        return wav;
    }

    private void writeAscii(byte[] target, int offset, String value) {
        byte[] bytes = value.getBytes();
        System.arraycopy(bytes, 0, target, offset, bytes.length);
    }

    private void writeIntLE(byte[] target, int offset, int value) {
        target[offset] = (byte) (value & 0xFF);
        target[offset + 1] = (byte) ((value >>> 8) & 0xFF);
        target[offset + 2] = (byte) ((value >>> 16) & 0xFF);
        target[offset + 3] = (byte) ((value >>> 24) & 0xFF);
    }

    private void writeShortLE(byte[] target, int offset, short value) {
        target[offset] = (byte) (value & 0xFF);
        target[offset + 1] = (byte) ((value >>> 8) & 0xFF);
    }
}
