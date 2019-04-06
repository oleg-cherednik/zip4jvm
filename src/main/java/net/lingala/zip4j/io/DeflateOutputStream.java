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

package net.lingala.zip4j.io;

import lombok.NonNull;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 22.03.2019
 */
public class DeflateOutputStream extends CipherOutputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    private final Deflater deflater = new Deflater();

    private boolean firstBytesRead;

    public DeflateOutputStream(@NonNull SplitOutputStream out, ZipModel zipModel) {
        super(out, zipModel);
    }

    @Override
    protected void putNextEntry(Path file, String fileNameStream, ZipParameters parameters) {
        super.putNextEntry(file, fileNameStream, parameters);

        if (parameters.getCompressionMethod() != CompressionMethod.DEFLATE)
            return;

        deflater.reset();
        deflater.setLevel(parameters.getCompressionLevel().getValue());
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(buf, 0, buf.length);
        if (len > 0) {
            if (deflater.finished()) {
                if (len == 4)
                    return;
                if (len < 4)
                    return;
                len -= 4;
            }
            if (!firstBytesRead) {
                super.write(buf, 2, len - 2);
                firstBytesRead = true;
            } else {
                super.write(buf, 0, len);
            }
        }
    }

    @Override
    public void write(byte[] buf, int offs, int len) throws IOException {
        crc.update(buf, offs, len);
        totalBytesRead += len;

        if (parameters.getCompressionMethod() != CompressionMethod.DEFLATE)
            super.write(buf, offs, len);
        else {
            deflater.setInput(buf, offs, len);
            while (!deflater.needsInput()) {
                deflate();
            }
        }
    }

    @Override
    public void closeEntry() throws IOException, ZipException {
        if (parameters.getCompressionMethod() == CompressionMethod.DEFLATE) {
            if (!deflater.finished()) {
                deflater.finish();

                while (!deflater.finished()) {
                    deflate();
                }
            }

            firstBytesRead = false;
        }
        super.closeEntry();
    }

}
