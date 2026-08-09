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
package ru.olegcherednik.zip4jvm.io.in.compressed;

import ru.olegcherednik.zip4jvm.io.in.BaseRealDataInput;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.utils.quitely.Quietly;

import lombok.Getter;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * @author Oleg Cherednik
 * @since 21.11.2024
 */
public class CompressedDataInput extends BaseRealDataInput {

    protected final InputStream is;
    @Getter
    protected long absOffs;

    protected CompressedDataInput(InputStream is, DataInput in) {
        super(in);
        this.is = is;
    }

    // ---------- DataInput ----------

    @Override
    public long skip(long bytes) {
        long skipped = Quietly.doRuntime(() -> is.skip(bytes));

        if (skipped > 0)
            absOffs += skipped;

        return skipped;
    }

    // ---------- ReadBuffer ----------

    @Override
    public final int read(byte[] buf, int offs, int len) {
        int readNow = Quietly.doRuntime(() -> is.read(buf, offs, len));

        if (readNow == IOUtils.EOF || readNow == 0)
            return IOUtils.EOF;

        absOffs += readNow;
        return readNow;
    }

}
