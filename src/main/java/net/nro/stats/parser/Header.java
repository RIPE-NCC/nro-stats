package net.nro.stats.parser;

import org.apache.commons.csv.CSVRecord;

import java.util.TimeZone;

public class Header implements Line {
    private final String version;
    private final String registry;
    private final String serial;
    private final Long records;
    private final String startDate;
    private final String endDate;
    private final TimeZone utcOffset;

    public Header(String version, String registry, String serial, String records, String startDate, String endDate, String utcOffset) {
        this.version = version;
        this.registry = registry;
        this.serial = serial;
        this.records = Long.parseLong(records);
        this.startDate = startDate;
        this.endDate = endDate;
        this.utcOffset = TimeZone.getTimeZone("GMT" + utcOffset);
    }

    public Header(CSVRecord line) {
        if (!fits(line)) throw new RuntimeException("Given line was not a Header");

        this.version = line.get(0);
        this.registry = line.get(1);
        this.serial = line.get(2);
        this.records = Long.parseLong(line.get(3));
        this.startDate = line.get(4);
        this.endDate = line.get(5);
        this.utcOffset = TimeZone.getTimeZone("GMT" + line.get(6));
    }

    public String getVersion() {
        return version;
    }

    public String getRegistry() {
        return registry;
    }

    public String getSerial() {
        return serial;
    }

    public static boolean fits(CSVRecord line) {
        return line.size() == 7 && "2.3".equals(line.get(0));
    }

    public Long getRecords() {
        return records;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public TimeZone getUtcOffset() {
        return utcOffset;
    }
}
