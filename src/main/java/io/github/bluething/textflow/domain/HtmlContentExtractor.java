package io.github.bluething.textflow.domain;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
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
    public TextContent extract(Path filePath, String detectedMimeType) throws IOException {
        try {
            String htmlContent = Files.readString(filePath);
            var doc = Jsoup.parse(htmlContent);

            // Enhanced extraction using pattern matching
            var extractionResult = extractContentFromDocument(doc);

            long fileSize = Files.size(filePath);
            ContentMetadata metadata = ContentMetadata.of(
                            extractionResult.title(), "UTF-8", fileSize)
                    .withProperty("meta-description", extractionResult.description())
                    .withProperty("meta-keywords", extractionResult.keywords());

            logger.debug("Extracted {} characters from HTML file: {} (title: '{}')",
                    extractionResult.content().length(), filePath.getFileName(), extractionResult.title());

            return TextContent.of(extractionResult.content(), filePath.getFileName().toString(),
                    getFileType(), detectedMimeType != null ? detectedMimeType : "text/html", metadata);

        } catch (Exception e) {
            throw new IOException("Failed to parse HTML file: " + filePath, e);
        }
    }

    private HtmlExtractionResult extractContentFromDocument(Document doc) {
        String title = doc.title().isBlank() ? null : doc.title();

        // Remove script and style elements
        doc.select("script, style").remove();

        // Extract all text content
        String textContent = doc.body() != null ? doc.body().text() : doc.text();

        // Extract alt text from images
        StringBuilder fullContent = new StringBuilder(textContent);
        doc.select("img[alt]").forEach(img -> {
            String altText = img.attr("alt").trim();
            if (!altText.isEmpty()) {
                fullContent.append(" ").append(altText);
            }
        });

        String description = getMetaContent(doc, "description");
        String keywords = getMetaContent(doc, "keywords");

        return new HtmlExtractionResult(
                fullContent.toString().trim(),
                title,
                description,
                keywords
        );
    }

    private void extractTextWithStructure(org.jsoup.nodes.Element element, StringBuilder text) {
        for (var child : element.children()) {
            String tagName = child.tagName().toLowerCase();

            // Add space before block elements
            if (isBlockElement(tagName) && !text.isEmpty() &&
                    !text.toString().endsWith(" ") && !text.toString().endsWith("\n")) {
                text.append(" ");
            }

            // Recursively extract text
            if (child.hasText()) {
                String childText = child.ownText().trim();
                if (!childText.isEmpty()) {
                    text.append(childText);
                    if (isBlockElement(tagName)) {
                        text.append(" ");
                    }
                }
            }

            extractTextWithStructure(child, text);
        }
    }

    private boolean isBlockElement(String tagName) {
        return Set.of("div", "p", "h1", "h2", "h3", "h4", "h5", "h6",
                "section", "article", "header", "footer", "li").contains(tagName);
    }

    private String getMetaContent(org.jsoup.nodes.Document doc, String name) {
        var meta = doc.selectFirst("meta[name=" + name + "]");
        return meta != null ? meta.attr("content") : "";
    }

    private record HtmlExtractionResult(
            String content,
            String title,
            String description,
            String keywords
    ) {}

    @Override
    public String getFileType() {
        return "HTML";
    }

    @Override
    public Set<String> getSupportedMimeTypes() {
        return SUPPORTED_MIME_TYPES;
    }
}
