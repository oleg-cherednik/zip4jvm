/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.zip.CRC32;

/**
 * @author Oleg Cherednik
 * @since 07.03.2019
 */
@RequiredArgsConstructor
public final class ChecksumCalculator {

    private static final int BUF_SIZE = 1 << 14; //16384

    @NonNull
    private final Path file;
    @NonNull
    private final ProgressMonitor progressMonitor;

    public long calculate() throws ZipException {
        try (InputStream inputStream = new FileInputStream(file.toFile())) {
            byte[] buf = new byte[BUF_SIZE];
            int readLen = -2;
            CRC32 crc32 = new CRC32();

            while ((readLen = inputStream.read(buf)) != -1) {
                crc32.update(buf, 0, readLen);

                if (progressMonitor == null)
                    continue;

                progressMonitor.updateWorkCompleted(readLen);

                if (!progressMonitor.isCancelAllTasks())
                    continue;

                progressMonitor.setResult(ProgressMonitor.RESULT_CANCELLED);
                progressMonitor.setState(ProgressMonitor.STATE_READY);

                return 0;
            }

            return crc32.getValue();
        } catch(IOException e) {
            throw new ZipException(e);
        }
    }
}
