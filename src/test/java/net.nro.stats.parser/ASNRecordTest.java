package net.nro.stats.parser;

import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ASNRecordTest extends LineTestBase {

    @Before
    public void setUp() throws Exception {
        createRawLines("parser/asn.txt");
    }

    @Test
    public void testFits() throws Exception {
        for (CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit ASNRecord", line.getRecordNumber()), ASNRecord.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        for (CSVRecord line : lines) {
            ASNRecord record = new ASNRecord(line);
            assertEquals("ASNRecord field not correct: registry", line.get(0), record.getRegistry());
            assertEquals("ASNRecord field not correct: countryCode", line.get(1), record.getCountryCode());
            assertEquals("ASNRecord field not correct: type", line.get(2), record.getType());
            assertEquals("ASNRecord field not correct: start", line.get(3), record.getStart());
            assertEquals("ASNRecord field not correct: value", line.get(4), record.getValue());
            assertEquals("ASNRecord field not correct: date", line.get(5), record.getDate());
            assertEquals("ASNRecord field not correct: status", line.get(6), record.getStatus());
            assertEquals("ASNRecord field not correct: regId", line.get(7), record.getRegId());

            assertEquals("ASNRecord: number of extensions does not match line", line.size() - 8 , record.getExtensions().length);
            for (int i = 0; i < record.getExtensions().length; i++) {
                assertEquals(String.format("extension %d does not match on line %d", i, line.getRecordNumber()), record.getExtensions()[i], line.get(i + 8));
            }
        }
    }

    @Test
    public void testASNRangeConversion() throws Exception {
        ASNRecord record1 = new ASNRecord("", "", "1", "10", "", "", "");
        assertEquals("", record1.getRange(), AsnRange.from(Asn.of(1L)).to(Asn.of(11L)));

    }
}