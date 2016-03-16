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

import com.google.common.base.Strings;
import net.nro.stats.components.DateTimeProvider;
import net.nro.stats.components.parser.Header;
import net.nro.stats.config.ExtendedOutputConfig;
import net.nro.stats.resources.ParsedRIRStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class HeaderMerger {

    private ExtendedOutputConfig extendedOutputConfig;
    private DateTimeProvider dateTimeProvider;

    @Autowired
    public HeaderMerger(ExtendedOutputConfig extendedOutputConfig, DateTimeProvider dateTimeProvider) {
        this.extendedOutputConfig = extendedOutputConfig;
        this.dateTimeProvider = dateTimeProvider;
    }

    public Header merge(List<Header> recordsList, ParsedRIRStats nroStats) {
        String today = dateTimeProvider.today();

        String startDate = recordsList.stream().map(Header::getStartDate).filter(s -> !Strings.isNullOrEmpty(s)).map(Long::parseLong).min(Long::compare).map(String::valueOf).get();

        return new Header(extendedOutputConfig.getVersion(), extendedOutputConfig.getIdentifier(), today,
                String.valueOf(nroStats.getAsnRecords().size() + nroStats.getIpv4Records().size()+ nroStats.getIpv6Records().size()),
                startDate, today, dateTimeProvider.localZone());
    }
}
