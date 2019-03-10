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

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.CompressionMethod;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.InternalZipConstants;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.zip.Deflater;

public abstract class DeflateOutputStream extends CipherOutputStream {

    private final byte[] buf = new byte[InternalZipConstants.BUFF_SIZE];
    private final Deflater deflater = new Deflater();

    private boolean firstBytesRead;

    protected DeflateOutputStream(OutputStream out, ZipModel zipModel) {
        super(out, zipModel);
    }

    @Override
    public void putNextEntry(Path file, ZipParameters parameters) throws ZipException {
        super.putNextEntry(file, parameters);

        if (parameters.getCompressionMethod() != CompressionMethod.DEFLATE)
            return;

        deflater.reset();
        deflater.setLevel(parameters.getCompressionLevel().getValue());
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    private void deflate() throws IOException {
        int len = deflater.deflate(buf, 0, buf.length);
        if (len > 0) {
            if (deflater.finished()) {
                if (len == 4) return;
                if (len < 4) {
                    decrementCompressedFileSize(4 - len);
                    return;
                }
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
    public void write(int bval) throws IOException {
        byte[] b = new byte[1];
        b[0] = (byte)bval;
        write(b, 0, 1);
    }

    public void write(byte[] buf, int off, int len) throws IOException {
        if (zipParameters.getCompressionMethod() != CompressionMethod.DEFLATE) {
            super.write(buf, off, len);
        } else {
            deflater.setInput(buf, off, len);
            while (!deflater.needsInput()) {
                deflate();
            }
        }
    }

    public void closeEntry() throws IOException, ZipException {
        if (zipParameters.getCompressionMethod() == CompressionMethod.DEFLATE) {
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

    public void finish() throws IOException, ZipException {
        super.finish();
    }
}
