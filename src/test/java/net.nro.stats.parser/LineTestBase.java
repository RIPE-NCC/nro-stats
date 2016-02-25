package net.nro.stats.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;

public abstract class LineTestBase {
    protected Iterable<CSVRecord> lines;

    protected void createRawLines(String sourceFileName) throws IOException {
        URL testFile = this.getClass().getClassLoader().getResource(sourceFileName);

        assert testFile != null;
        Reader in = new FileReader(testFile.getPath());
        lines = CSVFormat
                .DEFAULT
                .withDelimiter('|')
                .withCommentMarker('#') // only recognized at start of line!
                .withRecordSeparator('\n')
                .withIgnoreEmptyLines()
                .withIgnoreSurroundingSpaces()
                .parse(in);
    }
}
