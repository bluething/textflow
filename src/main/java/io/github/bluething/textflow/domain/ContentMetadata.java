package io.github.bluething.textflow.domain;

import java.util.Map;

public record ContentMetadata(String title,
                              String encoding,
                              long originalSizeBytes,
                              Map<String, String> additionalProperties) {
    public static ContentMetadata empty() {
        return new ContentMetadata(null, "UTF-8", 0, java.util.Map.of());
    }

    public static ContentMetadata of(String title, String encoding, long size) {
        return new ContentMetadata(title, encoding, size, java.util.Map.of());
    }

    public ContentMetadata withProperty(String key, String value) {
        var newProps = new java.util.HashMap<>(additionalProperties);
        newProps.put(key, value);
        return new ContentMetadata(title, encoding, originalSizeBytes, newProps);
    }
}
