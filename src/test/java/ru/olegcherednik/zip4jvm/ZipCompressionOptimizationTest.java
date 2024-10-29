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

import ru.olegcherednik.zip4jvm.engine.InfoEngine;
import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameBentley;
import static ru.olegcherednik.zip4jvm.TestData.fileNameOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleyAssert;
import static ru.olegcherednik.zip4jvm.TestDataAssert.fileBentleySize;
import static ru.olegcherednik.zip4jvm.assertj.Zip4jvmAssertions.assertThatZipFile;

/**
 * @author Oleg Cherednik
 * @since 27.10.2024
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class ZipCompressionOptimizationTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirNameWithTime(ZipCompressionOptimizationTest.class);

    public void shouldNotCreateDataDescriptionWhenStoreCompression() throws IOException {
        Path parent = Zip4jvmSuite.subDirNameAsMethodName(rootDir);
        Path zip = parent.resolve("src.zip");
        ZipIt.zip(zip).settings(ZipSettings.of(Compression.BZIP2)).add(fileOlegCherednik);

        InfoEngine infoEngine = new InfoEngine(SrcZip.of(zip), ZipInfoSettings.builder().readEntries(true).build());
        BlockModel blockModel = infoEngine.createModel();

        ZipEntryBlock entryBlock = blockModel.getZipEntryBlock(fileNameOlegCherednik);
        assertThat(entryBlock).isNotNull();
//        assertThat(entryBlock.getDataDescriptor()).isNull();

//        LocalFileHeader localFileHeader = entryBlock.getLocalFileHeader();
//        assertThat(localFileHeader).isNotNull();
//        assertThat(localFileHeader.getCompressionMethod()).isSameAs(CompressionMethod.STORE);
//        assertThat(localFileHeader.getGeneralPurposeFlag().isDataDescriptorAvailable()).isTrue();
//        assertThat(localFileHeader.getCrc32()).isEqualTo(1903786344L);
//        assertThat(localFileHeader.getCompressedSize()).isEqualTo(fileBentleySize);
//        assertThat(localFileHeader.getUncompressedSize()).isEqualTo(fileBentleySize);

        assertThatZipFile(zip).regularFile(fileNameOlegCherednik).hasContent("Oleg Cherednik Олег Чередник");//.matches(fileBentleyAssert);
    }

}
