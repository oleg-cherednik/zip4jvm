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
package ru.olegcherednik.zip4jvm.io.writers;

import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.utils.function.Writer;

import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
final class FileHeaderWriter implements Writer {

    private final List<CentralDirectory.FileHeader> fileHeaders;

    // ---------- Writer ----------

    @Override
    public void write(DataOutput out) {
        for (CentralDirectory.FileHeader fileHeader : fileHeaders)
            writeFileHeader(fileHeader, out);
    }

    // ---------- static ----------

    private static void writeFileHeader(CentralDirectory.FileHeader fileHeader, DataOutput out) {
        Charset charset = fileHeader.getGeneralPurposeFlag().getCharset();
        byte[] fileName = fileHeader.getFileName(charset);
        byte[] fileComment = fileHeader.getComment(charset);

        out.writeDwordSignature(CentralDirectory.FileHeader.SIGNATURE);
        out.writeWord(fileHeader.getVersionMadeBy().getData());
        out.writeWord(fileHeader.getVersionToExtract().getData());
        out.writeWord(fileHeader.getGeneralPurposeFlag().getAsInt(fileHeader.getOriginalCompressionMethod()));
        out.writeWord(fileHeader.getCompression().getCode());
        out.writeDword(fileHeader.getLastModifiedTime());
        out.writeDword(fileHeader.getCrc32());
        out.writeDword(fileHeader.getCompressedSize());
        out.writeDword(fileHeader.getUncompressedSize());
        out.writeWord(fileName.length);
        out.writeWord(fileHeader.getExtraField().getSize());
        out.writeWord(fileComment.length);
        out.writeWord(fileHeader.getDiskNo());
        out.writeBytes(fileHeader.getInternalFileAttributes().getData());
        out.writeBytes(fileHeader.getExternalFileAttributes().getData());
        out.writeDword(fileHeader.getLocalFileHeaderRelativeOffs());
        out.writeBytes(fileName);
        new ExtraFieldWriter(fileHeader.getExtraField()).write(out);
        out.writeBytes(fileComment);
    }

}
