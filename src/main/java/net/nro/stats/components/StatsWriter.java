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
import net.nro.stats.config.ExtendedOutputConfig;
import net.nro.stats.resources.ParsedRIRStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static net.nro.stats.config.JavaExtensions.rethrowConsumer;

@Component
public class StatsWriter {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ExtendedOutputConfig extendedOutputConfig;

    private Charset charset;

    @Autowired
    public StatsWriter(
            ExtendedOutputConfig extendedOutputConfig,
            Charset charset) {
        this.extendedOutputConfig = extendedOutputConfig;
        this.charset = charset;
    }

    public void write(ParsedRIRStats nroStats) {
        validateOutFolder();

        Path outFile = Paths.get(extendedOutputConfig.getFolder(), extendedOutputConfig.getFile());
        Path outFileTmp = Paths.get(extendedOutputConfig.getFolder(), extendedOutputConfig.getTmpFile());

        if (Files.exists(outFile) && extendedOutputConfig.getBackup()) {
            backupPreviousFile(outFile);
        }

        if (Files.exists(outFileTmp)) {
            cleanup(outFileTmp);
        }

        write(nroStats.getLines(), outFileTmp);

        move(outFileTmp, outFile);

    }

    private void move(Path outFileTmp, Path outFile) {
        try {
            Files.move(outFileTmp, outFile, ATOMIC_MOVE, REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Unable to move current generated file as new version");
            throw new RuntimeException(e);
        }
    }

    private void write(Stream<Line> targetLines, Path outFileTmp) {
        try (BufferedWriter writer = Files.newBufferedWriter(outFileTmp, charset)) {
            targetLines.forEachOrdered(
                    rethrowConsumer(line ->
                    {
                        writer.write(line.toString());
                        writer.newLine();
                    }
                )
            );
        } catch (IOException e) {
            logger.error("Unable to write the output file");
            throw new RuntimeException(e);
        }
    }

    private void cleanup(Path outFileTmp) {
        logger.warn("Last attempt to generate file failed. Cleaning up.");
        try {
            Files.delete(outFileTmp);
        } catch (IOException e) {
            logger.error("Unable to delete {} previously generated temp file", outFileTmp);
            throw new RuntimeException(e);
        }
    }

    private void backupPreviousFile(Path outFile) {
        logger.info("File {} already present, backing it up", outFile);
        try {
            SimpleDateFormat df = new SimpleDateFormat(extendedOutputConfig.getBackupFormat());
            Path outFileOld = Paths.get(extendedOutputConfig.getFolder(),
                    extendedOutputConfig.getFile() + "." + df.format(Files.getLastModifiedTime(outFile).toMillis()));
            Files.copy(outFile, outFileOld, COPY_ATTRIBUTES);
        } catch (Exception e) {
            logger.error("Unable to move to backup old file", e);
            throw new RuntimeException(e);
        }
    }

    private void validateOutFolder() {
        Path outFolder = Paths.get(extendedOutputConfig.getFolder());
        if (Files.notExists(outFolder)) {
            logger.info("outFolder {} missing. Creating it", outFolder);
            try {
                Files.createDirectory(outFolder);
            } catch (IOException io) {
                logger.error("Unable to create out folder.", io);
                throw new RuntimeException(io);
            }
        }
    }
}
