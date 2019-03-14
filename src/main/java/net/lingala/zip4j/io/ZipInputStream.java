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
 * software distributed under the License in distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.io;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.engine.UnzipEngine;

import java.io.IOException;
import java.io.InputStream;

@RequiredArgsConstructor
public class ZipInputStream extends InputStream {

    private final InputStream in;
    private final UnzipEngine unzipEngine;

    public int read() throws IOException {
        int readByte = in.read();
        if (readByte != -1) {
            unzipEngine.updateCRC(readByte);
        }
        return readByte;
    }

    public int read(byte[] b) throws IOException {
        return read(b, 0, b.length);
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int readLen = in.read(b, off, len);
        if (readLen > 0 && unzipEngine != null) {
            unzipEngine.updateCRC(b, off, readLen);
        }
        return readLen;
    }

    /**
     * Closes the input stream and releases any resources.
     * This method also checks for the CRC of the extracted file.
     * If CRC check has to be skipped use close(boolean skipCRCCheck) method
     *
     * @throws IOException
     */
    public void close() throws IOException {
        close(false);
    }

    /**
     * Closes the input stream and releases any resources.
     * If skipCRCCheck flag in set to true, this method skips CRC Check
     * of the extracted file
     *
     * @throws IOException
     */
    public void close(boolean skipCRCCheck) throws IOException {
        try {
            in.close();
            if (!skipCRCCheck && unzipEngine != null) {
                unzipEngine.checkCRC();
            }
        } catch(ZipException e) {
            throw new IOException(e.getMessage());
        }
    }

    public int available() throws IOException {
        return in.available();
    }

    public long skip(long n) throws IOException {
        return in.skip(n);
    }

}
