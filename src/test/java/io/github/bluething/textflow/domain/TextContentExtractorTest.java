package io.github.bluething.textflow.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
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

}