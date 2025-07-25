package io.github.bluething.textflow.domain.rules;

import io.github.bluething.textflow.domain.TextContent;
import io.github.bluething.textflow.domain.tokenization.TokenizationConfig;

public interface IndexingRule {
    /**
     * Gets the human-readable name of this rule.
     */
    String getName();

    /**
     * Applies this rule to the given text content.
     *
     * @param content The text content to analyze
     * @return The result of applying this rule
     */
    IndexingRuleResult apply(TextContent content);

    default void setTokenizationConfig(TokenizationConfig config) {
        // Default: no-op
    }
}
