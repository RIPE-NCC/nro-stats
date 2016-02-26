package net.nro.stats.components.parser;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Component
public class Parser {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final String FILE_ENCODING = "US-ASCII";
    
    public List<Line> parse(byte[] content) {
        List<Line> fileContent = new ArrayList<>();

        try {
            Reader in = new InputStreamReader(new ByteArrayInputStream(content), Charset.forName(FILE_ENCODING));
            Iterable<CSVRecord> lines = CSVFormat
                    .DEFAULT
                    .withDelimiter('|')
                    .withCommentMarker('#') // only recognized at start of line!
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .withIgnoreSurroundingSpaces()
                    .parse(in);
            for (CSVRecord line : lines) {
                if (Header.fits(line)) {
                    fileContent.add(new Header(line));
                } else if (Summary.fits(line)) {
                    fileContent.add(new Summary(line));
                } else if (IPv4Record.fits(line)) {
                    fileContent.add(new IPv4Record(line));
                } else if (IPv6Record.fits(line)) {
                    fileContent.add(new IPv6Record(line));
                } else if (ASNRecord.fits(line)) {
                    fileContent.add(new ASNRecord(line));
                } else {
                    throw new RuntimeException("Malformed line number " + line.getRecordNumber() + "\n" + line.toString());
                }
             }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.debug("Found records: " + fileContent.size());
        return fileContent;
    }

}
