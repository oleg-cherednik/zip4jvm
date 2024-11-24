/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 10.03.2019
 */
@RequiredArgsConstructor
public final class LocalFileHeaderWriter implements Writer {

    private final LocalFileHeader localFileHeader;

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) throws IOException {
        Charset charset = localFileHeader.getGeneralPurposeFlag().getCharset();
        byte[] fileName = localFileHeader.getFileName(charset);

        out.writeDwordSignature(LocalFileHeader.SIGNATURE);
        out.writeWord(localFileHeader.getVersionToExtract().getData());
        out.writeWord(localFileHeader.getGeneralPurposeFlag().getAsInt(localFileHeader.getOriginalCompressionMethod()));
        out.writeWord(localFileHeader.getCompressionMethod().getCode());
        out.writeDword(localFileHeader.getLastModifiedTime());
        out.writeDword(localFileHeader.getCrc32());
        out.writeDword(localFileHeader.getCompressedSize());
        out.writeDword(localFileHeader.getUncompressedSize());
        out.writeWord(fileName.length);
        out.writeWord(localFileHeader.getExtraField().getSize());
        out.writeBytes(fileName);

        ExtraField extraField = localFileHeader.getExtraField();

        if (extraField instanceof PkwareExtraField)
            new ExtraFieldWriter((PkwareExtraField) extraField).write(out);
    }

}
