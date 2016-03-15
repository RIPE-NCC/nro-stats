/**
 * The BSD License
 *
 * Copyright (c) 2010-2016 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.nro.stats.components;

import com.google.common.base.Strings;
import net.nro.stats.components.parser.ASNRecord;
import net.nro.stats.components.parser.Parser;
import net.nro.stats.config.AsnTranslate;
import net.nro.stats.resources.ASNTransfer;
import net.nro.stats.resources.ParsedRIRStats;
import net.nro.stats.resources.URIContent;
import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class Validator {

    private URIContentRetriever contentRetriever;
    private Parser parser;
    private AsnTranslate asnTranslate;

    @Autowired
    public Validator(AsnTranslate asnTranslate, URIContentRetriever contentRetriever, Parser parser) {
        this.contentRetriever = contentRetriever;
        this.parser = parser;
        this.asnTranslate = asnTranslate;
    }

    public void validate(List<ParsedRIRStats> sourceLinesPerRIR) {
        sourceLinesPerRIR
                .stream()
                .filter(p -> asnTranslate.getRir().keySet().contains(p.getRir()))
                .forEach(this::translateAsn);
        return;
    }

    private void translateAsn(ParsedRIRStats rirStats) {
        URIContent asnEuTranslate = contentRetriever.fetch(rirStats.getRir(), asnTranslate.getRir().get(rirStats.getRir()));
        ASNTransfer asnTransfer = parser.parseAsnTransfers(asnEuTranslate);
        asnTransfer.getRecords().stream()
                .forEach(r -> {
                    Optional<ASNRecord> optionalAsnRecord = rirStats.getAsnRecords().stream().filter(a -> a.getRange().contains(r.getAsn())).findFirst();
                    if (optionalAsnRecord.isPresent()) {
                        ASNRecord asnRecord = optionalAsnRecord.get();
                        rirStats.getAsnRecords().remove(asnRecord);
                        rirStats.addAsnRecord(asnRecord.clone(r.getAsnRange(), r.getCountryCode()));
                        List<AsnRange> asnRanges = asnRecord.getRange().exclude(r.getAsnRange());
                        for (AsnRange range: asnRanges) {
                            rirStats.addAsnRecord(asnRecord.clone(range));
                        }
                    }
                });
    }
}
