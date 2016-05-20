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
package net.nro.stats.components.merger;

import net.nro.stats.components.parser.IPv4Record;
import net.nro.stats.components.resolver.Resolver;
import net.ripe.commons.ip.Ipv4;
import net.ripe.commons.ip.Ipv4Range;
import net.ripe.commons.ip.PrefixUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.padStart;

@Component
public class IPv4Merger extends IPMerger<IPv4Record, Ipv4Range> {

    @Autowired
    public IPv4Merger(Resolver resolver) {
        super(resolver, Ipv4.NUMBER_OF_BITS);
    }

    @Override
    public String getSignificantBinaryValues(Ipv4Range range) {
        return padStart(range.start().asBigInteger().toString(2), maxSize, '0').substring(0, PrefixUtils.getPrefixLength(range));
    }

    @Override
    public List<Ipv4Range> prefixRanges(Ipv4Range range) {
        return range.splitToPrefixes();
    }

    @Override
    public List<Ipv4Range> splitRanges(Ipv4Range range) {
        List<Ipv4Range> ranges = new ArrayList<>();
        int length = range.start().getCommonPrefixLength(range.end());
        if (length < maxSize) {
            ranges.add(Ipv4Range.from(range.start()).to(range.start().upperBoundForPrefix(length + 1)));
            ranges.add(Ipv4Range.from(range.start().upperBoundForPrefix(length + 1).next()).to(range.end()));
        }
        return ranges;
    }
}
