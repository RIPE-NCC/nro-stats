package net.nro.stats.parser;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ParserTest {

    private Parser sut;

    @Before
    public void beforeEach() {
        sut = new Parser();
    }

    @Test
    public void testParseFileWithComments() {
        testFileAndAssertLineCount("parser/file_with_comments.txt", 0, "Does not parse file with comments correctly");
    }


    @Test
    public void testParseFileWithContent() {
        testFileAndAssertLineCount("parser/file_with_content.txt", 10, "Does not parse file with Header, Summary lines and IPv4/6 and ASN records correctly");
    }

    @Test
    public void testParseFileWithRecordExtensions() {
        testFileAndAssertLineCount("parser/file_with_extensions.txt", 10, "Does not parse file with extensions correctly");
    }

    @Test
    public void testParseFileComplete() {
        testFileAndAssertLineCount("parser/file_complete.txt", 10, "Does not parse file with everything mixed correctly");
    }

    private void testFileAndAssertLineCount(String filePath, int expectedRows, String message) {
        URL testFile = this.getClass().getClassLoader().getResource(filePath);
        assert testFile != null;
        String testFilePath = testFile.getPath();
        List<Line> lines = sut.parse(testFilePath);
        assertTrue(message, lines.size() == expectedRows);
    }
}