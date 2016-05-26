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
package net.nro.stats.services;

import net.nro.stats.components.*;
import net.nro.stats.components.merger.ASNIntervalTree;
import net.nro.stats.components.merger.Delta;
import net.nro.stats.components.merger.IPNode;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.config.ExtendedInputConfig;
import net.nro.stats.config.ExtendedOutputConfig;
import net.nro.stats.resources.MergedStats;
import net.nro.stats.resources.ParsedRIRStats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NroStatsServiceTest {
    @Spy
    ExtendedInputConfig extendedInputConfig = new ExtendedInputConfig();

    @Spy
    ExtendedOutputConfig extendedOutputConfig = new ExtendedOutputConfig();

    @Spy
    DateTimeProvider dateTimeProvider = new DummyDateTimeProvider();

    @Spy
    Parser parser = new Parser(Charset.forName("US-ASCII"), new DummyDateTimeProvider());

    @Mock
    RecordsMerger recordsMerger;

    @Mock
    PreProcessor preProcessor;

    @Mock
    StatsWriter writer;

    @Spy
    URIContentRetriever uriContentRetriever = new URIContentRetriever(new FileRetriever(), new HttpRetriever());

    @InjectMocks
    NroStatsService nroStatsService;

    @Before
    public void before() {
        Map<String, String> urls = new HashMap<>();
        urls.put("ripencc", "src/test/resources/ripencc.test.delegated.stats.txt");
        urls.put("apnic", "src/test/resources/apnic.test.delegated.stats.txt");
        extendedInputConfig.setRir(urls);
        extendedInputConfig.setIana("src/test/resources/iana.test.delegated-extended.stats.txt");
        extendedInputConfig.setSwaps("src/test/resources/rirswap.txt");
        MergedStats nroStats = new MergedStats();
        nroStats.setAsns(new ASNIntervalTree());
        nroStats.setIpv4s(new IPNode<>(null));
        nroStats.setIpv6s(new IPNode<>(null));
        when(recordsMerger.merge(anyListOf(ParsedRIRStats.class))).thenReturn(nroStats);
        extendedOutputConfig.setFolder("src/test/resources");
        extendedOutputConfig.setFile("it-delegated-extended");
        extendedOutputConfig.setPrevious("it-delegated-extended");
    }

    @Test
    public void testGenerate() throws Exception {
        nroStatsService.generate();

        verify(recordsMerger).merge(anyListOf(ParsedRIRStats.class));
        verify(writer).write(any());
    }

    @Test
    public void testDifferences() throws Exception {
        when(recordsMerger.merge(anyListOf(ParsedRIRStats.class))).thenReturn(mock(MergedStats.class));
        List<Delta<?>> differences = nroStatsService.getDifferences();
        verify(recordsMerger, times(2)).merge(anyListOf(ParsedRIRStats.class));
        assertTrue("differences should not come when it is same", differences.size() == 0);
    }

    @After
    public void cleanup() {
        reset(recordsMerger);
    }
}