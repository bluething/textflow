package io.github.bluething.textflow.domain;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

/**
 * Strategy interface for extracting text content from different file formats.
 * This allows the system to support multiple file formats while keeping
 * the indexing logic format-agnostic.
 */
public interface ContentExtractor {
    /**
     * Checks if this extractor can handle the given file using content-based detection.
     */
    boolean canHandle(Path filePath, String detectedMimeType);

    /**
     * Extracts text content from the file.
     */
    TextContent extract(Path filePath, String detectedMimeType) throws IOException;

    /**
     * Gets the file type identifier for this extractor.
     */
    String getFileType();

    /**
     * MIME type support for content-based detection
     * */
    Set<String> getSupportedMimeTypes();
}
