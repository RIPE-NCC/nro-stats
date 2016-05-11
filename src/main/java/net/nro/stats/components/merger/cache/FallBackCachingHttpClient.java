package net.nro.stats.components.merger.cache;

import net.nro.stats.config.CacheConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.execchain.ClientExecChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Component
public class FallBackCachingHttpClient extends HttpClientBuilder {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, CachedHttpResponse> cache;
    private File rootDir;
    private Boolean rejectEmptyResponse;

    private CacheConfig config;

    @Autowired
    public FallBackCachingHttpClient(CacheConfig config) {
        cache = new HashMap<>();
        this.config = config;
        this.rootDir = new File(config.getRoot());
        this.rejectEmptyResponse = config.getRejectEmpty();
    }

    @Override
    protected ClientExecChain decorateMainExec(final ClientExecChain mainExec) {
        return new FallBackCachingExec(mainExec, rootDir, cache, rejectEmptyResponse);
    }


}
