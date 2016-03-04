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

import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.StartAndSizeComparator;
import org.apache.commons.csv.CSVRecord;

import java.math.BigInteger;
import java.util.Comparator;

public class IPv4Record extends Record<Ipv4Range> {

    public IPv4Record(String registry, String countryCode, String start, String value, String date, String status, String regId, String... extensions) {
        super(registry, countryCode, "ipv4", start, value, date, status, regId, extensions);
    }

    public IPv4Record(CSVRecord line, String defaultDate) {
        super(line, defaultDate);
    }

    public static boolean fits(CSVRecord line) {
        return line.size() > 6 && "ipv4".equals(line.get(2));
    }

    @Override
    public Ipv4Range getRange() {
        Ipv4 start = Ipv4.of(getStart());
        BigInteger endInt = start.asBigInteger().add(BigInteger.valueOf(Long.parseLong(getValue()) - 1));
        Ipv4 end = Ipv4.of(endInt);
        return Ipv4Range.from(start).to(end);
    }

    @Override
    public Comparator getComparator() {
        return StartAndSizeComparator.<Ipv4, Ipv4Range>get();
    }

    @Override
    public IPv4Record clone(Ipv4Range range) {
        return new IPv4Record(getRegistry(), getCountryCode(), range.start().toString(), range.size().toString(), getDate(), getStatus(),getRegId(), getExtensions());
    }

}
