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
package net.nro.stats.components.parser;

import net.ripe.commons.ip.Asn;
import net.ripe.commons.ip.AsnRange;
import net.ripe.commons.ip.StartAndSizeComparator;
import org.apache.commons.csv.CSVRecord;

import java.util.Comparator;

public class ASNRecord extends Record<AsnRange> {

    public ASNRecord(String registry, String countryCode, String start, String value, String date, String status, String regId, String... extensions) {
        super(registry, countryCode, "asn", start, value, date, status, regId, extensions);
    }

    public ASNRecord(CSVRecord line, String defaultDate) {
        super(line, defaultDate);
    }

    public static boolean fits(CSVRecord line) {
        return line.size() > 6 && "asn".equals(line.get(2));
    }

    @Override
    public AsnRange getRange() {
        Asn start = Asn.of(getStart());
        Asn end = Asn.of(Long.parseLong(getStart()) + Long.parseLong(getValue()) - 1);
        return AsnRange.from(start).to(end);
    }

    @Override
    public Comparator getComparator() {
        return StartAndSizeComparator.<Asn, AsnRange>get();
    }

    @Override
    public ASNRecord clone(AsnRange range) {
        return new ASNRecord(getRegistry(), getCountryCode(), range.start().asBigInteger().toString(), range.size().toString(), getDate(), getStatus(), getRegId(), getExtensions());
    }
}
