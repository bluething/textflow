package io.github.bluething.textflow.domain;

import io.github.bluething.textflow.domain.rules.IndexingRuleResult;

import java.util.Map;

public record FileProcessingResult(String fileName,
                                   boolean isSuccess,
                                   Map<String, IndexingRuleResult> indexingResults,
                                   String errorMessage,
                                   long processingTimeMs,
                                   long fileSizeBytes) {
    /**
     * Creates a successful processing result.
     */
    public static FileProcessingResult success(String fileName, Map<String, IndexingRuleResult> results, long processingTimeMs, long fileSizeBytes) {
        return new FileProcessingResult(fileName, true, results, null, processingTimeMs, fileSizeBytes);
    }

    /**
     * Creates a failed processing result.
     */
    public static FileProcessingResult failure(String fileName, String errorMessage, long processingTimeMs, long fileSizeBytes) {
        return new FileProcessingResult(fileName, false, Map.of(), errorMessage, processingTimeMs, fileSizeBytes);
    }
}
