package io.github.bluething.textflow.domain;

import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContentExtractorRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ContentExtractorRegistry.class);

    private final Map<String, ContentExtractor> extractors = new HashMap<>();
    private final Tika tika = new Tika();

    public ContentExtractorRegistry() {
        registerDefaultExtractors();
    }

    private void registerDefaultExtractors() {
        register(new TextContentExtractor());
        register(new HtmlContentExtractor());
    }

    public void register(ContentExtractor extractor) {
        extractors.put(extractor.getFileType().toLowerCase(), extractor);
        logger.debug("Registered content extractor for type: {} with MIME types: {}",
                extractor.getFileType(), extractor.getSupportedMimeTypes());
    }

    public ContentExtractor findExtractor(Path filePath) throws IOException {
        String detectedMimeType = detectMimeType(filePath);
        String fileName = filePath.getFileName().toString().toLowerCase();

        logger.debug("Detected MIME type for {}: {}", filePath.getFileName(), detectedMimeType);

        // Use enhanced pattern matching for cleaner extractor selection
        return switch (detectedMimeType) {
            case "text/html", "application/xhtml+xml" -> extractors.get("html");
            case "application/json", "text/json" -> extractors.get("json");
            case "text/plain", "text/x-log" -> extractors.get("text");
            case null -> switch (getFileExtension(fileName)) {
                case ".txt", ".log" -> extractors.get("text");
                case ".html", ".htm" -> extractors.get("html");
                case ".json" -> extractors.get("json");
                default -> throw new UnsupportedOperationException(
                        "No content extractor found for file: " + filePath + " (MIME: " + detectedMimeType + ")");
            };
            case "application/octet-stream" -> switch (getFileExtension(fileName)) {
                case ".txt", ".log" -> extractors.get("text");
                case ".html", ".htm" -> extractors.get("html");
                case ".json" -> extractors.get("json");
                default -> throw new UnsupportedOperationException(
                        "No content extractor found for file: " + filePath + " (MIME: " + detectedMimeType + ")");
            };
            default -> {
                // Try to find extractor by MIME type
                ContentExtractor extractor = extractors.values().stream()
                        .filter(e -> e.getSupportedMimeTypes().contains(detectedMimeType))
                        .findFirst()
                        .orElse(null);

                yield extractor != null ? extractor : extractors.get("text"); // fallback
            }
        };
    }

    /**
     * Extract file extension using pattern matching.
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return switch (lastDot) {
            case -1 -> "";
            default -> fileName.substring(lastDot);
        };
    }

    private String detectMimeType(Path filePath) throws IOException {
        try {
            return tika.detect(filePath.toFile());
        } catch (Exception e) {
            logger.warn("Failed to detect MIME type for {}: {}", filePath, e.getMessage());
            return "application/octet-stream";
        }
    }

    public Set<String> getSupportedFileTypes() {
        return Set.copyOf(extractors.keySet());
    }
}
