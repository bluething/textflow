package io.github.bluething.textflow.domain;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestFileUtils {
    private TestFileUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Creates a temporary HTML file with the specified content.
     *
     * @param tempDir the temporary directory to create the file in
     * @param fileName the name of the file to create
     * @param htmlContent the HTML content to write to the file
     * @return the path to the created file
     * @throws IOException if an I/O error occurs
     */
    public static Path createHtmlFile(Path tempDir, String fileName, String htmlContent) throws IOException {
        Path filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, htmlContent);
        return filePath;
    }

    /**
     * Creates a simple HTML document with the specified title and body content.
     *
     * @param title the title of the HTML document
     * @param bodyContent the content to include in the body
     * @return the complete HTML document as a string
     */
    public static String createSimpleHtmlDocument(String title, String bodyContent) {
        return String.format("""
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>%s</title>
            </head>
            <body>
                %s
            </body>
            </html>
            """, title, bodyContent);
    }

    /**
     * Creates a complex HTML document with various elements for testing.
     *
     * @return a complex HTML document with multiple elements
     */
    public static String createComplexHtmlDocument() {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Complex Test Document</title>
                <style>
                    body { font-family: Arial, sans-serif; }
                    .hidden { display: none; }
                </style>
                <script>
                    function greet() {
                        alert('Hello World');
                    }
                </script>
            </head>
            <body>
                <header>
                    <h1>Main Title</h1>
                    <nav>
                        <ul>
                            <li><a href="#section1">Section 1</a></li>
                            <li><a href="#section2">Section 2</a></li>
                        </ul>
                    </nav>
                </header>
                
                <main>
                    <section id="section1">
                        <h2>Section 1 Heading</h2>
                        <p>This is a paragraph with <strong>bold text</strong> and <em>italic text</em>.</p>
                        <div class="hidden">This content should be hidden</div>
                    </section>
                    
                    <section id="section2">
                        <h2>Section 2 Heading</h2>
                        <table>
                            <tr>
                                <th>Column 1</th>
                                <th>Column 2</th>
                            </tr>
                            <tr>
                                <td>Row 1 Col 1</td>
                                <td>Row 1 Col 2</td>
                            </tr>
                        </table>
                    </section>
                </main>
                
                <footer>
                    <p>&copy; 2024 Test Company</p>
                </footer>
            </body>
            </html>
            """;
    }

    /**
     * Creates an HTML document with malformed markup for testing error handling.
     *
     * @return an HTML document with malformed markup
     */
    public static String createMalformedHtmlDocument() {
        return """
            <html>
            <head>
                <title>Malformed Document
            </head>
            <body>
                <p>Unclosed paragraph
                <div>
                    <span>Nested unclosed span
                </div>
                <img src="test.jpg" alt="No closing tag"
            </body>
            """;
    }

    /**
     * Sample HTML documents for various test scenarios.
     */
    public static final class SampleDocuments {

        public static final String EMPTY_HTML = "<html></html>";

        public static final String MINIMAL_HTML = "<html><body>Hello World</body></html>";

        public static final String WITH_ENTITIES =
                "<html><body>Special chars: &lt; &gt; &amp; &quot; &#39;</body></html>";

        public static final String WITH_COMMENTS =
                "<html><body><!-- This is a comment -->Visible content</body></html>";

        public static final String WITH_CDATA =
                "<html><body><![CDATA[This is CDATA content]]></body></html>";

        public static final String XHTML_DOCUMENT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
                "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
                <title>XHTML Document</title>
            </head>
            <body>
                <p>This is XHTML content.</p>
            </body>
            </html>
            """;
    }

    /**
     * Test file names with various extensions and edge cases.
     */
    public static final class TestFileNames {

        public static final List<String> VALID_HTML_NAMES = List.of(
                "index.html",
                "page.htm",
                "document.HTML",
                "file.HTM",
                "test.HtMl",
                "my.backup.file.html",
                ".hidden.html",
                "file-with-dashes.html",
                "file_with_underscores.html",
                "file with spaces.html",
                "file123.html",
                "очень-длинное-имя-файла-на-русском-языке.html"
        );

        public static final List<String> INVALID_HTML_NAMES = List.of(
                "document.txt",
                "image.jpg",
                "data.json",
                "style.css",
                "script.js",
                "archive.zip",
                "htmlfile",
                "html.txt",
                "file.htmlx",
                "test.php",
                "page.asp"
        );
    }
}
