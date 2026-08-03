/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package ru.olegcherednik.zip4jvm.io.out.compressed;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.settings.CompressionLevelEnum;

import java.util.zip.Deflater;

/**
 * @author Oleg Cherednik
 * @since 26.07.2019
 */
@SuppressWarnings("PMD.SingularField")
final class DeflateEntryDataOutput extends CompressedEntryDataOutput {

    private static final int FOUR = 4;

    private final byte[] buf1 = new byte[1024 * 4];
    private final byte[] buf2 = new byte[1];
    private final Deflater delegate;

    private boolean firstBytesRead;

    DeflateEntryDataOutput(DataOutput out, CompressionLevelEnum compressionLevel) {
        super(out);
        delegate = new Deflater(level(compressionLevel));
    }

    private void deflate() {
        int len = delegate.deflate(buf1, 0, buf1.length);

        if (len <= 0)
            return;

        if (delegate.finished()) {
            if (len == FOUR)
                return;
            if (len < FOUR)
                return;
            len -= FOUR;
        }

        if (firstBytesRead)
            out.write(buf1, 0, len);
        else {
            out.write(buf1, 2, len - 2);
            firstBytesRead = true;
        }
    }

    private void finish() {
        if (delegate.finished())
            return;

        delegate.finish();

        while (!delegate.finished()) {
            deflate();
        }
    }

    // ---------- WriteBuffer ----------

    @Override
    public void write(int b) {
        buf2[0] = (byte) b;
        delegate.setInput(buf2);

        while (!delegate.needsInput()) {
            deflate();
        }
    }

    // ---------- AutoCloseable ----------

    @Override
    public void close() {
        finish();
        super.close();
    }

    // ---------- static ----------

    private static int level(CompressionLevelEnum compressionLevel) {
        if (compressionLevel == CompressionLevelEnum.SUPER_FAST)
            return Deflater.NO_COMPRESSION;
        if (compressionLevel == CompressionLevelEnum.FAST)
            return Deflater.BEST_SPEED;
        if (compressionLevel == CompressionLevelEnum.NORMAL)
            return Deflater.DEFAULT_COMPRESSION;
        if (compressionLevel == CompressionLevelEnum.MAXIMUM)
            return Deflater.BEST_COMPRESSION;
        return Deflater.DEFAULT_COMPRESSION;
    }

}
