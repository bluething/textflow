package io.github.bluething.textflow.domain.rules;

public record CountResult(long count) implements IndexingRuleResult {
    @Override
    public String getDisplayValue() {
        return String.valueOf(count);
    }

    @Override
    public Object getValue() {
        return count;
    }
}
