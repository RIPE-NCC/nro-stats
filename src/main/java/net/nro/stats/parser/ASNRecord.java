package net.nro.stats.parser;

import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import org.apache.commons.csv.CSVRecord;

public class ASNRecord extends Record {

    public ASNRecord(String registry, String countryCode, String start, String value, String date, String status, String regId, String... extensions) {
        super(registry, countryCode, "asn", start, value, date, status, regId, extensions);
    }

    public ASNRecord(CSVRecord line) {
        super(line);
    }

    public static boolean fits(CSVRecord line) {
        String type = line.get(2);
        return line.size() > 7 && "asn".equals(type);
    }

    @Override
    public AsnRange getRange() {
        Asn start = Asn.of(getStart());
        Asn end = Asn.of(Long.parseLong(getStart()) + Long.parseLong(getValue()));
        return AsnRange.from(start).to(end);
    }
}
