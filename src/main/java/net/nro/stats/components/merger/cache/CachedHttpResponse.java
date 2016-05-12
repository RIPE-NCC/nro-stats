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

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.util.Locale;

public class CachedHttpResponse implements CloseableHttpResponse {

    private CachedHeaderIterator headerIterator;
    private StatusLine statusLine;
    private int statusCode;
    private HttpEntity entity;
    private Locale locale;

    public CachedHttpResponse() {
        this.headerIterator = new CachedHeaderIterator();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public StatusLine getStatusLine() {
        return statusLine;
    }

    @Override
    public void setStatusLine(StatusLine statusLine) {
        this.statusLine = statusLine;
    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i) {

    }

    @Override
    public void setStatusLine(ProtocolVersion protocolVersion, int i, String s) {

    }

    @Override
    public void setStatusCode(int i) throws IllegalStateException {
        this.statusCode = i;
    }

    @Override
    public void setReasonPhrase(String s) throws IllegalStateException {

    }

    @Override
    public HttpEntity getEntity() {
        return entity;
    }

    @Override
    public void setEntity(HttpEntity httpEntity) {
        this.entity = httpEntity;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        return null;
    }

    @Override
    public boolean containsHeader(String s) {
        return false;
    }

    @Override
    public Header[] getHeaders(String s) {
        return new Header[0];
    }

    @Override
    public Header getFirstHeader(String s) {
        return null;
    }

    @Override
    public Header getLastHeader(String s) {
        return null;
    }

    @Override
    public Header[] getAllHeaders() {
        return new Header[0];
    }

    @Override
    public void addHeader(Header header) {

    }

    @Override
    public void addHeader(String s, String s1) {

    }

    @Override
    public void setHeader(Header header) {

    }

    @Override
    public void setHeader(String s, String s1) {

    }

    @Override
    public void setHeaders(Header[] headers) {

    }

    @Override
    public void removeHeader(Header header) {

    }

    @Override
    public void removeHeaders(String s) {

    }

    @Override
    public HeaderIterator headerIterator() {
        return headerIterator;
    }

    @Override
    public HeaderIterator headerIterator(String s) {
        return headerIterator;
    }

    @Override
    public HttpParams getParams() {
        return null;
    }

    @Override
    public void setParams(HttpParams httpParams) {

    }

    class CachedHeaderIterator implements HeaderIterator {

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            return null;
        }

        @Override
        public Header nextHeader() {
            return null;
        }
    }
}
