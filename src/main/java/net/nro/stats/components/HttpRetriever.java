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

import net.nro.stats.components.merger.cache.FallBackCachingHttpClient;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class HttpRetriever implements URIBytesRetriever {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private FallBackCachingHttpClient httpClientBuilder;

    @Override
    public byte[] retrieveBytes(String uri) {
        logger.debug("retrieveBytes " + uri);
        try (CloseableHttpClient httpClient = httpClientBuilder.build();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(uri))) {

            if (response.getStatusLine().getStatusCode() == 200) {
                try (InputStream inputStream = response.getEntity().getContent()) {
                    return IOUtils.toByteArray(inputStream);
                } catch (Exception e) {
                    logger.error("Failed to get the content of the file: " + e);
                    throw new RuntimeException("Failed to get the content of the file.");
                }
            } else {
                logger.error("Invalid response from RIR {}, {}", uri, response.getStatusLine().getStatusCode());
                throw new RuntimeException(String.format("Invalid response from RIR %s ", uri));
            }
        } catch (IOException io) {
            throw new RuntimeException("Unable to fetch rir resource", io);
        }
    }
}
