package io.github.bluething.textflow.domain.rules;

public sealed interface IndexingRuleResult permits CountResult, ListResult {
    /**
     * Gets a human-readable display representation of the result.
     */
    String getDisplayValue();

    /**
     * Gets the raw result value for programmatic access.
     */
    Object getValue();
}
