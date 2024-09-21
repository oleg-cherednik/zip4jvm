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

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.extrafiled.ExtraFieldReader;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Version;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 08.03.2019
 */
@RequiredArgsConstructor
public class LocalFileHeaderReader implements Reader<LocalFileHeader> {

    private final long absoluteOffs;
    private final Function<Charset, Charset> customizeCharset;

    @Override
    public final LocalFileHeader read(DataInput in) {
        findSignature(in);
        return readLocalFileHeader(in);
    }

    protected LocalFileHeader readLocalFileHeader(DataInput in) {
        in.skip(in.dwordSignatureSize());

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
        localFileHeader.setExtraField(readExtraFiled(localFileHeader, in, extraFieldLength));

        return localFileHeader;
    }

    protected ExtraField readExtraFiled(LocalFileHeader localFileHeader, DataInput in, int size) {
        return new ExtraFieldReader(ExtraFieldReader.getReaders(localFileHeader)).read(in, size);
    }

    private void findSignature(DataInput in) {
        in.seek(absoluteOffs);

        if (in.readDwordSignature() != LocalFileHeader.SIGNATURE)
            throw new Zip4jvmException("invalid local file header signature");

        in.backward(in.dwordSignatureSize());
    }

}
