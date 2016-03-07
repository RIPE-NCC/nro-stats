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

import net.nro.stats.components.parser.IPv6Record;
import net.nro.stats.components.resolver.Resolver;
import net.ripe.commons.ip.Ipv6Range;
import net.ripe.commons.ip.PrefixUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.padEnd;
import static com.google.common.base.Strings.padStart;

@Component
public class IPv6Merger extends IPMerger<IPv6Record, Ipv6Range> {

    @Autowired
    public IPv6Merger(Resolver resolver) {
        super(resolver);
    }


    @Override
    public List<Ipv6Range> prefixRanges(Ipv6Range range) {
        return range.splitToPrefixes();
    }

    @Override
    public String getSignificantBinaryValues(Ipv6Range range) {
        return padStart(range.start().asBigInteger().toString(2), 128, '0').substring(0, PrefixUtils.getPrefixLength(range));
    }

    @Override
    public List<Ipv6Range> splitRanges(Ipv6Range range) {
        List<Ipv6Range> ranges = new ArrayList<>();
        if (range.size().compareTo(BigInteger.ZERO) > 0) {
            String binaryRange = getSignificantBinaryValues(range);
            String zeroRange = binaryRange + "0";
            ranges.add(Ipv6Range.from(new BigInteger(padEnd(zeroRange, 128, '0'), 2)).andPrefixLength(zeroRange.length()));
            String oneRange = binaryRange + "1";
            ranges.add(Ipv6Range.from(new BigInteger(padEnd(oneRange, 128, '0'), 2)).andPrefixLength(oneRange.length()));
        }
        return ranges;
    }
}
