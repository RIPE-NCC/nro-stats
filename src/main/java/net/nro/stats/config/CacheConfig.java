package net.nro.stats.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "nro.stats.extended.cache")
public class CacheConfig {
    private String root;
    private Boolean rejectEmpty = false;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Boolean getRejectEmpty() {
        return rejectEmpty;
    }

    public void setRejectEmpty(Boolean rejectEmpty) {
        this.rejectEmpty = rejectEmpty;
    }
}
