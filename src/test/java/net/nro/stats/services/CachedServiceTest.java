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

import com.google.common.collect.Maps;
import net.nro.stats.components.RecordsMerger;
import net.nro.stats.components.URIContentRetriever;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.config.CacheConfig;
import net.nro.stats.resources.MergedStats;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.URIContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CachedServiceTest {

    @Mock
    URIContentRetriever uriContentRetriever;

    @Mock
    Parser parser;

    @Spy
    CacheConfig cacheConfig = new CacheConfig();

    @Mock
    RecordsMerger recordsMerger;

    @InjectMocks
    CachedService cachedService;

    @Before
    public void before() {
        Map<String, String> maps = Maps.newHashMap();
        maps.put("current", "somefile");
        cacheConfig.setFile(maps);
    }

    @Test
    public void testFetch() throws Exception {
        ParsedRIRStats parsedRIRStats = mock(ParsedRIRStats.class);
        when(parser.parseNroStats(any(URIContent.class))).thenReturn(parsedRIRStats);
        when(recordsMerger.merge(anyListOf(ParsedRIRStats.class))).thenReturn(mock(MergedStats.class));
        cachedService.fetch("current");
        verify(uriContentRetriever).fetch(anyString(), anyString());
        reset(uriContentRetriever);
        cachedService.fetch("current");
        verify(uriContentRetriever, never()).fetch(anyString(), anyString());
    }

    @Test
    public void testFailureShouldReturnNull() {
        when(parser.parseNroStats(any(URIContent.class))).thenThrow(IOException.class);
        assertNull(cachedService.fetch("current"));
    }

}