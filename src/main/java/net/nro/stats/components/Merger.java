package net.nro.stats.components;

import net.nro.stats.components.parser.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class Merger {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    class PrioritizedRecord<I extends Record> {
        private int prio;
        private Record record;

        private PrioritizedRecord(int prio, Record record) {
            this.prio = prio;
            this.record = record;
        }

        public int getPrio() {
            return prio;
        }

        public Record getRecord() {
            return record;
        }
    }

    public List<Line> merge(List<List<Line>> inputLinesPerRIR) {



        List<PrioritizedRecord<IPv4Record>> mergedIPv4Lines = null;
        List<PrioritizedRecord<IPv6Record>> mergedIPv6Lines = null;
        List<PrioritizedRecord<ASNRecord>> mergedASNLines = null;

        for( int i = 0; i < inputLinesPerRIR.size(); i++) {
            final int prio = i;
            mergedIPv4Lines = inputLinesPerRIR.get(prio).stream()
                    .filter(record -> record instanceof IPv4Record)
                    .map(record -> new PrioritizedRecord<IPv4Record>(prio, (IPv4Record)record))
                    .sorted((o1, o2) -> ((IPv4Record)o1.getRecord()).getComparator().compare(o1.getRecord().getRange(), o2.getRecord().getRange()))
                    .collect(Collectors.toList());
            mergedIPv6Lines = inputLinesPerRIR.get(prio).stream()
                    .filter(record -> record instanceof IPv6Record)
                    .map(record -> new PrioritizedRecord<IPv6Record>(prio, (IPv6Record)record))
                    .sorted((o1, o2) -> ((IPv6Record)o1.getRecord()).getComparator().compare(o1.getRecord().getRange(), o2.getRecord().getRange()))
                    .collect(Collectors.toList());
            mergedASNLines = inputLinesPerRIR.get(prio).stream()
                    .filter(record -> record instanceof ASNRecord)
                    .map(record -> new PrioritizedRecord<ASNRecord>(prio, (ASNRecord)record))
                    .sorted((o1, o2) -> ((ASNRecord)o1.getRecord()).getComparator().compare(o1.getRecord().getRange(), o2.getRecord().getRange()))
                    .collect(Collectors.toList());


        }

        List<Line> result = handleConflicts(mergedIPv4Lines);
        result.addAll(handleConflicts(mergedIPv6Lines));
        result.addAll(handleConflicts(mergedASNLines));
        return result;
    }

    private <T extends Record> List<Line> handleConflicts(List<PrioritizedRecord<T>> lines) {
        List<Line> results = new ArrayList<>();

        for(int i = 0; i < lines.size(); i++) {

            if (i == 0 || !lines.get(i).getRecord().getRange().overlaps(lines.get(i-1).getRecord().getRange())) {

                results.add(lines.get(i).getRecord());
            }
            else {
                logger.info("Conflict: " + lines.get(i).getRecord().toString());
            }
        }
        return results;
    }
}
