package io.github.bluething.textflow.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("TextContentExtractor Tests")
class TextContentExtractorTest {

    private TextContentExtractor extractor;

    @Mock
    private Path mockPath;

    @BeforeEach
    void setUp() {
        extractor = new TextContentExtractor();
    }

    @Nested
    @DisplayName("canHandle() Method Tests")
    class CanHandleTests {

        @Nested
        @DisplayName("With MIME Type Detection")
        class WithMimeTypeTests {

            @ParameterizedTest
            @ValueSource(strings = {
                    "text/plain",
                    "text/x-log",
                    "application/octet-stream"
            })
            @DisplayName("Should handle supported MIME types")
            void shouldHandleSupportedMimeTypes(String mimeType) {
                // Given - No stubbing needed when MIME type is provided (takes precedence over filename)

                // When
                boolean result = extractor.canHandle(mockPath, mimeType);

                // Then
                assertThat(result).isTrue();
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "text/html",
                    "application/pdf",
                    "image/jpeg",
                    "application/json",
                    "text/css",
                    "application/javascript",
                    "text/xml",
                    "application/xml",
                    "text/csv",
                    "text/markdown"
            })
            @DisplayName("Should not handle unsupported MIME types")
            void shouldNotHandleUnsupportedMimeTypes(String mimeType) {
                // Given - No stubbing needed when MIME type is provided (takes precedence over filename)

                // When
                boolean result = extractor.canHandle(mockPath, mimeType);

                // Then
                assertThat(result).isFalse();
            }

            @Test
            @DisplayName("Should handle MIME type case sensitivity correctly")
            void shouldHandleMimeTypeCaseSensitivity() {
                // Given - No stubbing needed when MIME type is provided

                // When & Then - MIME types should be case-sensitive
                assertThat(extractor.canHandle(mockPath, "TEXT/PLAIN")).isFalse();
                assertThat(extractor.canHandle(mockPath, "Text/Plain")).isFalse();
                assertThat(extractor.canHandle(mockPath, "text/plain")).isTrue();
            }

