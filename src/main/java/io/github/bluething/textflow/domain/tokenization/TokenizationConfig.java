package io.github.bluething.textflow.domain.tokenization;

public class TokenizationConfig {
    public enum HyphenHandling {
        /**
         * Treat hyphenated words as single tokens: "state-of-the-art" → ["state-of-the-art"]
         */
        PRESERVE,

        /**
         * Split on hyphens: "state-of-the-art" → ["state", "of", "the", "art"]
         */
        SPLIT,

        /**
         * Index both: "state-of-the-art" → ["state-of-the-art", "state", "of", "the", "art"]
         */
        BOTH
    }

    public static TokenizationConfig defaultConfig() {
        return new Builder()
                .withHyphenHandling(HyphenHandling.PRESERVE)
                .withPreserveNumbers(true)
                .withPreserveEmails(true)
                .withPreserveUrls(true)
                .withMinWordLength(1)
                .build();
    }

    private final HyphenHandling hyphenHandling;
    private final boolean preserveNumbers;
    private final boolean preserveEmails;
    private final boolean preserveUrls;
    private final int minWordLength;

    private TokenizationConfig(Builder builder) {
        this.hyphenHandling = builder.hyphenHandling;
        this.preserveNumbers = builder.preserveNumbers;
        this.preserveEmails = builder.preserveEmails;
        this.preserveUrls = builder.preserveUrls;
        this.minWordLength = builder.minWordLength;
    }
    public static class Builder {
        private HyphenHandling hyphenHandling = HyphenHandling.PRESERVE;
        private boolean preserveNumbers = true;
        private boolean preserveEmails = true;
        private boolean preserveUrls = false;
        private int minWordLength = 1;

        public Builder withHyphenHandling(HyphenHandling handling) {
            this.hyphenHandling = handling;
            return this;
        }

        public Builder withPreserveNumbers(boolean preserve) {
            this.preserveNumbers = preserve;
            return this;
        }

        public Builder withPreserveEmails(boolean preserve) {
            this.preserveEmails = preserve;
            return this;
        }

        public Builder withPreserveUrls(boolean preserve) {
            this.preserveUrls = preserve;
            return this;
        }

        public Builder withMinWordLength(int length) {
            if (length < 0) throw new IllegalArgumentException("Min word length cannot be negative");
            this.minWordLength = length;
            return this;
        }

        public TokenizationConfig build() {
            return new TokenizationConfig(this);
        }
    }

    public HyphenHandling getHyphenHandling() {
        return hyphenHandling;
    }

    public boolean isPreserveNumbers() {
        return preserveNumbers;
    }

    public boolean isPreserveEmails() {
        return preserveEmails;
    }

    public boolean isPreserveUrls() {
        return preserveUrls;
    }

    public int getMinWordLength() {
        return minWordLength;
    }
}
