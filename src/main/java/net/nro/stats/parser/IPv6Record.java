package net.nro.stats.parser;

import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import org.apache.commons.csv.CSVRecord;

public class IPv6Record extends Record {

    public IPv6Record(String registry, String countryCode, String start, String value, String date, String status, String regId, String... extensions) {
        super(registry, countryCode, "ipv6", start, value, date, status, regId, extensions);
    }

    public IPv6Record(CSVRecord line) {
        super(line);
    }

    public static boolean fits(CSVRecord line) {
        String type = line.get(2);
        return line.size() > 7 && "ipv6".equals(type);
    }

    @Override
    public Ipv6Range getRange() {
        Ipv6 start = Ipv6.of(getStart());
        int prefix = Integer.parseInt(getValue());
        return Ipv6Range.from(start).andPrefixLength(prefix);
    }
}
