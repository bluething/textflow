package io.github.bluething.textflow.domain;

import io.github.bluething.textflow.domain.rules.IndexingRule;
import io.github.bluething.textflow.domain.rules.LongWordsRule;
import io.github.bluething.textflow.domain.rules.UppercaseWordCountRule;
import io.github.bluething.textflow.domain.tokenization.TokenizationConfig;

import java.util.List;

public class IndexerConfiguration {
    private final List<IndexingRule> indexingRules;
    private final ContentExtractorRegistry extractorRegistry;
    private final int maxConcurrentFiles;
    private final long maxFileSizeBytes;
    private final TokenizationConfig tokenizationConfig;
    private final boolean enableVirtualThreads;
    private final boolean enableMemoryMapping;

    public static IndexerConfiguration defaultConfiguration() {
        return new Builder()
                .addIndexingRule(new UppercaseWordCountRule())
                .addIndexingRule(new LongWordsRule())
                .withExtractorRegistry(new ContentExtractorRegistry())
                .withTokenizationConfig(TokenizationConfig.defaultConfig())
                .withVirtualThreads(true)
                .withMemoryMapping(true)
                .withMaxConcurrentFiles(0) // Unlimited with virtual threads
                .withMaxFileSizeBytes(1024L * 1024L * 1024L) // 1GB max file size
                .build();
    }

    private IndexerConfiguration(Builder builder) {
        this.indexingRules = List.copyOf(builder.indexingRules);
        this.extractorRegistry = builder.extractorRegistry;
        this.maxConcurrentFiles = builder.maxConcurrentFiles;
        this.maxFileSizeBytes = builder.maxFileSizeBytes;
        this.tokenizationConfig = builder.tokenizationConfig;
        this.enableVirtualThreads = builder.enableVirtualThreads;
        this.enableMemoryMapping = builder.enableMemoryMapping;

        // Apply tokenization config to all rules that support it
        this.indexingRules.forEach(rule -> rule.setTokenizationConfig(this.tokenizationConfig));
    }

    public static class Builder {
        private List<IndexingRule> indexingRules = List.of();
        private ContentExtractorRegistry extractorRegistry = new ContentExtractorRegistry();
        private int maxConcurrentFiles = Runtime.getRuntime().availableProcessors();
        private long maxFileSizeBytes = 1024L * 1024L * 1024L; // 1GB
        private TokenizationConfig tokenizationConfig = TokenizationConfig.defaultConfig();
        private boolean enableVirtualThreads = true;
        private boolean enableMemoryMapping = true;

        public Builder addIndexingRule(IndexingRule rule) {
            this.indexingRules = new java.util.ArrayList<>(this.indexingRules);
            this.indexingRules.add(rule);
            return this;
        }

        public Builder withIndexingRules(List<IndexingRule> rules) {
            this.indexingRules = List.copyOf(rules);
            return this;
        }

        public Builder withExtractorRegistry(ContentExtractorRegistry registry) {
            this.extractorRegistry = registry;
            return this;
        }

        public Builder withTokenizationConfig(TokenizationConfig config) {
            this.tokenizationConfig = config;
            return this;
        }

        public Builder withVirtualThreads(boolean enable) {
            this.enableVirtualThreads = enable;
            return this;
        }

        public Builder withMemoryMapping(boolean enable) {
            this.enableMemoryMapping = enable;
            return this;
        }

        public Builder withMaxConcurrentFiles(int maxConcurrentFiles) {
            if (maxConcurrentFiles < 0) {
                throw new IllegalArgumentException("Max concurrent files cannot be negative");
            }
            this.maxConcurrentFiles = maxConcurrentFiles;
            return this;
        }

        public Builder withMaxFileSizeBytes(long maxFileSizeBytes) {
            if (maxFileSizeBytes < 1) {
                throw new IllegalArgumentException("Max file size must be at least 1 byte");
            }
            this.maxFileSizeBytes = maxFileSizeBytes;
            return this;
        }

        public IndexerConfiguration build() {
            if (indexingRules.isEmpty()) {
                throw new IllegalStateException("At least one indexing rule must be configured");
            }

            if (maxConcurrentFiles == 0 && !enableVirtualThreads) {
                throw new IllegalStateException("Unlimited concurrent files requires virtual threads to be enabled");
            }
            return new IndexerConfiguration(this);
        }
    }

    public List<IndexingRule> getIndexingRules() { return indexingRules; }
    public ContentExtractorRegistry getExtractorRegistry() { return extractorRegistry; }

    public long getMaxFileSizeBytes() { return maxFileSizeBytes; }
}
