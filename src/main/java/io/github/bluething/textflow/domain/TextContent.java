package io.github.bluething.textflow.domain;

public record TextContent(String content,
                          String originalFileName,
                          String fileType,
                          String detectedMimeType,
                          ContentMetadata metadata) {
    /**
     * Creates TextContent with validation.
     */
    public static TextContent of(String content, String fileName, String fileType, String mimeType) {
        return of(content, fileName, fileType, mimeType, ContentMetadata.empty());
    }

    /**
     * Creates TextContent with metadata.
     */
    public static TextContent of(String content, String fileName, String fileType,
                                 String mimeType, ContentMetadata metadata) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new IllegalArgumentException("File type cannot be null or empty");
        }

        return new TextContent(content, fileName, fileType,
                mimeType != null ? mimeType : "unknown", metadata);
    }

    /**
     * Checks if the content is empty or contains only whitespace.
     */
    public boolean isEmpty() {
        return content.trim().isEmpty();
    }

    /**
     * Gets the content length in characters.
     */
    public int length() {
        return content.length();
    }
}
