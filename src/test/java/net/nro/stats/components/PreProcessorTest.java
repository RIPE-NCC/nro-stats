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

import net.nro.stats.components.parser.Parser;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.StatsSource;
import net.nro.stats.resources.URIContent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PreProcessorTest {

    @Spy
    URIContentRetriever uriContentRetriever = new URIContentRetriever(new FileRetriever(), new HttpRetriever());
    @Spy
    Parser parser = new Parser(Charset.forName("US-ASCII"), new DummyDateTimeProvider());

    @InjectMocks
    PreProcessor preProcessor;

    @Test
    public void testProcessIanaStats() throws Exception {
        URIContent ianaContent = uriContentRetriever.fetch("iana", "src/test/resources/iana.test.delegated-extended.stats.txt");
        ParsedRIRStats ianaStats = parser.parseRirStats(StatsSource.IANA_REGISTRY, ianaContent);
        assertEquals(3, ianaStats.getAsnRecords().size());
        assertEquals(3, ianaStats.getIpv4Records().size());
        assertEquals(11, ianaStats.getIpv6Records().size());
        preProcessor.processIanaStats(ianaStats);
        assertEquals(1, ianaStats.getAsnRecords().size());
        assertEquals(1, ianaStats.getIpv4Records().size());
        assertEquals(8, ianaStats.getIpv6Records().size());
    }
}