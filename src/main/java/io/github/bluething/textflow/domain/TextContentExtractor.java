package io.github.bluething.textflow.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;

class TextContentExtractor implements ContentExtractor {
    private static final Logger logger = LoggerFactory.getLogger(TextContentExtractor.class);
    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "text/plain",
            "text/x-log",
            "application/octet-stream"
    );

    // File size threshold for memory mapping (10MB)
    private static final long MEMORY_MAP_THRESHOLD = 10 * 1024 * 1024;

    @Override
    public boolean canHandle(Path filePath, String detectedMimeType) {
        return switch (detectedMimeType) {
            case String mime when SUPPORTED_MIME_TYPES.contains(mime) -> true;
            case null -> {
                String fileName = filePath.getFileName().toString().toLowerCase();
                yield fileName.endsWith(".txt") || fileName.endsWith(".log");
            }
            default -> false;
        };
    }

    @Override
    public TextContent extract(Path filePath, String detectedMimeType) throws IOException {
        long fileSize = Files.size(filePath);

        // Use memory mapping for large files, regular I/O for small files
        String content = switch (Long.compare(fileSize, MEMORY_MAP_THRESHOLD)) {
            case 1 -> { // Large file - use memory mapping
                logger.debug("Using memory mapping for large file: {} ({} bytes)",
                        filePath.getFileName(), fileSize);
                yield readWithMemoryMapping(filePath);
            }
            default -> // Small file - regular I/O
                    Files.readString(filePath);
        };

        ContentMetadata metadata = ContentMetadata.of(null, "UTF-8", fileSize);

        return TextContent.of(content, filePath.getFileName().toString(),
                getFileType(), detectedMimeType != null ? detectedMimeType : "text/plain", metadata);
    }

    /**
     * Memory-mapped file reading for large files using Foreign Memory API.
     */
    private String readWithMemoryMapping(Path filePath) throws IOException {
        try (var fileChannel = FileChannel.open(filePath, StandardOpenOption.READ)) {
            var fileSize = fileChannel.size();

            // Map the file into memory
            var mappedBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);

            // Convert to string using charset
            var charset = StandardCharsets.UTF_8;
            return charset.decode(mappedBuffer).toString();

        } catch (Exception e) {
            logger.warn("Memory mapping failed for {}, falling back to regular I/O: {}",
                    filePath, e.getMessage());
            return Files.readString(filePath);
        }
    }

    @Override
    public String getFileType() {
        return "TEXT";
    }

    @Override
    public Set<String> getSupportedMimeTypes() {
        return SUPPORTED_MIME_TYPES;
    }
}
