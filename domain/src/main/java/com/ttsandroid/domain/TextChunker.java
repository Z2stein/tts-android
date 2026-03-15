package com.ttsandroid.domain;

import java.util.ArrayList;
import java.util.List;

public final class TextChunker {
    private final int maxChunkChars;

    public TextChunker() {
        this(400);
    }

    public TextChunker(int maxChunkChars) {
        if (maxChunkChars <= 0) {
            throw new IllegalArgumentException("maxChunkChars must be > 0");
        }
        this.maxChunkChars = maxChunkChars;
    }

    public List<String> chunk(String input) {
        String normalized = input == null ? "" : input.trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return List.of();
        }

        String[] sentenceCandidates = normalized.split("(?<=[.!?])\\s+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String sentence : sentenceCandidates) {
            if (sentence.length() > maxChunkChars) {
                flush(chunks, current);
                chunks.addAll(splitLongSegment(sentence));
                continue;
            }

            if (current.isEmpty()) {
                current.append(sentence);
                continue;
            }

            int candidateLength = current.length() + 1 + sentence.length();
            if (candidateLength <= maxChunkChars) {
                current.append(' ').append(sentence);
            } else {
                flush(chunks, current);
                current.append(sentence);
            }
        }

        flush(chunks, current);
        return chunks;
    }

    private List<String> splitLongSegment(String segment) {
        String[] words = segment.split(" ");
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            if (word.length() > maxChunkChars) {
                flush(result, current);
                for (int i = 0; i < word.length(); i += maxChunkChars) {
                    int end = Math.min(word.length(), i + maxChunkChars);
                    result.add(word.substring(i, end));
                }
                continue;
            }

            if (current.isEmpty()) {
                current.append(word);
                continue;
            }

            int candidateLength = current.length() + 1 + word.length();
            if (candidateLength <= maxChunkChars) {
                current.append(' ').append(word);
            } else {
                flush(result, current);
                current.append(word);
            }
        }

        flush(result, current);
        return result;
    }

    private static void flush(List<String> target, StringBuilder current) {
        if (!current.isEmpty()) {
            target.add(current.toString());
            current.setLength(0);
        }
    }
}
