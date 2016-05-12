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
package net.nro.stats.components.merger.cache;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.execchain.ClientExecChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

class FallBackCachingExec implements ClientExecChain {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ClientExecChain backend;
    private final File rootDir;
    private final Map<String, CachedHttpResponse> cache;
    private final boolean rejectEmptyResponse;

    public FallBackCachingExec(final ClientExecChain mainExec, File rootDir, Map<String, CachedHttpResponse> cache, boolean rejectEmptyResponse) {
        this.backend = mainExec;
        this.rootDir = rootDir;
        this.cache = cache;
        this.rejectEmptyResponse = rejectEmptyResponse;
    }

    @Override
    public CloseableHttpResponse execute(
            final HttpRoute route,
            final HttpRequestWrapper request,
            final HttpClientContext clientContext,
            final HttpExecutionAware execAware) throws IOException, HttpException {

        CachedHttpResponse cachedHttpResponse = null;
        try {
            logger.trace("Making real request");
            CloseableHttpResponse response = backend.execute(route, request, clientContext, execAware);
            if (rejectEmptyResponse && response.getEntity().getContentLength() == 0)
                throw new EmptyContentException();
            cachedHttpResponse = createCachedResponse(request, response);
            return cachedHttpResponse;
        }
        catch (HttpException exp) {
            logger.trace("Using cached data");
            cachedHttpResponse = getCachedResponse(request);
            if (cachedHttpResponse == null)
                throw exp;
        }
        return cachedHttpResponse;
    }

    private CachedHttpResponse createCachedResponse(final HttpRequestWrapper request, final CloseableHttpResponse response) throws IOException {
        CachedHttpResponse cachedHttpResponse = new CachedHttpResponse();
        String uri = request.getOriginal().getRequestLine().getUri();
        String fileName = MD5(uri);
        File file = new File(rootDir, fileName);
        logger.trace("caching url: " + uri + " to file: " + file);
        HttpEntity httpEntity = createCachedFileEntity(response.getEntity(), file);
        cachedHttpResponse.setEntity(httpEntity);
        cachedHttpResponse.setStatusLine(response.getStatusLine());
        cachedHttpResponse.setLocale(response.getLocale());
        cachedHttpResponse.setHeaders(response.getAllHeaders());
        cache.put(fileName, cachedHttpResponse);
        return cachedHttpResponse;
    }

    private HttpEntity createCachedFileEntity(HttpEntity originalEntity, File file) throws IOException {
        ContentType contentType = ContentType.get(originalEntity);
        File cachedFile = createCachedFile(originalEntity, file);
        return new FileEntity(cachedFile, contentType);
    }

    private File createCachedFile(HttpEntity originalEntity, File file) throws IOException {
        if (file.exists())
            file.delete();
        rootDir.mkdirs();
        OutputStream outputStream = new FileOutputStream(file);
        InputStream inputStream = originalEntity.getContent();
        IOUtils.copy(inputStream, outputStream);
        inputStream.close();
        outputStream.close();
        return file;
    }

    private CachedHttpResponse getCachedResponse(HttpRequestWrapper request) {
        String uri = request.getOriginal().getRequestLine().getUri();
        String key = MD5(uri);
        return cache.get(key);
    }

    public String MD5(String source) {
        try {
            java.security.MessageDigest messageDigest = java.security.MessageDigest.getInstance("MD5");
            byte[] array = messageDigest.digest(source.getBytes());
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < array.length; ++i) {
                buffer.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
            }
            return buffer.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Missing support for MD5 encoding.");
        }
    }

    // simple marker class
    class EmptyContentException extends HttpException {}
}
