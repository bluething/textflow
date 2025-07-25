package io.github.bluething.textflow.domain.rules;

import io.github.bluething.textflow.domain.tokenization.SmartTokenizer;
import io.github.bluething.textflow.domain.tokenization.TokenizationConfig;

import java.util.List;

public abstract class SmartTokenizingRule implements IndexingRule {
    protected SmartTokenizer tokenizer;

    protected SmartTokenizingRule() {
        this.tokenizer = new SmartTokenizer(TokenizationConfig.defaultConfig());
    }

    protected SmartTokenizingRule(TokenizationConfig config) {
        this.tokenizer = new SmartTokenizer(config);
    }

    @Override
    public void setTokenizationConfig(TokenizationConfig config) {
        this.tokenizer = new SmartTokenizer(config);
    }

    protected List<String> tokenize(String text) {
        return tokenizer.tokenize(text);
    }
}
