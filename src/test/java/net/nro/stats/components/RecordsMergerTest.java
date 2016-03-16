/**
 * The BSD License
 *
 * Copyright (c) 2010-2016 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.nro.stats.components;

import net.nro.stats.components.merger.ASNMerger;
import net.nro.stats.components.merger.IPv4Merger;
import net.nro.stats.components.merger.IPv6Merger;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.components.resolver.OrderedResolver;
import net.nro.stats.config.DelegatedExtended;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.StatsSource;
import net.nro.stats.resources.URIContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class RecordsMergerTest {

    private OrderedResolver resolver = new OrderedResolver("apnic,afrinic,arin,ripencc,lacnic".split(","));

    @Spy
    IPv4Merger iPv4Merger = new IPv4Merger(resolver);
    @Spy
    IPv6Merger iPv6Merger = new IPv6Merger(resolver);
    @Spy
    ASNMerger asnMerger = new ASNMerger(resolver);

    @Spy
    DateTimeProvider dateTimeProvider = new DummyDateTimeProvider();
    @Spy
    DelegatedExtended delegatedExtended = new DelegatedExtended();

    @InjectMocks
    RecordsMerger recordsMerger;

    @Before
    public void setUp() throws Exception {
        delegatedExtended.setIdentifier("nro");
        delegatedExtended.setVersion("2.3");
    }

    @Test
    public void testMerge() throws Exception {

        ParsedRIRStats nroStats = recordsMerger.merge(fetchTestRIRStats());

        assertEquals(1, nroStats.getHeaders().count());
        assertEquals(3, nroStats.getSummary().count());
        assertEquals(1, nroStats.getAsnRecords().size());
        assertEquals(3, nroStats.getIpv4Records().size());
        assertEquals(1, nroStats.getIpv6Records().size());
        assertEquals(9, nroStats.getLines().count());
    }

    private List<ParsedRIRStats> fetchTestRIRStats() {
        Map<String, String> urls = new HashMap<>();
        urls.put("ripencc", "src/test/resources/ripencc.test.delegated.stats.txt");
        urls.put("apnic", "src/test/resources/apnic.test.delegated.stats.txt");
        URIContentRetriever uriContentRetriever = new URIContentRetriever(new FileRetriever(), new HttpRetriever());
        List<URIContent> rirStatses = uriContentRetriever.fetchAll(urls);
        Parser parser = new Parser(Charset.forName("US-ASCII"), new DummyDateTimeProvider());
        return rirStatses.stream().map(p -> parser.parseRirStats(StatsSource.ESTATS,p)).collect(Collectors.toList());
    }
}