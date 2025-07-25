package io.github.bluething.textflow.domain;

import java.nio.file.Path;
import java.util.List;

public interface FileIndexerService {
    /**
     * Processes multiple files concurrently and returns the results.
     *
     * @param filePaths List of file paths to process
     * @return List of processing results, one per file
     */
    List<FileProcessingResult> processFiles(List<Path> filePaths);

    /**
     * Processes a single file and returns the result.
     *
     * @param filePath The file path to process
     * @return The processing result
     */
    FileProcessingResult processFile(Path filePath);
}
