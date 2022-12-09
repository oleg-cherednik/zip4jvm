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

import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.in.data.ZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockCentralDirectoryReader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 06.03.2019
 */
public final class ZipModelReader extends BaseZipModelReader {

    public ZipModelReader(SrcZip srcZip) {
        this(srcZip, Charsets.UNMODIFIED);
    }

    public ZipModelReader(SrcZip srcZip, Function<Charset, Charset> customizeCharset) {
        super(srcZip, customizeCharset);
    }

    public ZipModel read() throws IOException {
        readCentralData();
        return new ZipModelBuilder(srcZip, endCentralDirectory, zip64, centralDirectory, customizeCharset).build();
    }

    public static int getTotalDisks(SrcZip srcZip) {
        ZipModelReader reader = new ZipModelReader(srcZip);

        try (DataInput in = reader.createDataInput()) {
            reader.readEndCentralDirectory(in);
            reader.readZip64EndCentralDirectoryLocator(in);

            if (reader.zip64 == Zip64.NULL)
                return reader.endCentralDirectory.getTotalDisks() + 1;
            return (int)reader.zip64.getEndCentralDirectoryLocator().getTotalDisks();
        } catch(Exception e) {
            return 0;
        }
    }

    @Override
    protected DataInput createDataInput() throws IOException {
        return new ZipInputStream(srcZip);
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
        Zip64.ExtensibleDataSector extensibleDataSector = null;

        if (zip64 != Zip64.NULL)
            extensibleDataSector = zip64.getEndCentralDirectory().getExtensibleDataSector();

        return extensibleDataSector == null ? new CentralDirectoryReader(totalEntries, customizeCharset)
                                            : new SecureCentralDirectoryReader(totalEntries, customizeCharset, extensibleDataSector);
    }

}
