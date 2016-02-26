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

import org.apache.commons.csv.CSVRecord;

public class Header implements Line {
    private final String version;
    private final String registry;
    private final String serial;
    private final String records;
    private final String startDate;
    private final String endDate;
    private final String utcOffset;

    public Header(String version, String registry, String serial, String records, String startDate, String endDate, String utcOffset) {
        this.version = version;
        this.registry = registry;
        this.serial = serial;
        this.records = records;
        this.startDate = startDate;
        this.endDate = endDate;
        this.utcOffset = utcOffset;
    }

    public Header(CSVRecord line) {
        if (!fits(line)) throw new RuntimeException("Given line was not a Header");

        this.version = line.get(0);
        this.registry = line.get(1);
        this.serial = line.get(2);
        this.records = line.get(3);
        this.startDate = line.get(4);
        this.endDate = line.get(5);
        this.utcOffset = line.get(6);
    }

    public String getVersion() {
        return version;
    }

    public String getRegistry() {
        return registry;
    }

    public String getSerial() {
        return serial;
    }

    public static boolean fits(CSVRecord line) {
        return line.size() == 7 && ("2.3".equals(line.get(0)) || "2".equals(line.get(0)));
    }

    public String getRecords() {
        return records;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getUtcOffset() {
        return utcOffset;
    }
}
