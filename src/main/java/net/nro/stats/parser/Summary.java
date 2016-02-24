package net.nro.stats.parser;

import org.apache.commons.csv.CSVRecord;

public class Summary implements Line {

    private final String registry;
    private final String type;
    private final String count;

    public Summary(String registry, String type, String count) {
        this.registry = registry;
        this.type = type;
        this.count = count;
    }

    public Summary(CSVRecord line) {
        if (!fits(line)) throw new RuntimeException("Given line was not a Summary");
        this.registry = line.get(0);
        this.type = line.get(2);
        this.count = line.get(4);
    }

    public String getRegistry() {
        return registry;
    }

    public String getType() {
        return type;
    }

    public String getCount() {
        return count;
    }

    public static boolean fits(CSVRecord line) {
        return line.size() == 6 && "summary".equals(line.get(5));
    }
}
