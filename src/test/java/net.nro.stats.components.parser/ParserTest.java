package net.nro.stats.components.parser;

import org.junit.Before;
import org.junit.Test;

import java.io.*;
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
    public void testParseFileWithComments() throws IOException {
        testFileAndAssertLineCount("parser/file_with_comments.txt", 0, "Does not parse file with comments correctly");
    }


    @Test
    public void testParseFileWithContent() throws IOException {
        testFileAndAssertLineCount("parser/file_with_content.txt", 10, "Does not parse file with Header, Summary lines and IPv4/6 and ASN records correctly");
    }

    @Test
    public void testParseFileWithRecordExtensions() throws IOException {
        testFileAndAssertLineCount("parser/file_with_extensions.txt", 10, "Does not parse file with extensions correctly");
    }

    @Test
    public void testParseFileComplete() throws IOException {
        testFileAndAssertLineCount("parser/file_complete.txt", 10, "Does not parse file with everything mixed correctly");
    }

    private void testFileAndAssertLineCount(String filePath, int expectedRows, String message) throws IOException {
        URL testFile = this.getClass().getClassLoader().getResource(filePath);
        assert testFile != null;
        String testFilePath = testFile.getPath();
        List<Line> lines = sut.parse(bytesFromFile(testFilePath));
        assertTrue(message, lines.size() == expectedRows);
    }

    public byte[] bytesFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        ByteArrayOutputStream ous = null;
        InputStream ios = null;
        try {
            byte[] buffer = new byte[4096];
            ous = new ByteArrayOutputStream();
            ios = new FileInputStream(file);
            int read = 0;
            while ((read = ios.read(buffer)) != -1) {
                ous.write(buffer, 0, read);
            }
        }finally {
            try {
                if (ous != null)
                    ous.close();
            } catch (IOException e) {
            }

            try {
                if (ios != null)
                    ios.close();
            } catch (IOException e) {
            }
        }
        return ous.toByteArray();
    }
}