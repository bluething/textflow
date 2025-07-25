package io.github.bluething.textflow.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class FileIndexerServiceImpl implements FileIndexerService {

    private static final Logger logger = LoggerFactory.getLogger(FileIndexerServiceImpl.class);

    private final FileProcessor fileProcessor;
    private final IndexerConfiguration configuration;

    public FileIndexerServiceImpl(IndexerConfiguration configuration) {
        this.configuration = configuration;
        this.fileProcessor = new FileProcessor(
                configuration.getExtractorRegistry(),
                configuration.getIndexingRules()
        );
    }

    @Override
    public List<FileProcessingResult> processFiles(List<Path> filePaths) {
        return switch (filePaths.size()) {
            case 0 -> {
                logger.warn("No files provided for processing");
                yield List.of();
            }
            case 1 -> {
                logger.info("Processing single file: {}", filePaths.getFirst());
                yield List.of(processFile(filePaths.getFirst()));
            }
            default -> {
                logger.info("Starting concurrent processing of {} files using Virtual Threads", filePaths.size());
                yield processFilesConcurrently(filePaths);
            }
        };
    }
    private List<FileProcessingResult> processFilesConcurrently(List<Path> filePaths) {
        // Pre-validate files to fail fast
        List<Path> validatedPaths = validateFiles(filePaths);

        // Use Virtual Threads for massive concurrency without thread limits
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            List<CompletableFuture<FileProcessingResult>> futures = validatedPaths.stream()
                    .map(path -> CompletableFuture.supplyAsync(() ->
                            processFileWithSizeCheck(path), executor))
                    .toList();

            return futures.stream()
                    .map(CompletableFuture::join)
                    .toList();
        }
    }
    private List<Path> validateFiles(List<Path> filePaths) {
        return filePaths.stream()
                .filter(this::isValidFile)
                .toList();
    }
    private boolean isValidFile(Path path) {
        return switch (path) {
            case null -> {
                logger.warn("Skipping null file path");
                yield false;
            }
            case Path p when !Files.exists(p) -> {
                logger.warn("Skipping non-existent file: {}", p);
                yield false;
            }
            case Path p when Files.isDirectory(p) -> {
                logger.warn("Skipping directory: {}", p);
                yield false;
            }
            case Path p when !Files.isReadable(p) -> {
                logger.warn("Skipping unreadable file: {}", p);
                yield false;
            }
            default -> true;
        };
    }
    private FileProcessingResult processFileWithSizeCheck(Path filePath) {
        try {
            long fileSize = Files.size(filePath);
            String fileName = filePath.getFileName().toString();

            return switch (Long.compare(fileSize, configuration.getMaxFileSizeBytes())) {
                case 1 -> { // fileSize > maxSize
                    String error = "File size (" + fileSize + " bytes) exceeds maximum allowed size (" + configuration.getMaxFileSizeBytes() + " bytes)";
                    logger.warn("Skipping large file: {} - {}", filePath, error);
                    yield FileProcessingResult.failure(fileName, error, 0, fileSize);
                }
                default -> fileProcessor.processFile(filePath);
            };

        } catch (Exception e) {
            logger.error("Error checking file size for {}: {}", filePath, e.getMessage());
            return FileProcessingResult.failure(
                    filePath.getFileName().toString(),
                    "Error accessing file: " + e.getMessage(),
                    0,
                    0
            );
        }
    }

    @Override
    public FileProcessingResult processFile(Path filePath) {
        return processFileWithSizeCheck(filePath);
    }
}
