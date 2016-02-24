package net.nro.stats.parser;

import org.apache.commons.csv.CSVRecord;

public class Record implements Line {
    private final String registry;
    private final String countryCode;
    private final String type;
    private final String start;
    private final String value;
    private final String date;
    private final String status;
    private final String regId;
    private final String[] extensions;

    public Record(String registry, String countryCode, String Type, String start, String value, String date, String status, String regId, String... extensions) {
        this.registry = registry;
        this.countryCode = countryCode;
        type = Type;
        this.start = start;
        this.value = value;
        this.date = date;
        this.status = status;
        this.regId = regId;
        this.extensions = extensions;
    }

    public Record(CSVRecord line) {
        if (!fits(line)) throw new RuntimeException("Given line was not a Record");

        this.registry = line.get(0);
        this.countryCode = line.get(1);
        type = line.get(2);
        this.start = line.get(3);
        this.value = line.get(4);
        this.date = line.get(5);
        this.status = line.get(6);
        this.regId = line.get(7);

        String[] exts = new String[line.size() - 8];
        for (int i = 8; i < line.size(); i++) {
            exts[i - 8] = line.get(i);
        }
        this.extensions = exts;
    }

    public String getRegistry() {
        return registry;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getType() {
        return type;
    }

    public String getStart() {
        return start;
    }

    public String getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getRegId() {
        return regId;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public static boolean fits(CSVRecord line) {
        String type = line.get(2);
        return line.size() > 7 && ("asn".equals(type) || "ipv4".equals(type) || "ipv6".equals(type)) ;
    }

    public  boolean hasExtensions() {
        return extensions.length > 0;
    }
}
