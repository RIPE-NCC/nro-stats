package net.nro.stats.parser;

import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import org.apache.commons.csv.CSVRecord;

public class IPv4Record extends Record {

    public IPv4Record(String registry, String countryCode, String start, String value, String date, String status, String regId, String... extensions) {
        super(registry, countryCode, "ipv4", start, value, date, status, regId, extensions);
    }

    public IPv4Record(CSVRecord line) {
        super(line);
    }

    public static boolean fits(CSVRecord line) {
        String type = line.get(2);
        return line.size() > 7 && "ipv4".equals(type);
    }

    @Override
    public Ipv4Range getRange() {
        Ipv4 start = Ipv4.of(getStart());
        int prefix = 33 - Long.toBinaryString(Long.parseLong(getValue())).length();
        return Ipv4Range.from(start).andPrefixLength(prefix);
    }


}
