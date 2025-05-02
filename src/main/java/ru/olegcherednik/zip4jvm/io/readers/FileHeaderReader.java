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
package ru.olegcherednik.zip4jvm.io.readers;

import ru.olegcherednik.zip4jvm.exception.SignatureNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.charset.CharsetProvider;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
public class FileHeaderReader implements Reader<List<CentralDirectory.FileHeader>> {

    private final long totalEntries;
    private final CharsetProvider charsetProvider;

    @Override
    public final List<CentralDirectory.FileHeader> read(DataInput in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    protected CentralDirectory.FileHeader readFileHeader(DataInput in) throws IOException {
        checkSignature(in);

        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(Version.of(in.readWord()));
        fileHeader.setVersionToExtract(Version.of(in.readWord()));
        fileHeader.setGeneralPurposeFlag(new GeneralPurposeFlag(in.readWord()));
        fileHeader.setCompression(Compression.parseCode(in.readWord()));
        fileHeader.setLastModifiedTime((int) in.readDword());
        fileHeader.setCrc32(in.readDword());
        fileHeader.setCompressedSize(in.readDword());
        fileHeader.setUncompressedSize(in.readDword());

        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        Charset charset = charsetProvider.apply(fileHeader.getGeneralPurposeFlag().getCharset());

        fileHeader.setCommentLength(in.readWord());
        fileHeader.setDiskNo(in.readWord());
        fileHeader.setInternalFileAttributes(getInternalFileAttribute(in.readBytes(InternalFileAttributes.SIZE)));
        fileHeader.setExternalFileAttributes(getExternalFileAttribute(in.readBytes(ExternalFileAttributes.SIZE)));
        fileHeader.setLocalFileHeaderRelativeOffs(in.readDword());
        fileHeader.setFileName(in.readString(fileNameLength, charset));
        fileHeader.setExtraField((PkwareExtraField) getExtraFiledReader(extraFieldLength, fileHeader).read(in));
        fileHeader.setComment(in.readString(fileHeader.getCommentLength(), charset));

        return fileHeader;
    }

    private static void checkSignature(DataInput in) throws IOException {
        long absOffs = in.getAbsOffs();

        if (in.readDwordSignature() != CentralDirectory.FileHeader.SIGNATURE)
            throw new SignatureNotFoundException(CentralDirectory.FileHeader.SIGNATURE, "CentralDirectory", absOffs);
    }

    private static InternalFileAttributes getInternalFileAttribute(byte[] data) {
        return new InternalFileAttributes(data);
    }

    private static ExternalFileAttributes getExternalFileAttribute(byte[] data) {
        return new ExternalFileAttributes(data);
    }

    protected ExtraFieldReader getExtraFiledReader(int size, CentralDirectory.FileHeader fileHeader) {
        return new ExtraFieldReader(size, ExtraFieldReader.getReaders(fileHeader));
    }

}
