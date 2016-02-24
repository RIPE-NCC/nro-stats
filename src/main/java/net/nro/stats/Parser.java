package net.nro.stats;

import net.nro.stats.parser.*;
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

    private List<Line> parse(String fileName) {
        List<Line> fileContent = new ArrayList<>();

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
                } else if (IPv4Record.fits(line)) {
                    IPv4Record record = new IPv4Record(line);
                    fileContent.add(record);
                    System.out.println("range: " + record.getRange());
                } else if (IPv6Record.fits(line)) {
                    IPv6Record record = new IPv6Record(line);
                    fileContent.add(record);
                    System.out.println("range: " + record.getRange());
                } else if (ASNRecord.fits(line)) {
                    ASNRecord record = new ASNRecord(line);
                    fileContent.add(record);
                    System.out.println("range: " + record.getRange());
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
        return fileContent;
    }

}
