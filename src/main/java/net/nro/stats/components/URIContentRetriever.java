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

import net.nro.stats.resources.URIContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class URIContentRetriever {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private FileRetriever fileRetriever;
    private HttpRetriever httpRetriever;

    @Autowired
    public URIContentRetriever(FileRetriever fileRetriever, HttpRetriever httpRetriever) {
        this.fileRetriever = fileRetriever;
        this.httpRetriever = httpRetriever;
    }

    public List<URIContent> fetchAll(Map<String, String> urls) {
        logger.debug("fetchAll called");
        return urls.keySet()
                .parallelStream()
                .map(rir -> fetch(rir, urls.get(rir)))
                .collect(Collectors.toList());
    }

    public URIContent fetch(String rir, String url) {
        logger.debug("fetching {} for {}", url, rir);

        URIBytesRetriever retriever = fileRetriever;
        if (isExternal(url)) {
            retriever = httpRetriever;
        }
        return new URIContent(rir, retriever.retrieveBytes(url));
    }

    public boolean isExternal(String url) {
        return url.startsWith("http");
    }
}
