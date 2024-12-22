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
package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.engine.info.InfoEngine;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleySize;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 27.10.2024
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipCompressionOptimizationTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipCompressionOptimizationTest.class);

    @Test(dataProvider = "zip64")
    public void shouldNotCreateDataDescriptionWhenStoreNoEncryptionCompression(boolean zip64)
            throws IOException {
        Path parent = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = parent.resolve(UUID.randomUUID() + ".zip");

        ZipIt.zip(zip)
             .settings(ZipSettings.builder()
                                  .zip64(zip64)
                                  .entrySettings(
                                          ZipEntrySettings.builder()
                                                          .compression(Compression.STORE)
                                                          .encryption(Encryption.OFF, password)
                                                          .dataDescriptor(DataDescriptorEnum.AUTO)
                                                          .build())
                                  .build())
             .add(fileBentley);

        InfoEngine infoEngine = new InfoEngine(SrcZip.of(zip), ZipInfoSettings.builder().readEntries(true).build());
        BlockModel blockModel = infoEngine.createModel();

        ZipEntryBlock entryBlock = blockModel.getZipEntryBlock(fileNameBentley);
        assertThat(entryBlock).isNotNull();
        assertThat(entryBlock.getDataDescriptor()).isNull();

        LocalFileHeader localFileHeader = entryBlock.getLocalFileHeader();
        assertThat(localFileHeader).isNotNull();
        assertThat(localFileHeader.getCompressionMethod()).isSameAs(CompressionMethod.STORE);
        assertThat(localFileHeader.getGeneralPurposeFlag().isDataDescriptorAvailable()).isFalse();
        assertThat(localFileHeader.getCrc32()).isNotZero();
        assertThat(localFileHeader.getCompressedSize()).isNotZero();
        assertThat(localFileHeader.getUncompressedSize()).isEqualTo(fileBentleySize);
        assertThatZipFile(zip, password).regularFile(fileNameBentley).matches(fileBentleyAssert);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    @DataProvider(name = "zip64")
    public static Object[][] zip64() {
        return new Object[][] {
                { false },
                { true } };
    }

}
