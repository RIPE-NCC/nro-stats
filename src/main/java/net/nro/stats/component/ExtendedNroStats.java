package net.nro.stats.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExtendedNroStats {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void generate() {
        logger.info("Generating Extended NRO Stats");

        logger.info("Finished Generating Extended NRO stats");
    }
}
