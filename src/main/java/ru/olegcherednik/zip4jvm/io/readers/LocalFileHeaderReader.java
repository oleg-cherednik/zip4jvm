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
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public class LocalFileHeaderReader implements Reader<LocalFileHeader> {

    private final Function<Charset, Charset> customizeCharset;

    @Override
    public LocalFileHeader read(DataInput in) throws IOException {
        checkSignature(in);

        LocalFileHeader localFileHeader = new LocalFileHeader();

        localFileHeader.setVersionToExtract(Version.of(in.readWord()));
        localFileHeader.setGeneralPurposeFlag(new GeneralPurposeFlag(in.readWord()));
        localFileHeader.setCompressionMethod(CompressionMethod.parseCode(in.readWord()));
        localFileHeader.setLastModifiedTime((int) in.readDword());
        localFileHeader.setCrc32(in.readDword());
        localFileHeader.setCompressedSize(in.readDword());
        localFileHeader.setUncompressedSize(in.readDword());

        int fileNameLength = in.readWord();
        int extraFieldLength = in.readWord();
        Charset charset = localFileHeader.getGeneralPurposeFlag().getCharset();

        localFileHeader.setFileName(in.readString(fileNameLength, customizeCharset.apply(charset)));
        localFileHeader.setExtraField(readExtraFiled(extraFieldLength, localFileHeader, in));

        return localFileHeader;
    }

    protected ExtraField readExtraFiled(int size, LocalFileHeader localFileHeader, DataInput in) throws IOException {
        return new ExtraFieldReader(size, ExtraFieldReader.getReaders(localFileHeader)).read(in);
    }

    private static void checkSignature(DataInput in) throws IOException {
        long offs = in.getAbsOffs();

        if (in.readDwordSignature() != LocalFileHeader.SIGNATURE)
            throw new SignatureNotFoundException(LocalFileHeader.SIGNATURE, "LocalFileHeader", offs);
    }

}
