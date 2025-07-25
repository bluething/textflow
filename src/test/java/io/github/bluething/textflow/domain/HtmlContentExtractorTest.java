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
@DisplayName("HtmlContentExtractor Tests")
class HtmlContentExtractorTest {
    private HtmlContentExtractor extractor;

    @Mock
    private Path mockPath;

    @BeforeEach
    void setUp() {
        extractor = new HtmlContentExtractor();
    }

    @Nested
    @DisplayName("canHandle() Method Tests")
    class CanHandleTests {

        @Nested
        @DisplayName("With MIME Type Detection")
        class WithMimeTypeTests {

            @ParameterizedTest
            @ValueSource(strings = {
                    "text/html",
                    "application/xhtml+xml"
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
                    "text/plain",
                    "application/pdf",
                    "image/jpeg",
                    "application/json",
                    "text/css",
                    "application/javascript"
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
                assertThat(extractor.canHandle(mockPath, "TEXT/HTML")).isFalse();
                assertThat(extractor.canHandle(mockPath, "Text/Html")).isFalse();
                assertThat(extractor.canHandle(mockPath, "text/html")).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("With File Extension Fallback")
    class WithFileExtensionTests {

        @ParameterizedTest
        @ValueSource(strings = {
                "index.html",
                "page.htm",
                "document.HTML",
                "file.HTM",
                "test.HtMl"
        })
        @DisplayName("Should handle HTML file extensions when MIME type is null")
        void shouldHandleHtmlExtensionsWhenMimeTypeIsNull(String fileName) {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(fileName));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "document.txt",
                "image.jpg",
                "data.json",
                "style.css",
                "script.js",
                "archive.zip",
                "htmlfile", // no extension
                "html.txt", // wrong extension
                "file.htmlx" // similar but different extension
        })
        @DisplayName("Should not handle non-HTML file extensions when MIME type is null")
        void shouldNotHandleNonHtmlExtensionsWhenMimeTypeIsNull(String fileName) {
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
            // Given - Stubbing needed since we're testing MIME type precedence over extension
            when(mockPath.getFileName()).thenReturn(Path.of("test.html"));

            // When
            boolean result = extractor.canHandle(mockPath, "text/plain");

            // Then - should return false because MIME type takes precedence
            assertThat(result).isFalse();
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
            lenient().when(mockPath.getFileName()).thenReturn(Path.of("test.html"));

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
            when(mockPath.getFileName()).thenReturn(Path.of("htmlfile"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should handle file path with multiple dots")
        void shouldHandleFilePathWithMultipleDots() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of("my.backup.file.html"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should handle file path starting with dot")
        void shouldHandleFilePathStartingWithDot() {
            // Given
            when(mockPath.getFileName()).thenReturn(Path.of(".html"));

            // When
            boolean result = extractor.canHandle(mockPath, null);

            // Then
            assertThat(result).isTrue();
        }
    }

}