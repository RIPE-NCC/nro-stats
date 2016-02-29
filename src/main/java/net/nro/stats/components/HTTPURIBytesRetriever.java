package net.nro.stats.components;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Created by rudi on 29-02-16.
 */

@Component
@Scope("prototype")
@Profile("production")
public class HTTPURIBytesRetriever implements URIBytesRetriever {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public byte[] retrieveBytes(String uri) {
        logger.debug("retrieveBytes " + uri);
        try (CloseableHttpClient httpClient = HttpClientBuilder.create().build();
             CloseableHttpResponse response = httpClient.execute(new HttpGet(uri))) {

            if (response.getStatusLine().getStatusCode() == 200) {
                try (InputStream inputStream = response.getEntity().getContent()) {
                    return IOUtils.toByteArray(inputStream);
                } catch (Exception e) {
                    logger.error("Failed to get the content of the file. ", e);
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
