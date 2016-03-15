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

import net.nro.stats.components.DateTimeProvider;
import net.nro.stats.components.parser.ASNRecord;
import net.nro.stats.components.parser.Header;
import net.nro.stats.components.parser.IPv4Record;
import net.nro.stats.components.parser.IPv6Record;
import net.nro.stats.components.parser.Line;
import net.nro.stats.components.parser.Summary;
import net.nro.stats.config.DelegatedExtended;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ParsedRIRStats {
    private List<Header> headers;
    private List<Summary> summary;
    private List<IPv4Record> ipv4Records;
    private List<IPv6Record> ipv6Records;
    private List<ASNRecord> asnRecords;

    private String rir;

    public ParsedRIRStats(String rir) {
        this.rir = rir;
        this.headers = new ArrayList<>();
        this.summary = new ArrayList<>();
        this.ipv4Records = new ArrayList<>();
        this.ipv6Records = new ArrayList<>();
        this.asnRecords = new ArrayList<>();
    }

    public Stream<Line> getLines() {
        return Stream.of(headers, summary, asnRecords, ipv4Records, ipv6Records).flatMap(List::stream);
    }

    public void generateHeaderAndSummary(DelegatedExtended delegatedExtended, DateTimeProvider dateTimeProvider) {
        addSummary(new Summary(delegatedExtended.getIdentifier(), "asn", String.valueOf(asnRecords.size())));
        addSummary(new Summary(delegatedExtended.getIdentifier(), "ipv4", String.valueOf(ipv4Records.size())));
        addSummary(new Summary(delegatedExtended.getIdentifier(), "ipv6", String.valueOf(ipv6Records.size())));
        String today = dateTimeProvider.today();

        addHeader(new Header(delegatedExtended.getVersion(), delegatedExtended.getIdentifier(), today,
                String.valueOf(asnRecords.size() + ipv4Records.size()+ ipv6Records.size()),
                today, today, dateTimeProvider.localZone()));
    }

    public void addHeader(Header header) {
        headers.add(header);
    }

    public void addSummary(Summary summary) {
        this.summary.add(summary);
    }

    public void addIPv4Record(IPv4Record record) {
        ipv4Records.add(record);
    }

    public void addAllIPv4Record(List<IPv4Record> record) {
        ipv4Records.addAll(record);
    }

    public void addIPv6Record(IPv6Record record) {
        ipv6Records.add(record);
    }

    public void addAllIPv6Record(List<IPv6Record> records) {
        ipv6Records.addAll(records);
    }

    public void addAsnRecord(ASNRecord record) {
        asnRecords.add(record);
    }

    public void addAllAsnRecord(List<ASNRecord> records) {
        asnRecords.addAll(records);
    }

    public String getRir() {
        return rir;
    }

    public Stream<Header> getHeaders() {
        return headers.stream();
    }

    public Stream<Summary> getSummary() {
        return summary.stream();
    }

    public List<IPv4Record> getIpv4Records() {
        return ipv4Records;
    }

    public List<IPv6Record> getIpv6Records() {
        return ipv6Records;
    }

    public List<ASNRecord> getAsnRecords() {
        return asnRecords;
    }
}
