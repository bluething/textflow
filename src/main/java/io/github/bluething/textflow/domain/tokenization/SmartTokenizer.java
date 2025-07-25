package io.github.bluething.textflow.domain.tokenization;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;

public class SmartTokenizer {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");

    private static final Pattern URL_PATTERN =
            Pattern.compile("\\bhttps?://[\\w.-]+(?:\\.[\\w.-]+)+[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=]*\\b");

    private static final Pattern HYPHENATED_WORD_PATTERN =
            Pattern.compile("\\b\\w+(?:-\\w+)+\\b");

    private static final Pattern NUMBER_PATTERN =
            Pattern.compile("\\b\\d+(?:[.,]\\d+)*\\b");

    private static final Pattern BASIC_WORD_PATTERN =
            Pattern.compile("\\b\\w+\\b");

    private final TokenizationConfig config;

    public SmartTokenizer(TokenizationConfig config) {
        this.config = config;
    }

    /**
     * Tokenizes text according to the configured strategy.
     */
    public java.util.List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return java.util.List.of();
        }

        List<String> tokens = new java.util.ArrayList<>();

        // Handle special patterns first
        if (config.isPreserveEmails()) {
            extractPattern(text, EMAIL_PATTERN, tokens);
        }

        if (config.isPreserveUrls()) {
            extractPattern(text, URL_PATTERN, tokens);
        }

        if (config.isPreserveNumbers()) {
            extractPattern(text, NUMBER_PATTERN, tokens);
        }

        // Handle hyphenated words
        handleHyphenatedWords(text, tokens);

        // Extract remaining basic words
        extractPattern(text, BASIC_WORD_PATTERN, tokens);

        // Filter by minimum length and clean up
        return tokens.stream()
                .filter(token -> token.length() >= config.getMinWordLength())
                .distinct()
                .collect(java.util.stream.Collectors.toList());
    }

    private void extractPattern(String text, Pattern pattern, List<String> tokens) {
        pattern.matcher(text)
                .results()
                .map(MatchResult::group)
                .forEach(tokens::add);
    }

    private void handleHyphenatedWords(String text, java.util.List<String> tokens) {
        switch (config.getHyphenHandling()) {
            case PRESERVE -> extractPattern(text, HYPHENATED_WORD_PATTERN, tokens);

            case SPLIT -> HYPHENATED_WORD_PATTERN.matcher(text)
                    .results()
                    .forEach(match -> {
                        String[] parts = match.group().split("-");
                        for (String part : parts) {
                            if (part.length() >= config.getMinWordLength()) {
                                tokens.add(part);
                            }
                        }
                    });

            case BOTH -> HYPHENATED_WORD_PATTERN.matcher(text)
                    .results()
                    .forEach(match -> {
                        // Add the full hyphenated word
                        tokens.add(match.group());
                        // Add individual parts
                        String[] parts = match.group().split("-");
                        for (String part : parts) {
                            if (part.length() >= config.getMinWordLength()) {
                                tokens.add(part);
                            }
                        }
                    });
        }
    }
}
