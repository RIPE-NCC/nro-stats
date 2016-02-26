package net.nro.stats.components.parser;

import org.apache.commons.csv.CSVRecord;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SummaryTest extends LineTestBase {

    @Before
    public void setUp() throws Exception {
        createRawLines("parser/summary.txt");
    }

    @Test
    public void testFits() throws Exception {
        for(CSVRecord line : lines) {
            assertTrue(String.format("line %d should fit Summary", line.getRecordNumber()), Summary.fits(line));
        }
    }

    @Test
    public void testValuesCorrect() throws Exception {
        CSVRecord line1 = lines.iterator().next();
        Summary summary1 = new Summary(line1);
        assertTrue("Summary field not correct: registry", line1.get(0).equals(summary1.getRegistry()));
        assertTrue("Summary field not correct: type", line1.get(2).equals(summary1.getType()));
        assertTrue("Summary field not correct: count", line1.get(4).equals(summary1.getCount()));
    }
}