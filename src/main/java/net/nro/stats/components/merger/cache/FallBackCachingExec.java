package net.nro.stats.components.merger.cache;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpExecutionAware;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.HttpHostConnectException;
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

    public FallBackCachingExec(final ClientExecChain mainExec, File rootDir, Map<String, CachedHttpResponse> cache) {
        this.backend = mainExec;
        this.rootDir = rootDir;
        this.cache = cache;
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
            cachedHttpResponse = createCachedResponse(request, response);
            return cachedHttpResponse;
        }
        catch (HttpHostConnectException hhce) {
            logger.trace("Using cached data");
            cachedHttpResponse = getCachedResponse(request);
            if (cachedHttpResponse == null)
                throw hhce;
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
}
