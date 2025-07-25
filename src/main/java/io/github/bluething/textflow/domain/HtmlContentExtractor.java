package io.github.bluething.textflow.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

class HtmlContentExtractor implements ContentExtractor {
    private static final Logger logger = LoggerFactory.getLogger(HtmlContentExtractor.class);

    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "text/html",
            "application/xhtml+xml"
    );

    @Override
    public boolean canHandle(Path filePath, String detectedMimeType) {
        return switch (detectedMimeType) {
            case String mime when SUPPORTED_MIME_TYPES.contains(mime) -> true;
            case null -> {
                String fileName = filePath.getFileName().toString().toLowerCase();
                yield fileName.endsWith(".html") || fileName.endsWith(".htm");
            }
            default -> false;
        };
    }

    @Override
    public TextContent extract(Path filePath) throws IOException {
        return null;
    }

    @Override
    public String getFileType() {
        return "";
    }
}
