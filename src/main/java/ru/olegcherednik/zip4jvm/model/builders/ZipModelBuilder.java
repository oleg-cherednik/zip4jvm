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
package ru.olegcherednik.zip4jvm.model.builders;

import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.charset.CharsetProvider;
import ru.olegcherednik.zip4jvm.model.charset.UnmodifiedCharsetProvider;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.password.PasswordProvider;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.split.LimitSizeSplitTrigger;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireZipFileExist;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@RequiredArgsConstructor
public final class ZipModelBuilder {

    private final SrcZip srcZip;
    private final EndCentralDirectory endCentralDirectory;
    private final Zip64 zip64;
    private final CentralDirectory centralDirectory;
    private final CharsetProvider charsetProvider;

    public static ZipModel read(SrcZip srcZip) {
        return read(srcZip, UnmodifiedCharsetProvider.INSTANCE, null);
    }

    public static ZipModel read(SrcZip srcZip,
                                CharsetProvider charsetProvider,
                                PasswordProvider passwordProvider) {
        return new ZipModelReader(srcZip, charsetProvider, passwordProvider).read();
    }

    public static ZipModel build(Path zip, ZipSettings settings) {
        requireZipFileExist(zip);

        ZipModel zipModel = new ZipModel(SrcZip.of(zip));
        zipModel.setComment(settings.getComment());
        zipModel.setZip64(settings.isZip64());

        if (settings.getSplitSize() != null)
            zipModel.addSplitTrigger(new LimitSizeSplitTrigger(settings.getSplitSize()));

        return zipModel;
    }

    public ZipModel build() {
        ZipModel zipModel = new ZipModel(srcZip);
        zipModel.setZip64(zip64 != Zip64.NULL);
        zipModel.setCentralDirectoryEncrypted(zip64.isCentralDirectoryEncrypted());
        zipModel.setComment(endCentralDirectory.getComment());
        zipModel.setTotalDisks(getTotalDisks());
        zipModel.setMainDiskNo(getMainDiskNo());
        zipModel.setCentralDirectorySize(getCentralDirectorySize());
        zipModel.setCentralDirectoryRelativeOffs(getCentralDirectoryRelativeOffs(endCentralDirectory, zip64));

        if (!srcZip.isSolid())
            zipModel.addSplitTrigger(new LimitSizeSplitTrigger(srcZip.getSplitSize()));

        createAndAddEntries(zipModel);

        return zipModel;
    }

    private void createAndAddEntries(ZipModel zipModel) {
        if (centralDirectory != null)
            centralDirectory.getFileHeaders().stream()
                    .map(fileHeader -> ZipEntryBuilder.build(fileHeader,
                            zipModel.getSrcZip(),
                            charsetProvider))
                    .forEach(zipModel::addZipEntry);
    }

    private int getTotalDisks() {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getTotalDisks();
        return (int) zip64.getEndCentralDirectoryLocator().getTotalDisks();
    }

    private long getMainDiskNo() {
        return getMainDiskNo(endCentralDirectory, zip64);
    }

    public long getCentralDirectorySize() {
        return getCentralDirectorySize(endCentralDirectory, zip64);
    }

    public static int getMainDiskNo(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getMainDiskNo();
        return (int) zip64.getEndCentralDirectory().getMainDiskNo();
    }

    public static long getCentralDirectorySize(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getCentralDirectorySize();
        return zip64.getEndCentralDirectory().getCentralDirectorySize();
    }

    public static long getCentralDirectoryRelativeOffs(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getCentralDirectoryRelativeOffs();
        return zip64.getEndCentralDirectory().getCentralDirectoryRelativeOffs();
    }

    public static long getTotalEntries(EndCentralDirectory endCentralDirectory, Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getTotalEntries();
        return zip64.getEndCentralDirectory().getTotalEntries();
    }

}
