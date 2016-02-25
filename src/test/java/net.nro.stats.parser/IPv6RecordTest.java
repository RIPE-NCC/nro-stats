package net.nro.stats.parser;

import net.ripe.commons.ip.Ipv6;
import net.ripe.commons.ip.Ipv6Range;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IPv6RecordTest extends LineTestBase {

    @Before
    public void setUp() throws Exception {
        createRawLines("ipv6.txt");
    }

    @Test
    public void testFits() throws Exception {
        for (CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit IPv6Record", line.getRecordNumber()), IPv6Record.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        for (CSVRecord line : lines) {
            IPv6Record record = new IPv6Record(line);
            assertEquals("IPv6Record field not correct: registry", line.get(0), record.getRegistry());
            assertEquals("IPv6Record field not correct: countryCode", line.get(1), record.getCountryCode());
            assertEquals("IPv6Record field not correct: type", line.get(2), record.getType());
            assertEquals("IPv6Record field not correct: start", line.get(3), record.getStart());
            assertEquals("IPv6Record field not correct: value", line.get(4), record.getValue());
            assertEquals("IPv6Record field not correct: date", line.get(5), record.getDate());
            assertEquals("IPv6Record field not correct: status", line.get(6), record.getStatus());
            assertEquals("IPv6Record field not correct: regId", line.get(7), record.getRegId());

            assertEquals("IPv6Record: number of extensions does not match line", line.size() - 8 , record.getExtensions().length);
            for (int i = 0; i < record.getExtensions().length; i++) {
                assertEquals(String.format("extension %d does not match on line %d", i, line.getRecordNumber()), record.getExtensions()[i], line.get(i + 8));
            }
        }
    }

    @Test
    public void testIpRangeConversion() throws Exception {
        IPv6Record record1 = new IPv6Record("", "", "1::", "128", "", "", "");
        assertEquals("", record1.getRange(), Ipv6Range.from(Ipv6.of("1::")).to(Ipv6.of("1::")));

        IPv6Record record2 = new IPv6Record("", "", "1::", "127", "", "", "");
        assertEquals("", record2.getRange(), Ipv6Range.from(Ipv6.of("1::")).to(Ipv6.of("1::1")));

    }
}