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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import net.nro.stats.components.RecordsMerger;
import net.nro.stats.components.URIContentRetriever;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.config.CacheConfig;
import net.nro.stats.resources.MergedStats;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.URIContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

@Service
public class CachedService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final LoadingCache<String, MergedStats> cacheResources;

    @Autowired
    public CachedService(URIContentRetriever contentRetriever, Parser parser, RecordsMerger merger, CacheConfig cacheConfig) {
        cacheResources = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, MergedStats>() {
            @Override
            public MergedStats load(String key) throws Exception {
                logger.info(format("Fetching %s from %s", key, cacheConfig.getFile().get(key)));
                URIContent content = contentRetriever.fetch(key, cacheConfig.getFile().get(key));
                ParsedRIRStats stats = parser.parseNroStats(content);
                MergedStats searchableStats =  merger.merge(Lists.newArrayList(stats));
                return searchableStats;
            }
        });
    }

    public MergedStats fetch(String key) {
        MergedStats retVal = null;
        try {
            retVal = cacheResources.get(key);
        } catch (Exception e){
            logger.warn("Error while fetching content from cacheResources", e);
        }
        return retVal;
    }
}
