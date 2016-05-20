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
package net.nro.stats.components.resolver;

import net.nro.stats.components.parser.Record;
import net.nro.stats.resources.StatsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class OrderedResolver implements Resolver {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<String> registryPriorityOrder;

    @Autowired
    public OrderedResolver(@Value("${nro.stats.extended.order}") String[] registryPriorityOrder) {
        this.registryPriorityOrder = Arrays.asList(registryPriorityOrder);
    }

    @Override
    public <T extends Record> T resolve(T record1, T record2) {
        return (registryPriorityOrder.indexOf(record1.getRegistry()) > registryPriorityOrder.indexOf(record2.getRegistry())) ? record2 : record1;
    }

    @Override
    public <T extends Record> void recordConflict(T record1, List<T> record2list) {
        if (record1.getSource() == StatsSource.RIRSWAP) {
            //This is fallback scenario and should not be logged as conflict.
            return;
        }
        for (T record2: record2list) {
            if (record2.getSource() == StatsSource.RIRSWAP) {
                continue;
            }
            logger.warn("Conflict found for {} b/w {} and {}", record1.getRange().intersection(record2.getRange()), record1.getRegistry(), record2.getRegistry());
        }
    }
}
