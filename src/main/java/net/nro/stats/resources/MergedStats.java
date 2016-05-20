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
package net.nro.stats.resources;

import net.nro.stats.components.merger.ASNIntervalTree;
import net.nro.stats.components.merger.IPNode;
import net.nro.stats.components.parser.IPv4Record;
import net.nro.stats.components.parser.IPv6Record;

public class MergedStats {
    private ASNIntervalTree asns;
    private IPNode<IPv4Record> ipv4s;
    private IPNode<IPv6Record> ipv6s;
    private String headerStartDate;

    public ASNIntervalTree getAsns() {
        return asns;
    }

    public void setAsns(ASNIntervalTree asns) {
        this.asns = asns;
    }

    public IPNode<IPv4Record> getIpv4s() {
        return ipv4s;
    }

    public void setIpv4s(IPNode<IPv4Record> ipv4s) {
        this.ipv4s = ipv4s;
    }

    public IPNode<IPv6Record> getIpv6s() {
        return ipv6s;
    }

    public void setIpv6s(IPNode<IPv6Record> ipv6s) {
        this.ipv6s = ipv6s;
    }

    public void setHeaderStartDate(String headerStartDate) {
        this.headerStartDate = headerStartDate;
    }

    public String getHeaderStartDate() {
        return headerStartDate;
    }
}
