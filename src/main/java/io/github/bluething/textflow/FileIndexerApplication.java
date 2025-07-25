package io.github.bluething.textflow;

import io.github.bluething.textflow.domain.FileIndexerService;
import io.github.bluething.textflow.domain.FileIndexerServiceImpl;
import io.github.bluething.textflow.domain.FileProcessingResult;
import io.github.bluething.textflow.domain.IndexerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class FileIndexerApplication {

    private static final Logger logger = LoggerFactory.getLogger(FileIndexerApplication.class);

    public static void main(String[] args) {
        var app = new FileIndexerApplication();

        var exitCode = switch (args.length) {
            case 0 -> {
                app.printUsage();
                yield 1;
            }
            default -> app.run(args);
        };

        System.exit(exitCode);
    }

    public int run(String[] args) {
        try {
            logger.info("Starting Indexer with {} files", args.length);

            List<Path> filePaths = Arrays.stream(args)
                    .map(Paths::get)
                    .toList();

            var indexerService = createIndexerService();
            var results = indexerService.processFiles(filePaths);

            displayResults(results);

            logger.info("File indexing completed successfully");
            return 0;

        } catch (Exception e) {
            logger.error("Application failed with error: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
    }
    private FileIndexerService createIndexerService() {
        var config = IndexerConfiguration.defaultConfiguration();

        return new FileIndexerServiceImpl(config);
    }
    private void displayResults(List<FileProcessingResult> results) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("FILE INDEXING RESULTS");
        System.out.println("=".repeat(80));

        results.forEach(this::displaySingleResult);
    }
    private void displaySingleResult(FileProcessingResult result) {
        if (result.isSuccess()) {
            System.out.println("File: " + result.fileName());
            System.out.println("Processing Time: " + result.processingTimeMs() + " ms");
            System.out.println("File Size: " + formatFileSize(result.fileSizeBytes()));

            result.indexingResults().forEach((ruleName, ruleResult) ->
                    System.out.println(STR."\{ruleName}: \{ruleResult.getDisplayValue()}"));

            System.out.println();
        } else {
            System.out.println("File: " + result.fileName());
            System.out.println("ERROR: " + result.errorMessage());
            System.out.println("Processing Time: " + result.processingTimeMs() + " ms");
            if (result.fileSizeBytes() > 0) {
                System.out.println("File Size: " + formatFileSize(result.fileSizeBytes()));
            }
            System.out.println();
        }

        System.out.println("-".repeat(80));
    }
    private String formatFileSize(long bytes) {
        if (bytes == 0) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024L * 1024L) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }

    private void printUsage() {
        System.out.println("""
            Usage: java -jar smart-content-indexer.jar <file1> <file2> ... <fileN>
            
            Example Usage:
            =============
            java -jar smart-content-indexer.jar document.txt webpage.html data.json
            java -jar smart-content-indexer.jar /path/to/documents/*.txt
            java -jar smart-content-indexer.jar large-dataset.json huge-log.txt
            
            Supported file formats (auto-detected by content):
            ================================================
            • Text files (.txt, .log) - Plain text with memory mapping for large files
            • HTML files (.html, .htm) - Web pages with metadata extraction
            • JSON files (.json) - Structured data with field names included
            """);
    }
}
