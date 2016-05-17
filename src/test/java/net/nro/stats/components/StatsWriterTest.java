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
package net.nro.stats.components;

import net.nro.stats.components.parser.Header;
import net.nro.stats.components.parser.Summary;
import net.nro.stats.config.ExtendedOutputConfig;
import net.nro.stats.resources.ParsedRIRStats;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class StatsWriterTest {

    ExtendedOutputConfig out = new ExtendedOutputConfig();

    StatsWriter statsWriter = new StatsWriter(out, Charset.forName("US-ASCII"));

    @Before
    public void setUp() throws Exception {
        out.setFolder("tmp");
        out.setFile("file");
    }

    @Test
    public void testWriteClean() throws Exception {
        out.setBackup(true);
        out.setBackupFormat("$");
        // -- //
        ParsedRIRStats nroStats = new ParsedRIRStats("nro");
        nroStats.addHeader(new Header("2.3", "nro", "20160301", "0", "20160301", "20160301", "+0100"));
        nroStats.addSummary(new Summary("nro", "asn", "0"));

        statsWriter.write(nroStats);
        assertFalse(Files.exists(Paths.get(out.getFolder(), out.getTmpFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile())));

        statsWriter.write(nroStats);
        assertFalse(Files.exists(Paths.get(out.getFolder(), out.getTmpFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile() + ".$")));
        assertTrue(Files.isSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())));
        assertTrue(Files.readSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())).getFileName().toString()
                .equals(out.getFile() + ".$"));

        out.setBackupFormat("$$");
        statsWriter.write(nroStats);
        assertFalse(Files.exists(Paths.get(out.getFolder(), out.getTmpFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile() + ".$$")));
        assertTrue(Files.isSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())));
        assertTrue(Files.readSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())).getFileName().toString()
                .equals(out.getFile() + ".$$"));
    }

    @Test
    public void testWithoutBackup() throws Exception {
        out.setBackup(false);
        out.setBackupFormat("$");

        ParsedRIRStats nroStats = new ParsedRIRStats("nro");
        nroStats.addHeader(new Header("2.3", "nro", "20160301", "0", "20160301", "20160301", "+0100"));
        nroStats.addSummary(new Summary("nro", "asn", "0"));

        statsWriter.write(nroStats);
        assertFalse(Files.exists(Paths.get(out.getFolder(), out.getTmpFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile())));

        statsWriter.write(nroStats);
        assertFalse(Files.exists(Paths.get(out.getFolder(), out.getTmpFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile() + ".$")));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getPreviousFileLink())));
        assertTrue(Files.isSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())));
        assertTrue(Files.readSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())).getFileName().toString()
                .equals(out.getFile() + ".$"));
        assertTrue(Files.exists(Paths.get(out.getFolder())));

        out.setBackupFormat("$$");
        statsWriter.write(nroStats);
        assertFalse(Files.exists(Paths.get(out.getFolder(), out.getFile() + ".$")));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile() + ".$$")));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getFile())));
        assertTrue(Files.exists(Paths.get(out.getFolder(), out.getPreviousFileLink())));
        assertTrue(Files.isSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())));
        assertTrue(Files.readSymbolicLink(Paths.get(out.getFolder(), out.getPreviousFileLink())).getFileName().toString()
                .equals(out.getFile() + ".$$"));
        assertTrue(Files.exists(Paths.get(out.getFolder())));
    }

    @After
    public void cleanup() throws Exception {
        if (Files.isDirectory(Paths.get(out.getFolder()))) {
            try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(out.getFolder()))) {
                for (Path path : directoryStream) {
                    Files.deleteIfExists(path);
                }
            }
        }
        Files.deleteIfExists(Paths.get(out.getFolder()));
    }
}