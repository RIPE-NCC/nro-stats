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
