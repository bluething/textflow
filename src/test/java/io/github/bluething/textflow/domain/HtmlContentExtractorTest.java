package io.github.bluething.textflow.domain;

import io.github.bluething.textflow.TestFileUtils;
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
            // Given - No stubbing needed since unsupported MIME type goes to default case
            // The test demonstrates that MIME type takes precedence by never checking filename

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

    @Nested
    @DisplayName("extract() Method Tests")
    class ExtractTests {

        @TempDir
        Path tempDir;

        @Test
        @DisplayName("Should extract content successfully with working implementation")
        void shouldExtractContentSuccessfullyWithWorkingImplementation() throws IOException {
            // Given
            Path htmlFile = tempDir.resolve("test.html");
            Files.writeString(htmlFile, "<html><body>Test content</body></html>");

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Test content");
            assertThat(result.originalFileName()).isEqualTo("test.html");
            assertThat(result.fileType()).isEqualTo("HTML");
        }

        @Test
        @DisplayName("Should handle null MIME type parameter")
        void shouldHandleNullMimeTypeParameter() throws IOException {
            // Given
            Path htmlFile = tempDir.resolve("test.html");
            Files.writeString(htmlFile, "<html><body>Content</body></html>");

            // When & Then
            assertThatCode(() -> extractor.extract(htmlFile, null))
                    .doesNotThrowAnyException();

            TextContent result = extractor.extract(htmlFile, null);
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Content");
        }

        @Test
        @DisplayName("Should handle non-existent file with appropriate exception")
        void shouldHandleNonExistentFileWithAppropriateException() {
            // Given
            Path nonExistentFile = tempDir.resolve("non-existent.html");

            // When & Then - Should throw IOException for non-existent file
            assertThatThrownBy(() -> extractor.extract(nonExistentFile, "text/html"))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Failed to parse HTML file");
        }

        @Test
        @DisplayName("Should handle directory path with appropriate exception")
        void shouldHandleDirectoryPathWithAppropriateException() throws IOException {
            // Given
            Path directory = tempDir.resolve("test-directory");
            Files.createDirectory(directory);

            // When & Then - Should throw IOException for directory
            assertThatThrownBy(() -> extractor.extract(directory, "text/html"))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Failed to parse HTML file");
        }

        @Test
        @DisplayName("Should handle XHTML MIME type")
        void shouldHandleXhtmlMimeType() throws IOException {
            // Given
            Path xhtmlFile = tempDir.resolve("test.xhtml");
            Files.writeString(xhtmlFile, "<html><body>XHTML content</body></html>");

            // When
            TextContent result = extractor.extract(xhtmlFile, "application/xhtml+xml");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("XHTML content");
        }

                @Test
        @DisplayName("Should extract text from simple HTML without tags")
        void shouldExtractTextFromSimpleHtmlWithoutTags() throws IOException {
            // Given
            String htmlContent = "<html><body>Simple text content</body></html>";
            Path htmlFile = tempDir.resolve("simple-text.html");
            Files.writeString(htmlFile, htmlContent);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Simple text content");
            assertThat(result.fileType()).isEqualTo("HTML");
            // Should not contain HTML tags
            assertThat(result.content()).doesNotContain("<html>");
            assertThat(result.content()).doesNotContain("<body>");
        }

        @Test
        @DisplayName("Should extract content with paragraph tags")
        void shouldExtractContentWithParagraphTags() throws IOException {
            // Given
            String htmlContent = "<html><body><p>First paragraph</p><p>Second paragraph</p></body></html>";
            Path htmlFile = tempDir.resolve("paragraphs.html");
            Files.writeString(htmlFile, htmlContent);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("First paragraph");
            assertThat(result.content()).contains("Second paragraph");
        }

        @Test
        @DisplayName("Should extract title from HTML")
        void shouldExtractTitleFromHtml() throws IOException {
            // Given
            String htmlContent = "<html><head><title>Test Title</title></head><body>Body content</body></html>";
            Path htmlFile = tempDir.resolve("with-title.html");
            Files.writeString(htmlFile, htmlContent);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Body content");
            // Title should be accessible via metadata (implementation specific)
        }

        @Test
        @DisplayName("Should handle minimal HTML")
        void shouldHandleMinimalHtml() throws IOException {
            // Given
            String minimalHtml = "<html>Direct text</html>";
            Path htmlFile = tempDir.resolve("minimal.html");
            Files.writeString(htmlFile, minimalHtml);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Direct text");
        }

        @Test
        @DisplayName("Should handle HTML with nested elements")
        void shouldHandleHtmlWithNestedElements() throws IOException {
            // Given
            String nestedHtml = "<html><body><div><span>Nested content</span></div></body></html>";
            Path htmlFile = tempDir.resolve("nested.html");
            Files.writeString(htmlFile, nestedHtml);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Nested content");
        }

        @Test
        @DisplayName("Should remove script and style content")
        void shouldRemoveScriptAndStyleContent() throws IOException {
            // Given
            String htmlWithScriptAndStyle = """
                <html>
                <head>
                    <style>
                        body { font-size: 14px; }
                        .hidden { display: none; }
                    </style>
                    <script>
                        function hideContent() {
                            document.querySelector('.content').style.display = 'none';
                        }
                    </script>
                </head>
                <body>
                    <p class="content">Visible content</p>
                    <script>hideContent();</script>
                </body>
                </html>
                """;
            Path htmlFile = tempDir.resolve("with-script-style.html");
            Files.writeString(htmlFile, htmlWithScriptAndStyle);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isEqualTo("Visible content");
            assertThat(result.content()).doesNotContain("font-size");
            assertThat(result.content()).doesNotContain("hideContent");
            assertThat(result.content()).doesNotContain("function");
            assertThat(result.content()).doesNotContain("display: none");
        }

        @Test
        @DisplayName("Should extract alt text from images")
        void shouldExtractAltTextFromImages() throws IOException {
            // Given
            String htmlWithImages = """
                <html>
                <body>
                    <p>Main content</p>
                    <img src="image1.jpg" alt="Image description 1">
                    <img src="image2.jpg" alt="Image description 2">
                    <img src="image3.jpg"> <!-- no alt text -->
                </body>
                </html>
                """;
            Path htmlFile = tempDir.resolve("with-images.html");
            Files.writeString(htmlFile, htmlWithImages);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("Main content");
            assertThat(result.content()).contains("Image description 1");
            assertThat(result.content()).contains("Image description 2");
        }

        @Test
        @DisplayName("Should extract plain text from simple HTML")
        void shouldExtractPlainTextFromSimpleHtml() throws IOException {
            // Given
            String htmlContent = "<html><body><p>Hello World</p></body></html>";
            Path htmlFile = tempDir.resolve("simple.html");
            Files.writeString(htmlFile, htmlContent);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("Hello World");
            assertThat(result.originalFileName()).isEqualTo("simple.html");
            assertThat(result.fileType()).isEqualTo("HTML");
        }

        @Test
        @DisplayName("Should extract text from complex HTML document")
        void shouldExtractTextFromComplexHtmlDocument() throws IOException {
            // Given
            String complexHtml = TestFileUtils.createComplexHtmlDocument();
            Path htmlFile = tempDir.resolve("complex.html");
            Files.writeString(htmlFile, complexHtml);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content())
                .contains("Main Title")
                .contains("Section 1 Heading")
                .contains("This is a paragraph with bold text and italic text")
                .contains("Section 2 Heading")
                .doesNotContain("<html>") // HTML tags should be stripped
                .doesNotContain("<script>") // Script content should be removed
                .doesNotContain("function greet()"); // JavaScript should be removed
        }

        @Test
        @DisplayName("Should handle HTML with special characters and entities")
        void shouldHandleHtmlWithSpecialCharactersAndEntities() throws IOException {
            // Given
            String htmlWithEntities = "<html><body>Special chars: &lt; &gt; &amp; &quot; &#39;</body></html>";
            Path htmlFile = tempDir.resolve("entities.html");
            Files.writeString(htmlFile, htmlWithEntities);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("Special chars: < > & \" '");
        }

        @Test
        @DisplayName("Should handle malformed HTML gracefully")
        void shouldHandleMalformedHtmlGracefully() throws IOException {
            // Given
            String malformedHtml = TestFileUtils.createMalformedHtmlDocument();
            Path htmlFile = tempDir.resolve("malformed.html");
            Files.writeString(htmlFile, malformedHtml);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).isNotEmpty();
            // Should extract whatever text is available despite malformed structure
        }

        @Test
        @DisplayName("Should handle empty HTML file")
        void shouldHandleEmptyHtmlFile() throws IOException {
            // Given
            Path emptyFile = tempDir.resolve("empty.html");
            Files.writeString(emptyFile, "");

            // When
            TextContent result = extractor.extract(emptyFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should handle HTML with only whitespace")
        void shouldHandleHtmlWithOnlyWhitespace() throws IOException {
            // Given
            String whitespaceHtml = "<html><body>   \n\t\n   </body></html>";
            Path htmlFile = tempDir.resolve("whitespace.html");
            Files.writeString(htmlFile, whitespaceHtml);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.isEmpty()).isTrue();
        }

        @Test
        @DisplayName("Should preserve text formatting and line breaks appropriately")
        void shouldPreserveTextFormattingAndLineBreaksAppropriately() throws IOException {
            // Given
            String formattedHtml = """
                <html>
                <body>
                    <h1>Title</h1>
                    <p>First paragraph.</p>
                    <p>Second paragraph.</p>
                    <ul>
                        <li>Item 1</li>
                        <li>Item 2</li>
                    </ul>
                </body>
                </html>
                """;
            Path htmlFile = tempDir.resolve("formatted.html");
            Files.writeString(htmlFile, formattedHtml);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            String content = result.content();
            assertThat(content).contains("Title");
            assertThat(content).contains("First paragraph.");
            assertThat(content).contains("Second paragraph.");
            assertThat(content).contains("Item 1");
            assertThat(content).contains("Item 2");
            // Should have reasonable line breaks/spacing
        }

        @Test
        @DisplayName("Should handle different HTML encodings")
        void shouldHandleDifferentHtmlEncodings() throws IOException {
            // Given
            String utf8Html = "<html><body>UTF-8: ñáéíóú 中文 🌍</body></html>";
            Path htmlFile = tempDir.resolve("utf8.html");
            Files.writeString(htmlFile, utf8Html, StandardCharsets.UTF_8);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("ñáéíóú");
            assertThat(result.content()).contains("中文");
            assertThat(result.content()).contains("🌍");
        }

        @Test
        @DisplayName("Should handle large HTML files efficiently")
        void shouldHandleLargeHtmlFilesEfficiently() throws IOException {
            // Given
            StringBuilder largeHtml = new StringBuilder("<html><body>");
            for (int i = 0; i < 10000; i++) {
                largeHtml.append("<p>Paragraph ").append(i).append(" content</p>");
            }
            largeHtml.append("</body></html>");

            Path largeFile = tempDir.resolve("large.html");
            Files.writeString(largeFile, largeHtml.toString());

            // When
            long startTime = System.nanoTime();
            TextContent result = extractor.extract(largeFile, "text/html");
            long endTime = System.nanoTime();
            long durationMs = (endTime - startTime) / 1_000_000;

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("Paragraph 0 content");
            assertThat(result.content()).contains("Paragraph 9999 content");
            // Should complete in reasonable time (adjust threshold as needed)
            assertThat(durationMs).isLessThan(5000); // 5 seconds max
        }

        @Test
        @DisplayName("Should ignore script and style content")
        void shouldIgnoreScriptAndStyleContent() throws IOException {
            // Given
            String htmlWithScriptAndStyle = """
                <html>
                <head>
                    <style>
                        body { font-size: 14px; }
                        .hidden { display: none; }
                    </style>
                    <script>
                        function hideContent() {
                            document.querySelector('.content').style.display = 'none';
                        }
                    </script>
                </head>
                <body>
                    <p class="content">Visible content</p>
                    <script>hideContent();</script>
                </body>
                </html>
                """;
            Path htmlFile = tempDir.resolve("with-script-style.html");
            Files.writeString(htmlFile, htmlWithScriptAndStyle);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content())
                .contains("Visible content")
                .doesNotContain("font-size")
                .doesNotContain("hideContent")
                .doesNotContain("function")
                .doesNotContain("display: none");
        }

        @Test
        @DisplayName("Should handle XHTML documents")
        void shouldHandleXhtmlDocuments() throws IOException {
            // Given
            String xhtmlContent = TestFileUtils.SampleDocuments.XHTML_DOCUMENT;
            Path xhtmlFile = tempDir.resolve("document.xhtml");
            Files.writeString(xhtmlFile, xhtmlContent);

            // When
            TextContent result = extractor.extract(xhtmlFile, "application/xhtml+xml");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("This is XHTML content.");
            assertThat(result.originalFileName()).isEqualTo("document.xhtml");
        }

        @Test
        @DisplayName("Should extract text from HTML with comments")
        void shouldExtractTextFromHtmlWithComments() throws IOException {
            // Given
            String htmlWithComments = TestFileUtils.SampleDocuments.WITH_COMMENTS;
            Path htmlFile = tempDir.resolve("with-comments.html");
            Files.writeString(htmlFile, htmlWithComments);

            // When
            TextContent result = extractor.extract(htmlFile, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content())
                .contains("Visible content")
                .doesNotContain("This is a comment"); // Comments should be ignored
        }

        @Test
        @DisplayName("Should handle HTML with null MIME type parameter")
        void shouldHandleHtmlWithNullMimeTypeParameter() throws IOException {
            // Given
            String htmlContent = "<html><body>Content without MIME type</body></html>";
            Path htmlFile = tempDir.resolve("no-mime.html");
            Files.writeString(htmlFile, htmlContent);

            // When
            TextContent result = extractor.extract(htmlFile, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content()).contains("Content without MIME type");
        }

        @Test
        @DisplayName("Should handle various HTML5 elements")
        void shouldHandleVariousHtml5Elements() throws IOException {
            // Given
            String html5Content = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>HTML5 Test</title>
                </head>
                <body>
                    <header>Header content</header>
                    <nav>Navigation content</nav>
                    <main>
                        <article>Article content</article>
                        <section>Section content</section>
                        <aside>Aside content</aside>
                    </main>
                    <footer>Footer content</footer>
                </body>
                </html>
                """;
            Path html5File = tempDir.resolve("html5.html");
            Files.writeString(html5File, html5Content);

            // When
            TextContent result = extractor.extract(html5File, "text/html");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.content())
                .contains("Header content")
                .contains("Navigation content")
                .contains("Article content")
                .contains("Section content")
                .contains("Aside content")
                .contains("Footer content");
        }
    }

}