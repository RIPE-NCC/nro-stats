package net.nro.stats.parser;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HeaderTest extends LineTestBase {

    @Before
    public void setUp() throws Exception {
        createRawLines("header.txt");
    }

    @Test
    public void testFits() throws Exception {
        for(CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit Header", line.getRecordNumber()), Header.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        CSVRecord line1 = lines.iterator().next();
        Header header1 = new Header(line1);
        assertTrue("Header field not correct: version", line1.get(0).equals(header1.getVersion()));
        assertTrue("Header field not correct: registry", line1.get(1).equals(header1.getRegistry()));
        assertTrue("Header field not correct: serial", line1.get(2).equals(header1.getSerial()));
        assertTrue("Header field not correct: records", line1.get(3).equals(header1.getRecords()));
        assertTrue("Header field not correct: startDate", line1.get(4).equals(header1.getStartDate()));
        assertTrue("Header field not correct: endDate", line1.get(5).equals(header1.getEndDate()));
        assertTrue("Header field not correct: utcOffset", line1.get(6).equals(header1.getUtcOffset()));
    }
}