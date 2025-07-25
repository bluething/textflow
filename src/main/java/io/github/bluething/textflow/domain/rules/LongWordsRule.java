package io.github.bluething.textflow.domain.rules;

import io.github.bluething.textflow.domain.TextContent;
import io.github.bluething.textflow.domain.tokenization.TokenizationConfig;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.SequencedSet;
import java.util.stream.Collectors;

public class LongWordsRule extends SmartTokenizingRule {
    private final int minLength;

    public LongWordsRule() {
        this(6); // Longer than 5 means 6 or more
    }

    public LongWordsRule(int minLength) {
        super();
        this.minLength = minLength;
    }

    public LongWordsRule(TokenizationConfig config, int minLength) {
        super(config);
        this.minLength = minLength;
    }

    @Override
    public String getName() {
        return "Words longer than " + (minLength - 1) + " characters";
    }

    @Override
    public IndexingRuleResult apply(TextContent content) {
        if (content.isEmpty()) {
            return new ListResult(List.of());
        }

        List<String> tokens = tokenize(content.content());

        // Use SequencedSet for better performance with ordered operations
        SequencedSet<String> longWords = tokens.stream()
                .filter(word -> word.length() >= minLength)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return new ListResult(longWords.stream().toList());
    }
}
