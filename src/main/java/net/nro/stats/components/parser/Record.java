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

import net.ripe.commons.ip.AbstractRange;
import org.apache.commons.csv.CSVRecord;

import java.util.Comparator;

public abstract class Record<R extends AbstractRange> implements Line {
    private final String registry;
    private final String countryCode;
    private final String type;
    private final String start;
    private final String value;
    private final String date;
    private final String status;
    private final String regId;
    private final String[] extensions;

    public Record(String registry, String countryCode, String Type, String start, String value, String date, String status, String regId, String... extensions) {
        this.registry = registry;
        this.countryCode = countryCode;
        type = Type;
        this.start = start;
        this.value = value;
        this.date = date;
        this.status = status;
        this.regId = regId;
        this.extensions = extensions;
    }

    public Record(CSVRecord line) {
        if (!fits(line)) throw new RuntimeException("Given line was not a Record");

        this.registry = line.get(0);
        this.countryCode = line.get(1);
        type = line.get(2);
        this.start = line.get(3);
        this.value = line.get(4);
        this.date = line.get(5);
        this.status = line.get(6);

        String[] exts;
        if (line.size() > 7) {
            this.regId = line.get(7);
            exts = new String[line.size() - 8];
            for (int i = 8; i < line.size(); i++) {
                exts[i - 8] = line.get(i);
            }
        } else {
            this.regId = "";
            exts = new String[0];
        }

        this.extensions = exts;
    }

    public String getRegistry() {
        return registry;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getType() {
        return type;
    }

    public String getStart() {
        return start;
    }

    public String getValue() {
        return value;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getRegId() {
        return regId;
    }

    public String[] getExtensions() {
        return extensions;
    }

    public static boolean fits(CSVRecord line) {
        return IPv4Record.fits(line) || IPv6Record.fits(line) || ASNRecord.fits(line);
    }

    public  boolean hasExtensions() {
        return extensions.length > 0;
    }

    public abstract R getRange();

    public abstract Comparator getComparator();

    public abstract <T extends Record> T clone(R range);

    // TODO:
    @Override
    public String toString() {
        return String.format("Record[%s; %s; %s; %s; %s; %s; %s; %s;]", getRegistry(), getCountryCode(), getType(), getStart(), getValue(), getDate(), getStatus(), getRegId(), String.join(";", getExtensions()));
    }
}


