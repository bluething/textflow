package io.github.bluething.textflow.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

class TextContentExtractor implements ContentExtractor {
    private static final Logger logger = LoggerFactory.getLogger(TextContentExtractor.class);
    private static final Set<String> SUPPORTED_MIME_TYPES = Set.of(
            "text/plain",
            "text/x-log",
            "application/octet-stream"
    );

    @Override
    public boolean canHandle(Path filePath, String detectedMimeType) {
        return false;
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
