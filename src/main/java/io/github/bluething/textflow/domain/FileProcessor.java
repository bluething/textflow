package io.github.bluething.textflow.domain;

import io.github.bluething.textflow.domain.rules.CountResult;
import io.github.bluething.textflow.domain.rules.IndexingRule;
import io.github.bluething.textflow.domain.rules.IndexingRuleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FileProcessor.class);

    private final ContentExtractorRegistry extractorRegistry;
    private final List<IndexingRule> indexingRules;

    public FileProcessor(ContentExtractorRegistry extractorRegistry, List<IndexingRule> indexingRules) {
        this.extractorRegistry = extractorRegistry;
        this.indexingRules = List.copyOf(indexingRules);
    }

    public FileProcessingResult processFile(Path filePath) {
        long startTime = System.currentTimeMillis();
        String fileName = filePath.getFileName().toString();
        long fileSize = 0;

        try {
            // Validate file exists and is readable
            if (!Files.exists(filePath)) {
                return createFailureResult(fileName, "File does not exist", startTime, 0);
            }

            if (!Files.isReadable(filePath)) {
                return createFailureResult(fileName, "File is not readable", startTime, 0);
            }

            fileSize = Files.size(filePath);
            logger.debug("Processing file: {} (size: {} bytes)", fileName, fileSize);

            // Extract content from file using Java 21 enhanced registry
            var extractor = extractorRegistry instanceof ContentExtractorRegistry contentExtractorRegistry
                    ? contentExtractorRegistry.findExtractor(filePath)
                    : extractorRegistry.findExtractor(filePath);

            TextContent textContent = extractor.extract(filePath, null);

            logger.debug("Extracted {} characters from {}", textContent.length(), fileName);

            // Apply all indexing rules
            Map<String, IndexingRuleResult> results = applyIndexingRules(textContent);

            long processingTime = System.currentTimeMillis() - startTime;
            logger.debug("Successfully processed {} in {} ms", fileName, processingTime);

            return FileProcessingResult.success(fileName, results, processingTime, fileSize);

        } catch (UnsupportedOperationException e) {
            logger.warn("Unsupported file format: {}", fileName);
            return createFailureResult(fileName, "Unsupported file format", startTime, fileSize);

        } catch (IOException e) {
            logger.error("IO error processing file {}: {}", fileName, e.getMessage());
            return createFailureResult(fileName, "IO error: " + e.getMessage(), startTime, fileSize);

        } catch (Exception e) {
            logger.error("Unexpected error processing file {}", fileName, e);
            return createFailureResult(fileName, "Unexpected error: " + e.getMessage(), startTime, fileSize);
        }
    }
    private Map<String, IndexingRuleResult> applyIndexingRules(TextContent content) {
        Map<String, IndexingRuleResult> results = new LinkedHashMap<>();

        for (IndexingRule rule : indexingRules) {
            try {
                long ruleStartTime = System.currentTimeMillis();
                IndexingRuleResult result = rule.apply(content);
                long ruleProcessingTime = System.currentTimeMillis() - ruleStartTime;

                results.put(rule.getName(), result);

                logger.debug("Applied rule '{}' in {} ms", rule.getName(), ruleProcessingTime);

            } catch (Exception e) {
                logger.error("Error applying rule '{}' to file {}: {}",
                        rule.getName(), content.originalFileName(), e.getMessage());

                // Continue with other rules even if one fails
                results.put(rule.getName(), new CountResult(0));
            }
        }

        return results;
    }

    /**
     * Creates a failure result with consistent timing information.
     */
    private FileProcessingResult createFailureResult(String fileName, String errorMessage,
                                                     long startTime, long fileSize) {
        long processingTime = System.currentTimeMillis() - startTime;
        return FileProcessingResult.failure(fileName, errorMessage, processingTime, fileSize);
    }

    /**
     * Gets the list of supported file types from the extractor registry.
     */
    public java.util.Set<String> getSupportedFileTypes() {
        return extractorRegistry.getSupportedFileTypes();
    }

    /**
     * Gets the names of all configured indexing rules.
     */
    public List<String> getIndexingRuleNames() {
        return indexingRules.stream()
                .map(IndexingRule::getName)
                .toList();
    }
}
