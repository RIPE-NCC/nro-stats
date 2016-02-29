package net.nro.stats.components;

import java.net.URI;
import java.net.URL;

/**
 * Created by rudi on 29-02-16.
 */
public interface URIBytesRetriever {
    public byte[] retrieveBytes(String uri);
}
