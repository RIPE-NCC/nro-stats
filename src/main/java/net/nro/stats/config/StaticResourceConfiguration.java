package net.nro.stats.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@AutoConfigureAfter(ExtendedOutputConfig.class)
public class StaticResourceConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    ExtendedOutputConfig outputConfig;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/nro/**").addResourceLocations("file:" + outputConfig.getFolder())
                .setCacheControl(CacheControl.noCache().mustRevalidate());
    }
}