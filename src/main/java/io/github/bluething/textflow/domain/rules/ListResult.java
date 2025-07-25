package io.github.bluething.textflow.domain.rules;

import java.util.List;

public record ListResult(List<String> items) implements IndexingRuleResult {
    @Override
    public String getDisplayValue() {
        if (items.isEmpty()) {
            return "[]";
        }

        if (items.size() <= 10) {
            return items.toString();
        }

        // For large lists, show first 10 items and indicate there are more
        List<String> preview = items.subList(0, 10);
        return preview + " ... (and " + (items.size() - 10) + " more)";
    }

    @Override
    public Object getValue() {
        return items;
    }
}
