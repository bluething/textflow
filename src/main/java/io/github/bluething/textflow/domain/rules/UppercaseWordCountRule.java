package io.github.bluething.textflow.domain.rules;

import io.github.bluething.textflow.domain.TextContent;

import java.util.List;

public class UppercaseWordCountRule extends SmartTokenizingRule {
    @Override
    public String getName() {
        return "Words starting with uppercase";
    }

    @Override
    public IndexingRuleResult apply(TextContent content) {
        if (content.isEmpty()) {
            return new CountResult(0);
        }

        List<String> tokens = tokenize(content.content());
        long count = tokens.stream()
                .filter(this::startsWithUppercase)
                .count();
        return new CountResult(count);
    }

    private boolean startsWithUppercase(String word) {
        return switch (word) {
            case String w when w.isEmpty() -> false;
            case String w -> Character.isUpperCase(w.charAt(0));
        };
    }
}
