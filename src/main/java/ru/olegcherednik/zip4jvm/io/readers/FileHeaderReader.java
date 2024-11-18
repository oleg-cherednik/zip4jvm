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
import ru.olegcherednik.zip4jvm.io.in.data.xxx.XxxDataInput;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.utils.function.XxxReader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 26.04.2019
 */
@RequiredArgsConstructor
public class FileHeaderReader implements XxxReader<List<CentralDirectory.FileHeader>> {

    private final long totalEntries;
    private final Function<Charset, Charset> customizeCharset;

    @Override
    public final List<CentralDirectory.FileHeader> read(XxxDataInput in) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new LinkedList<>();

        for (int i = 0; i < totalEntries; i++)
            fileHeaders.add(readFileHeader(in));

        return fileHeaders;
    }

    protected CentralDirectory.FileHeader readFileHeader(XxxDataInput in) throws IOException {
        checkSignature(in);

        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setVersionMadeBy(Version.of(in.readWord()));
        fileHeader.setVersionToExtract(Version.of(in.readWord()));
        fileHeader.setGeneralPurposeFlagData(in.readWord());
        fileHeader.setCompressionMethod(CompressionMethod.parseCode(in.readWord()));
        fileHeader.setLastModifiedTime((int) in.readDword());
        fileHeader.setCrc32(in.readDword());
        fileHeader.setCompressedSize(in.readDword());
        fileHeader.setUncompressedSize(in.readDword());

        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        int fileCommentLength = in.readWord();
        Charset charset = customizeCharset.apply(fileHeader.getGeneralPurposeFlag().getCharset());

        fileHeader.setDiskNo(in.readWord());
        fileHeader.setInternalFileAttributes(getInternalFileAttribute(in.readBytes(InternalFileAttributes.SIZE)));
        fileHeader.setExternalFileAttributes(getExternalFileAttribute(in.readBytes(ExternalFileAttributes.SIZE)));
        fileHeader.setLocalFileHeaderRelativeOffs(in.readDword());
        fileHeader.setFileName(in.readString(fileNameLength, charset));
        fileHeader.setExtraField((PkwareExtraField) getExtraFiledReader(extraFieldLength, fileHeader).read(in));
        fileHeader.setComment(in.readString(fileCommentLength, charset));

        return fileHeader;
    }

    private static void checkSignature(XxxDataInput in) throws IOException {
        long offs = in.getAbsOffs();

        if (in.readDwordSignature() != CentralDirectory.FileHeader.SIGNATURE)
            throw new SignatureNotFoundException(CentralDirectory.FileHeader.SIGNATURE, "CentralDirectory", offs);
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
