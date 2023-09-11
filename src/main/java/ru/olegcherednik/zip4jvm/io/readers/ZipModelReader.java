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

import ru.olegcherednik.zip4jvm.io.in.file.DataInputFile;
import ru.olegcherednik.zip4jvm.io.in.file.LittleEndianDataInputFile;
import ru.olegcherednik.zip4jvm.io.readers.centraldirectory.CentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.centraldirectory.EncryptedCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.io.readers.zip64.Zip64Reader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.CustomizeCharset;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;
import ru.olegcherednik.zip4jvm.utils.ValidationUtils;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
public final class ZipModelReader extends BaseZipModelReader {

    public ZipModelReader(SrcZip srcZip) {
        this(srcZip, Charsets.UNMODIFIED, null);
    }

    public ZipModelReader(SrcZip srcZip, CustomizeCharset customizeCharset, PasswordProvider passwordProvider) {
        super(srcZip, customizeCharset, passwordProvider);
    }

    public ZipModel read() {
        readCentralData();
        return new ZipModelBuilder(srcZip,
                                   endCentralDirectory,
                                   zip64,
                                   centralDirectory,
                                   customizeCharset).build();
    }

    /**
     * Returns <code>1</code> for single zip and <code>{@literal >}1</code> for split
     *
     * @param srcZip the zip archive model
     * @return retrieves total disks in the zip archive
     */
    public static int getTotalDisks(SrcZip srcZip) {
        ZipModelReader reader = new ZipModelReader(srcZip);

        try (DataInputFile in = reader.createDataInput()) {
            reader.readEndCentralDirectory(in);
            reader.readZip64EndCentralDirectoryLocator(in);

            if (reader.zip64 == Zip64.NULL)
                return reader.endCentralDirectory.getTotalDisks() + 1;

            long totalDisks = reader.zip64.getEndCentralDirectoryLocator().getTotalDisks();
            ValidationUtils.requireLessOrEqual(totalDisks, Integer.MAX_VALUE, "zip64.totalDisks");
            return (int)totalDisks;
        } catch (Exception e) {
            return 1;
        }
    }

    @Override
    protected DataInputFile createDataInput() {
        return new LittleEndianDataInputFile(srcZip);
    }

    @Override
    protected EndCentralDirectoryReader getEndCentralDirectoryReader() {
        return new EndCentralDirectoryReader(customizeCharset);
    }

    @Override
    protected Zip64Reader getZip64Reader() {
        return new Zip64Reader();
    }

    @Override
    protected CentralDirectoryReader getCentralDirectoryReader(long totalEntries) {
        if (zip64.isCentralDirectoryEncrypted())
            return new EncryptedCentralDirectoryReader(totalEntries,
                                                       customizeCharset,
                                                       zip64.getExtensibleDataSector(),
                                                       passwordProvider);

        return new CentralDirectoryReader(totalEntries, customizeCharset);
    }

}
