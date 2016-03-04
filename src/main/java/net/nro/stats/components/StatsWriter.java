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

import net.nro.stats.components.parser.Line;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.List;

@Component
public class StatsWriter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String folder;

    private Charset charset;

    @Autowired
    public StatsWriter(@Value("${nro.stats.extended.output}") String folder, Charset charset) {
        this.folder = folder;
        this.charset = charset;
    }

    public void write(List<Line> targetLines) {
        // convert lines back to a file and write it
        // file should be put in 'nro.stats.extended.output'
        Path outFolder = Paths.get(folder);
        if (Files.notExists(outFolder)) {
            logger.info("outFolder {} missing. Creating it", outFolder);
            try {
                Files.createDirectory(outFolder);
            } catch (IOException io) {
                logger.error("Unable to create out folder.", io);
                throw new RuntimeException(io);
            }
        }
        Path outFile = Paths.get(folder, "nro.stats.extended.output");
        if (Files.exists(outFile)) {
            logger.info("File {} already present, moving it", outFile);
            try {
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd.hh.mm.ss");
                Path outFileOld = Paths.get(folder, "nro.stats.extended.output." + df.format(Files.getLastModifiedTime(outFile).toMillis()));
                Files.move(outFile, outFileOld, StandardCopyOption.ATOMIC_MOVE);
            } catch (Exception e) {
                logger.error("Unable to move to backup old file", e);
                throw new RuntimeException(e);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(outFile, charset);) {
            for (Line line : targetLines) {
                writer.write(line.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            logger.error("Unable to write the output file");
            throw new RuntimeException(e);
        }

    }
}