            @Test
            @DisplayName("Should handle application/octet-stream for binary text files")
            void shouldHandleOctetStreamForBinaryTextFiles() {
                // Given - No stubbing needed when MIME type is provided

                // When
                boolean result = extractor.canHandle(mockPath, "application/octet-stream");

                // Then
                assertThat(result).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("With File Extension Fallback")
    class WithFileExtensionTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "document.txt",
                "application.log",
                "readme.TXT",
                "error.LOG",
                "file.Txt",
                "debug.Log"
        })
        @DisplayName("Should handle text file extensions when MIME type is null")
        void shouldHandleTextExtensionsWhenMimeTypeIsNull(String fileName) {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(fileName));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "document.html",
                "image.jpg",
                "data.json",
                "style.css",
                "script.js",
                "archive.zip",
                "textfile", // no extension
                "txt.html", // wrong extension
                "file.txtx", // similar but different extension
                "log.pdf", // wrong extension
                "application.logger" // similar but different extension
        })
        @DisplayName("Should not handle non-text file extensions when MIME type is null")
        void shouldNotHandleNonTextExtensionsWhenMimeTypeIsNull(String fileName) {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(fileName));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should prefer MIME type over file extension")
        void shouldPreferMimeTypeOverFileExtension() {

            // When
            boolean result = extractor.canHandle(mockPath, "text/html");

            // Then - should return false because MIME type takes precedence
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle compound file extensions correctly")
        void shouldHandleCompoundFileExtensionsCorrectly() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of("backup.2024.txt"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }
    }
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should handle edge case MIME types gracefully")
        void shouldHandleEdgeCaseMimeTypes(String mimeType) {
            // Given - Use lenient stubbing since it's only needed for null mimeType
            lenient().when(mockPath.getFileName()).thenReturn(Path.of("test.txt"));

            // When
            boolean result = extractor.canHandle(mockPath, mimeType);

            // Then - null should fall back to extension check, others should return false
            if (mimeType == null) {
                assertThat(result).isTrue(); // falls back to extension check
            } else {
                assertThat(result).isFalse(); // whitespace-only strings are not supported
            }
        }

        @Test
        @DisplayName("Should handle file path without extension")
        void shouldHandleFilePathWithoutExtension() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of("textfile"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle file path with multiple dots")
        void shouldHandleFilePathWithMultipleDots() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of("my.backup.file.txt"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle file path starting with dot")
        void shouldHandleFilePathStartingWithDot() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(".txt"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle hidden log files")
        void shouldHandleHiddenLogFiles() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(".hidden.log"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle files with uppercase extensions")
        void shouldHandleFilesWithUppercaseExtensions() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of("README.TXT"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("Special File Types")
    class SpecialFileTypeTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "application.log",
                "access.log",
                "error.log",
                "debug.log",
                "system.log",
                "audit.log"
        })
        @DisplayName("Should handle various log file types")
        void shouldHandleVariousLogFileTypes(String fileName) {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(fileName));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "readme.txt",
                "LICENSE.txt",
                "CHANGELOG.txt",
                "notes.txt",
                "config.txt"
        })
        @DisplayName("Should handle common text file types")
        void shouldHandleCommonTextFileTypes(String fileName) {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(fileName));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }
    }

    @Nested
    @DisplayName("extract() Method Tests")
    class ExtractTests {
        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should extract content from plain text file")
        void shouldExtractContentFromPlainTextFile() throws IOException {
            // Given
            String textContent = "Hello World\nThis is a test file.";
            Path textFile = tempDir.resolve("simple.txt");
            Files.writeString(textFile, textContent);

            // When
            TextContent result = extractor.extract(textFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(textContent);
            assertThat(result.originalFileName()).isEqualTo("simple.txt");
            assertThat(result.fileType()).isEqualTo("TEXT");
        }

        @Test
        @DisplayName("Should extract content from log file")
        void shouldExtractContentFromLogFile() throws IOException {
            // Given
            String logContent = "2024-01-01 10:00:00 INFO Application started\n" +
                    "2024-01-01 10:00:01 ERROR Connection failed";
            Path logFile = tempDir.resolve("application.log");
            Files.writeString(logFile, logContent);

            // When
            TextContent result = extractor.extract(logFile, "text/x-log");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(logContent);
            assertThat(result.originalFileName()).isEqualTo("application.log");
            assertThat(result.fileType()).isEqualTo("TEXT");
        }

        @Test
        @DisplayName("Should extract content with null MIME type")
        void shouldExtractContentWithNullMimeType() throws IOException {
            // Given
            String textContent = "Content without MIME type";
            Path textFile = tempDir.resolve("no-mime.txt");
            Files.writeString(textFile, textContent);

            // When
            TextContent result = extractor.extract(textFile, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(textContent);
            assertThat(result.originalFileName()).isEqualTo("no-mime.txt");
            assertThat(result.fileType()).isEqualTo("TEXT");
        }

        @Test
        @DisplayName("Should handle empty text files")
        void shouldHandleEmptyTextFiles() throws IOException {
            // Given
            Path emptyFile = tempDir.resolve("empty.txt");
            Files.writeString(emptyFile, "");

            // When
            TextContent result = extractor.extract(emptyFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();
            assertThat(result.isEmpty()).isTrue();
            assertThat(result.originalFileName()).isEqualTo("empty.txt");
            assertThat(result.fileType()).isEqualTo("TEXT");
        }

        @Test
        @DisplayName("Should handle files with only whitespace")
        void shouldHandleFilesWithOnlyWhitespace() throws IOException {
            // Given
            String whitespaceContent = "   \n\t\n   ";
            Path whitespaceFile = tempDir.resolve("whitespace.txt");
            Files.writeString(whitespaceFile, whitespaceContent);

            // When
            TextContent result = extractor.extract(whitespaceFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(whitespaceContent);
            assertThat(result.isEmpty()).isTrue(); // isEmpty() should return true for whitespace-only
            assertThat(result.originalFileName()).isEqualTo("whitespace.txt");
        }

        @Test
        @DisplayName("Should handle files with different encodings")
        void shouldHandleFilesWithDifferentEncodings() throws IOException {
            // Given
            String utf8Content = "UTF-8 content: ñáéíóú 中文 🌍";
            Path utf8File = tempDir.resolve("utf8.txt");
            Files.writeString(utf8File, utf8Content, StandardCharsets.UTF_8);

            // When
            TextContent result = extractor.extract(utf8File, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(utf8Content);
            assertThat(result.content()).contains("ñáéíóú");
            assertThat(result.content()).contains("中文");
            assertThat(result.content()).contains("🌍");
        }

        @Test
        @DisplayName("Should handle small files with regular I/O")
        void shouldHandleSmallFilesWithRegularIO() throws IOException {
            // Given - Create a small file (under 10MB threshold)
            String smallContent = "This is a small file content.";
            Path smallFile = tempDir.resolve("small.txt");
            Files.writeString(smallFile, smallContent);

            // When
            TextContent result = extractor.extract(smallFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(smallContent);
            assertThat(Files.size(smallFile)).isLessThan(10 * 1024 * 1024); // Confirm it's under threshold
        }

        @Test
        @DisplayName("Should handle large files with memory mapping")
        void shouldHandleLargeFilesWithMemoryMapping() throws IOException {
            // Given - Create a large file (over 10MB threshold)
            StringBuilder largeContent = new StringBuilder();
            // Create approximately 11MB of content
            String line = "This is line %d with some content to make the file large enough for memory mapping.%n";
            for (int i = 1; i <= 200000; i++) {
                largeContent.append(String.format(line, i));
            }

            Path largeFile = tempDir.resolve("large.txt");
            Files.writeString(largeFile, largeContent.toString());

            // Verify file is large enough
            long fileSize = Files.size(largeFile);
            assertThat(fileSize).isGreaterThan(10 * 1024 * 1024); // Should be over 10MB

            // When
            long startTime = System.nanoTime();
            TextContent result = extractor.extract(largeFile, "text/plain");
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(largeContent.length());
            assertThat(result.content()).startsWith("This is line 1");
            assertThat(result.content()).contains("This is line 200000");
            // Should complete in reasonable time
            assertThat(durationMs).isLessThan(5000); // 5 seconds max
        }

        @Test
        @DisplayName("Should preserve line endings and formatting")
        void shouldPreserveLineEndingsAndFormatting() throws IOException {
            // Given
            String formattedContent = "First line\n" +
                    "Second line with spaces    \n" +  // 4 trailing spaces
                    "\tTab-indented line\n" +
                    "\n" +
                    "Line after empty line\n" +
                    "Final line";
            Path textFile = tempDir.resolve("formatted.txt");
            Files.writeString(textFile, formattedContent);

            // When
            TextContent result = extractor.extract(textFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(formattedContent);
            // Verify specific formatting is preserved
            assertThat(result.content()).contains("spaces    ");
            assertThat(result.content()).contains("\t");
            assertThat(result.content()).contains("\n\n");
        }

        @Test
        @DisplayName("Should handle files with special characters")
        void shouldHandleFilesWithSpecialCharacters() throws IOException {
            // Given
            String specialContent = """
                Special characters test:
                Quotes: "double" 'single' `backtick`
                Symbols: @#$%^&*()_+-=[]{}|;:,.<>?
                Accents: àáâãäåæçèéêëìíîïñòóôõöøùúûüý
                Math: ±×÷≠≤≥∞∑∏∆√∫∂
                Currency: $€£¥¢₹₽₩₨
                """;
            Path specialFile = tempDir.resolve("special.txt");
            Files.writeString(specialFile, specialContent);

            // When
            TextContent result = extractor.extract(specialFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(specialContent);
            assertThat(result.content()).contains("@#$%^&*()");
            assertThat(result.content()).contains("àáâãäå");
            assertThat(result.content()).contains("±×÷≠≤≥");
        }

        @Test
        @DisplayName("Should extract structured log content correctly")
        void shouldExtractStructuredLogContentCorrectly() throws IOException {
            // Given
            String structuredLog = """
                2024-01-01 10:00:00 INFO Application started successfully
                2024-01-01 10:00:01 DEBUG Loading configuration from config.properties
                2024-01-01 10:00:02 INFO Database connection established
                2024-01-01 10:00:03 WARN Deprecated API method called
                2024-01-01 10:00:04 ERROR Failed to process user request
                2024-01-01 10:00:05 FATAL Critical system failure detected
                """;
            Path logFile = tempDir.resolve("structured.log");
            Files.writeString(logFile, structuredLog);

            // When
            TextContent result = extractor.extract(logFile, "text/x-log");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(structuredLog);
            assertThat(result.content()).contains("INFO Application started");
            assertThat(result.content()).contains("ERROR Failed to process");
            assertThat(result.content()).contains("FATAL Critical system failure");
        }

        @Test
        @DisplayName("Should handle files with mixed content types")
        void shouldHandleFilesWithMixedContentTypes() throws IOException {
            // Given
            String mixedContent = """
                Plain text content
                Numbers: 123, 456.78, -999
                Special chars: @#$%^&*()
                Unicode: ñáéíóú 中文 🌍
                URLs: https://example.com
                Emails: test@example.com
                Dates: 2024-01-15 10:30:45
                """;
            Path mixedFile = tempDir.resolve("mixed.txt");
            Files.writeString(mixedFile, mixedContent);

            // When
            TextContent result = extractor.extract(mixedFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(mixedContent);
            assertThat(result.content()).contains("https://example.com");
            assertThat(result.content()).contains("test@example.com");
            assertThat(result.content()).contains("2024-01-15");
        }

        @Test
        @DisplayName("Should handle Windows vs Unix line endings")
        void shouldHandleWindowsVsUnixLineEndings() throws IOException {
            // Given
            String windowsContent = "Line 1\r\nLine 2\r\nLine 3";
            String unixContent = "Line 1\nLine 2\nLine 3";

            Path windowsFile = tempDir.resolve("windows.txt");
            Path unixFile = tempDir.resolve("unix.txt");

            Files.writeString(windowsFile, windowsContent);
            Files.writeString(unixFile, unixContent);

            // When
            TextContent windowsResult = extractor.extract(windowsFile, "text/plain");
            TextContent unixResult = extractor.extract(unixFile, "text/plain");

            // Then
            assertThat(windowsResult).isNotNull();
            assertThat(unixResult).isNotNull();

            // Should preserve original line endings
            assertThat(windowsResult.content()).isEqualTo(windowsContent);
            assertThat(unixResult.content()).isEqualTo(unixContent);

            // Both should contain the same logical content
            assertThat(windowsResult.content()).contains("Line 1");
            assertThat(windowsResult.content()).contains("Line 2");
            assertThat(windowsResult.content()).contains("Line 3");

            assertThat(unixResult.content()).contains("Line 1");
            assertThat(unixResult.content()).contains("Line 2");
            assertThat(unixResult.content()).contains("Line 3");
        }

        @Test
        @DisplayName("Should handle application/octet-stream MIME type")
        void shouldHandleOctetStreamMimeType() throws IOException {
            // Given
            String content = "Binary file detected as text";
            Path file = tempDir.resolve("binary.dat");
            Files.writeString(file, content);

            // When
            TextContent result = extractor.extract(file, "application/octet-stream");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo(content);
            assertThat(result.originalFileName()).isEqualTo("binary.dat");
        }

        @Test
        @DisplayName("Should handle non-existent file with appropriate exception")
        void shouldHandleNonExistentFileWithAppropriateException() {
            // Given
            Path nonExistentFile = tempDir.resolve("non-existent.txt");

            // When & Then
            assertThatThrownBy(() -> extractor.extract(nonExistentFile, "text/plain"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should handle directory path with appropriate exception")
        void shouldHandleDirectoryPathWithAppropriateException() throws IOException {
            // Given
            Path directory = tempDir.resolve("test-directory");
            Files.createDirectory(directory);

            // When & Then
            assertThatThrownBy(() -> extractor.extract(directory, "text/plain"))
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should fallback from memory mapping to regular I/O on failure")
        void shouldFallbackFromMemoryMappingToRegularIOOnFailure() throws IOException {
            // Given - Create a large file that should trigger memory mapping
            StringBuilder largeContent = new StringBuilder();
            String line = "Fallback test line %d with content for memory mapping failure test.%n";
            for (int i = 1; i <= 200000; i++) {
                largeContent.append(String.format(line, i));
            }

            Path largeFile = tempDir.resolve("large-fallback.txt");
            Files.writeString(largeFile, largeContent.toString());

            // Verify file is large enough to trigger memory mapping
            assertThat(Files.size(largeFile)).isGreaterThan(10 * 1024 * 1024);

            // When - Extract should work even if memory mapping fails internally
            TextContent result = extractor.extract(largeFile, "text/plain");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(largeContent.length());
            assertThat(result.content()).startsWith("Fallback test line 1");
        }
    }

}