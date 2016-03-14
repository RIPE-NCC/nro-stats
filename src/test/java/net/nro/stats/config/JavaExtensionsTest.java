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
package net.nro.stats.config;

import org.junit.Test;

import java.util.StringJoiner;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.nro.stats.config.JavaExtensions.rethrowConsumer;
import static net.nro.stats.config.JavaExtensions.rethrowFunction;
import static net.nro.stats.config.JavaExtensions.rethrowSupplier;
import static net.nro.stats.config.JavaExtensions.uncheck;

public class JavaExtensionsTest {
    @Test
    public void test_Consumer_with_checked_exceptions() throws IllegalAccessException {
        Stream.of("java.lang.Object", "java.lang.Integer", "java.lang.String")
                .forEach(rethrowConsumer(Class::forName));

        Stream.of("java.lang.Object", "java.lang.Integer", "java.lang.String")
                .forEach(rethrowConsumer(System.out::println));
    }

    @Test
    public void test_Function_with_checked_exceptions() throws ClassNotFoundException {
        Stream.of("Object", "Integer", "String")
                .map(rethrowFunction(className -> Class.forName("java.lang." + className)));

        Stream.of("java.lang.Object", "java.lang.Integer", "java.lang.String")
                .map(rethrowFunction(Class::forName))
                .collect(Collectors.toList());
    }

    @Test
    public void test_Supplier_with_checked_exceptions() throws ClassNotFoundException {
        Collector.of(
                rethrowSupplier(() -> new StringJoiner(new String(new byte[]{77, 97, 114, 107}, "UTF-8"))),
                StringJoiner::add, StringJoiner::merge, StringJoiner::toString);
    }

    @Test
    public void test_uncheck_exception_thrown_by_method() {
        Class clazz1 = uncheck(() -> Class.forName("java.lang.String"));

        Class clazz2 = uncheck(Class::forName, "java.lang.String");
    }

    @Test (expected = ClassNotFoundException.class)
    public void test_if_correct_exception_is_still_thrown_by_method() {
        Class clazz3 = uncheck(Class::forName, "INVALID");
    }
}
