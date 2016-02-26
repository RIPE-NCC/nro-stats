package net.nro.stats.components;

import net.nro.stats.components.parser.Line;
import org.springframework.stereotype.Component;

import java.util.List;


// TODO: build me
@Component
public class StatsWriter {
    public void write(List<Line> targetLines) {
        // convert lines back to a file and write it
        // file should be put in 'nro.stats.extended.output'
    }
}
