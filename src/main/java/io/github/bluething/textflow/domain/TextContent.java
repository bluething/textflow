package io.github.bluething.textflow.domain;

public record TextContent(String content, String originalFileName, String fileType) {
    public static TextContent of(String content, String fileName, String fileType) {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty");
        }
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new IllegalArgumentException("File type cannot be null or empty");
        }

        return new TextContent(content, fileName, fileType);
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
