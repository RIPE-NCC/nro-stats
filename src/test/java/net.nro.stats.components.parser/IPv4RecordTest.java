package net.nro.stats.components.parser;

import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IPv4RecordTest extends LineTestBase {

    @Before
    public void setUp() throws Exception {
        createRawLines("parser/ipv4.txt");
    }

    @Test
    public void testFits() throws Exception {
        for (CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit IPv4Record", line.getRecordNumber()), IPv4Record.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        for (CSVRecord line : lines) {
            IPv4Record record = new IPv4Record(line);
            assertEquals("IPv4Record field not correct: registry", line.get(0), record.getRegistry());
            assertEquals("IPv4Record field not correct: countryCode", line.get(1), record.getCountryCode());
            assertEquals("IPv4Record field not correct: type", line.get(2), record.getType());
            assertEquals("IPv4Record field not correct: start", line.get(3), record.getStart());
            assertEquals("IPv4Record field not correct: value", line.get(4), record.getValue());
            assertEquals("IPv4Record field not correct: date", line.get(5), record.getDate());
            assertEquals("IPv4Record field not correct: status", line.get(6), record.getStatus());
            assertEquals("IPv4Record field not correct: regId", line.get(7), record.getRegId());

            assertEquals("IPv4Record: number of extensions does not match line", line.size() - 8 , record.getExtensions().length);
            for (int i = 0; i < record.getExtensions().length; i++) {
                assertEquals(String.format("extension %d does not match on line %d", i, line.getRecordNumber()), record.getExtensions()[i], line.get(i + 8));
            }
        }
    }

    @Test
    public void testIpRangeConversion() throws Exception {
        IPv4Record record1 = new IPv4Record("", "", "0.0.0.0", "1", "", "", "");
        assertEquals("", record1.getRange(), Ipv4Range.from(Ipv4.of("0.0.0.0")).to(Ipv4.of("0.0.0.0")));

        IPv4Record record2 = new IPv4Record("", "", "100.0.0.0", "256", "", "", "");
        assertEquals("", record2.getRange(), Ipv4Range.from(Ipv4.of("100.0.0.0")).to(Ipv4.of("100.0.0.255")));

        IPv4Record record3 = new IPv4Record("", "", "192.168.0.0", "1024", "", "", "");
        assertEquals("", record3.getRange(), Ipv4Range.from(Ipv4.of("192.168.0.0")).to(Ipv4.of("192.168.3.255")));

    }
}