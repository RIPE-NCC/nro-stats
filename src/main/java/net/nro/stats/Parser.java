package net.nro.stats;

import net.nro.stats.parser.Header;
import net.nro.stats.parser.Line;
import net.nro.stats.parser.Record;
import net.nro.stats.parser.Summary;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd");

    public Parser() {
    }

    public static void main(String[] args) {
        Parser parser = new Parser();
        parser.run();
    }

    private void run() {
        parse("stats/delegated-apnic-extended-latest.txt");
    }

    private void parse(String fileName) {
        List<Line> fileContent = new ArrayList<Line>();

        try {
            Reader in = new FileReader(fileName);
            Iterable<CSVRecord> lines = CSVFormat
                    .DEFAULT
                    .withDelimiter('|')
                    .withCommentMarker('#')
                    .withRecordSeparator('\n')
                    .withIgnoreEmptyLines()
                    .parse(in);
            for (CSVRecord line : lines) {
                if (Header.fits(line)) {
                    fileContent.add(new Header(line));
                } else if (Summary.fits(line)) {
                    fileContent.add(new Summary(line));
                } else if (Record.fits(line)) {
                    fileContent.add(new Record(line));
                } else {
                    throw new RuntimeException("Malformed line number " + line.getRecordNumber());
                }
             }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Found records: " + fileContent.size());
    }

}
