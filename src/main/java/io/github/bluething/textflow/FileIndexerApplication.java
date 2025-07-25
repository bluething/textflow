package io.github.bluething.textflow;

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

            //TODO

            logger.info("File indexing completed successfully");
            return 0;

        } catch (Exception e) {
            logger.error("Application failed with error: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
            return 1;
        }
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
