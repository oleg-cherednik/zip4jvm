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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.SignatureWasNotFoundException;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * Start reading from the end of the file.
 *
 * <pre>
 * ...
 * [zip64 end of central directory record]
 * [zip64 end of central directory locator]
 * [end of central directory record]
 * EOF
 * </pre>
 *
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseZipModelReader {

    private static final String MARKER_END_CENTRAL_DIRECTORY = "end_central_directory";

    protected final SrcZip srcZip;
    protected final Function<Charset, Charset> customizeCharset;
    protected final PasswordProvider passwordProvider;

    protected EndCentralDirectory endCentralDirectory;
    protected Zip64 zip64;
    protected boolean centralDirectoryEncrypted;
    protected CentralDirectory centralDirectory;

    public final void readCentralData() throws IOException {
        readCentralData(true);
    }

    protected final void readCentralData(boolean readCentralDirectory) throws IOException {
        try (DataInput in = createDataInput()) {
            readEndCentralDirectory(in);
            readZip64(in);

            if (readCentralDirectory)
                readCentralDirectory(in);
        }
    }

    protected final void readEndCentralDirectory(DataInput in) throws IOException {
        findCentralDirectorySignature(in);
        endCentralDirectory = getEndCentralDirectoryReader().read(in);
    }

    protected void readZip64(DataInput in) throws IOException {
        in.seek(MARKER_END_CENTRAL_DIRECTORY);
        zip64 = getZip64Reader().read(in);
        centralDirectoryEncrypted = zip64.isCentralDirectoryEncrypted();
    }

    protected final void readZip64EndCentralDirectoryLocator(DataInput in) throws IOException {
        in.seek(MARKER_END_CENTRAL_DIRECTORY);
        zip64 = getZip64Reader().findAndReadEndCentralDirectoryLocator(in);
    }

    private void readCentralDirectory(DataInput in) throws IOException {
        int mainDiskNo = ZipModelBuilder.getMainDiskNo(endCentralDirectory, zip64);
        long relativeOffs = ZipModelBuilder.getCentralDirectoryRelativeOffs(endCentralDirectory, zip64);
        long totalEntries = ZipModelBuilder.getTotalEntries(endCentralDirectory, zip64);
        in.seek(mainDiskNo, relativeOffs);
        centralDirectory = getCentralDirectoryReader(totalEntries).read(in);
    }

    protected abstract DataInput createDataInput() throws IOException;

    protected abstract EndCentralDirectoryReader getEndCentralDirectoryReader();

    protected abstract Zip64Reader getZip64Reader();

    protected abstract CentralDirectoryReader getCentralDirectoryReader(long totalEntries);

    public static void findCentralDirectorySignature(DataInput in) throws IOException {
        int commentLength = ZipModel.MAX_COMMENT_SIZE;
        long available = in.size() - EndCentralDirectory.MIN_SIZE;

        do {
            in.seek(available--);
            commentLength--;

            if (in.readDwordSignature() == EndCentralDirectory.SIGNATURE) {
                in.backward(in.dwordSignatureSize());
                in.mark(MARKER_END_CENTRAL_DIRECTORY);
                return;
            }
        } while (commentLength >= 0 && available >= 0);

        throw new SignatureWasNotFoundException(EndCentralDirectory.SIGNATURE, "EndCentralDirectory");
    }

}
