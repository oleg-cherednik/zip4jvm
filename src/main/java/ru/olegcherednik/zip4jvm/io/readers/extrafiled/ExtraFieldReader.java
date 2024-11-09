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
package ru.olegcherednik.zip4jvm.io.readers.extrafiled;

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.ExtraFieldRecordReader;
import ru.olegcherednik.zip4jvm.io.readers.zip64.ExtendedInfoReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.extrafield.AlignmentExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.ExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AesExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.AlignmentExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExecutableJarMarkerExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.ExtendedTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipNewUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.InfoZipOldUnixExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.NtfsTimestampExtraFieldRecord;
import ru.olegcherednik.zip4jvm.model.extrafield.records.StrongEncryptionHeaderExtraFieldRecord;
import ru.olegcherednik.zip4jvm.utils.function.Reader;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_ENTRY_SIZE;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_LOCAL_FILE_HEADER_OFFS;
import static ru.olegcherednik.zip4jvm.model.ZipModel.MAX_TOTAL_DISKS;

/**
 * @author Oleg Cherednik
 * @since 14.04.2019
 */
@RequiredArgsConstructor
public class ExtraFieldReader implements Reader<ExtraField> {

    private final int size;
    protected final Map<Integer, Function<Integer, Reader<? extends PkwareExtraField.Record>>> readers;

    public static Map<Integer, Function<Integer, Reader<? extends PkwareExtraField.Record>>> getReaders(
            CentralDirectory.FileHeader fileHeader) {
        boolean uncompressedSize = fileHeader.getUncompressedSize() == MAX_ENTRY_SIZE;
        boolean compressedSize = fileHeader.getCompressedSize() == MAX_ENTRY_SIZE;
        boolean offs = fileHeader.getLocalFileHeaderRelativeOffs() == MAX_LOCAL_FILE_HEADER_OFFS;
        boolean disk = fileHeader.getDiskNo() == MAX_TOTAL_DISKS;
        return getReaders(uncompressedSize, compressedSize, offs, disk);
    }

    public static Map<Integer, Function<Integer, Reader<? extends PkwareExtraField.Record>>> getReaders(
            LocalFileHeader localFileHeader) {
        boolean uncompressedSize = localFileHeader.getUncompressedSize() == MAX_ENTRY_SIZE;
        boolean compressedSize = localFileHeader.getCompressedSize() == MAX_ENTRY_SIZE;
        return getReaders(uncompressedSize, compressedSize, false, false);
    }

    private static Map<Integer, Function<Integer, Reader<? extends PkwareExtraField.Record>>> getReaders(
            boolean uncompressedSize,
            boolean compressedSize,
            boolean offs,
            boolean disk) {
        Map<Integer, Function<Integer, Reader<? extends PkwareExtraField.Record>>> map = new HashMap<>();

        map.put(Zip64.ExtendedInfo.SIGNATURE, size -> new ExtendedInfoReader(size,
                                                                             uncompressedSize,
                                                                             compressedSize,
                                                                             offs, disk));
        map.put(AesExtraFieldRecord.SIGNATURE, AesExtraFieldRecordReader::new);
        map.put(NtfsTimestampExtraFieldRecord.SIGNATURE, NtfsTimestampExtraFieldRecordReader::new);
        map.put(InfoZipOldUnixExtraFieldRecord.SIGNATURE, InfoZipOldUnixExtraFieldRecordReader::new);
        map.put(InfoZipNewUnixExtraFieldRecord.SIGNATURE, InfoZipNewUnixExtraFieldRecordReader::new);
        map.put(ExtendedTimestampExtraFieldRecord.SIGNATURE, ExtendedTimestampExtraFieldRecordReader::new);
        map.put(StrongEncryptionHeaderExtraFieldRecord.SIGNATURE, StrongEncryptionHeaderExtraFieldRecordReader::new);
        map.put(ExecutableJarMarkerExtraFieldRecord.SIGNATURE, ExecutableJarMarkerExtraFieldRecordReader::new);
        map.put(AlignmentExtraFieldRecord.SIGNATURE, AlignmentExtraFieldRecordReader::new);

        return map;
    }

    @Override
    public ExtraField read(DataInput in) {
        if (size == 0)
            return PkwareExtraField.NULL;

        int headerSize = ExtraFieldRecordReader.getHeaderSize(in);

        if (size < headerSize)
            return new AlignmentExtraField(in.readBytes(size));

        return readPkwareExtraField(in);
    }

    protected PkwareExtraField readPkwareExtraField(DataInput in) {
        List<PkwareExtraField.Record> records = new ArrayList<>();
        long offsMax = in.getAbsoluteOffs() + size;

        while (in.getAbsoluteOffs() < offsMax) {
            records.add(getExtraFieldRecordReader().read(in));
        }

        return new PkwareExtraField(records);
    }

    protected ExtraFieldRecordReader getExtraFieldRecordReader() {
        return new ExtraFieldRecordReader(readers);
    }

}
