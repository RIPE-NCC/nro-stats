/**
 * The BSD License
 *
 * Copyright (c) 2010-2016 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.nro.stats.components.parser;

import net.nro.stats.components.DateTimeProvider;
import net.nro.stats.components.FileURIBytesRetriever;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ParserTest {

    private Parser sut;
    private FileURIBytesRetriever bytesRetriever = new FileURIBytesRetriever();

    @Before
    public void beforeEach() {
        sut = new Parser(Charset.forName("US-ASCII"), new DateTimeProvider());
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
        byte[] bytes = bytesRetriever.retrieveBytes(testFile.getPath());
        List<Line> lines = sut.parse(bytes);
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