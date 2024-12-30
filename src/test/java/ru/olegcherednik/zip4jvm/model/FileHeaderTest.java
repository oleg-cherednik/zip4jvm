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
package ru.olegcherednik.zip4jvm.model;

import ru.olegcherednik.zip4jvm.Zip4jvmSuite;
import ru.olegcherednik.zip4jvm.model.extrafield.PkwareExtraField;

import org.apache.commons.lang3.ArrayUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.olegcherednik.zip4jvm.TestData.fileBentley;

/**
 * @author Oleg Cherednik
 * @since 15.09.2019
 */
@Test
@SuppressWarnings("VariableDeclarationUsageDistance")
public class FileHeaderTest {

    private static final Path ROOT_DIR = Zip4jvmSuite.generateSubDirNameWithTime(FileHeaderTest.class);
    private static final String ZIP4JVM = "zip4jvm";

    @BeforeClass
    public static void createDir() throws IOException {
        Files.createDirectories(ROOT_DIR);
    }

    @AfterClass(enabled = Zip4jvmSuite.clear)
    public static void removeDir() throws IOException {
        Zip4jvmSuite.removeDir(ROOT_DIR);
    }

    public void shouldUseSettersGettersCorrectly() throws IOException {
        GeneralPurposeFlag generalPurposeFlag = new GeneralPurposeFlag();
        InternalFileAttributes internalFileAttributes = new InternalFileAttributes(new byte[] { 1, 2 });
        ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.regularFile(fileBentley);
        PkwareExtraField extraField = PkwareExtraField.builder().addRecord(Zip64.ExtendedInfo.builder()
                                                                                             .uncompressedSize(4)
                                                                                             .build()).build();

        //    TODO commented tests
        //        assertThat(internalFileAttributes).isNotSameAs(InternalFileAttributes.NULL);
        //        assertThat(externalFileAttributes).isNotSameAs(ExternalFileAttributes.NULL);
        assertThat(extraField).isNotSameAs(PkwareExtraField.NULL);

        Version versionMadeBy = Version.of(Version.FileSystem.MS_DOS_OS2_NT_FAT, 20);
        Version versionToExtract = Version.of(Version.FileSystem.Z_SYSTEM, 15);

        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        assertThat(fileHeader.getVersionMadeBy()).isSameAs(Version.NULL);
        assertThat(fileHeader.getVersionToExtract()).isSameAs(Version.NULL);

        fileHeader.setVersionMadeBy(versionMadeBy);
        fileHeader.setVersionToExtract(versionToExtract);
        fileHeader.setGeneralPurposeFlag(generalPurposeFlag);
        fileHeader.setCompressionMethod(CompressionMethod.AES);
        fileHeader.setLastModifiedTime(3);
        fileHeader.setCrc32(4);
        fileHeader.setCompressedSize(5);
        fileHeader.setUncompressedSize(6);
        fileHeader.setCommentLength(7);
        fileHeader.setDiskNo(8);
        fileHeader.setInternalFileAttributes(internalFileAttributes);
        fileHeader.setExternalFileAttributes(externalFileAttributes);
        fileHeader.setLocalFileHeaderRelativeOffs(9);
        fileHeader.setFileName("fileName");
        fileHeader.setExtraField(extraField);

        assertThat(fileHeader.getVersionMadeBy()).isSameAs(versionMadeBy);
        assertThat(fileHeader.getVersionToExtract()).isSameAs(versionToExtract);
        assertThat(fileHeader.getGeneralPurposeFlag()).isSameAs(generalPurposeFlag);
        assertThat(fileHeader.getCompressionMethod()).isSameAs(CompressionMethod.AES);
        assertThat(fileHeader.getLastModifiedTime()).isEqualTo(3);
        assertThat(fileHeader.getCrc32()).isEqualTo(4);
        assertThat(fileHeader.getCompressedSize()).isEqualTo(5);
        assertThat(fileHeader.getUncompressedSize()).isEqualTo(6);
        assertThat(fileHeader.getCommentLength()).isEqualTo(7);
        assertThat(fileHeader.getDiskNo()).isEqualTo(8);
        assertThat(fileHeader.getInternalFileAttributes().getData()).isEqualTo(internalFileAttributes.getData());
        assertThat(fileHeader.getExternalFileAttributes()).isSameAs(externalFileAttributes);
        assertThat(fileHeader.getLocalFileHeaderRelativeOffs()).isEqualTo(9);
        assertThat(fileHeader.getExtraField().getExtendedInfo()).isNotNull();
        assertThat(fileHeader.getFileName()).isEqualTo("fileName");
    }

    public void shouldRetrieveNotNullFileName() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getFileName()).isNull();
        assertThat(fileHeader.getFileName(Charsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        fileHeader.setFileName(ZIP4JVM);
        assertThat(fileHeader.getFileName(Charsets.UTF_8)).isEqualTo(ZIP4JVM.getBytes(Charsets.UTF_8));
    }

    public void shouldRetrieveNotNullComment() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getComment()).isNull();
        assertThat(fileHeader.getComment(Charsets.UTF_8)).isSameAs(ArrayUtils.EMPTY_BYTE_ARRAY);

        fileHeader.setComment(ZIP4JVM);
        assertThat(fileHeader.getComment(Charsets.UTF_8)).isEqualTo(ZIP4JVM.getBytes(Charsets.UTF_8));
    }

    public void shouldRetrieveFileNameWhenToString() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.toString()).isNull();

        fileHeader.setFileName(ZIP4JVM);
        assertThat(fileHeader.toString()).isEqualTo(ZIP4JVM);
    }

    public void shouldRetrieveIsZip64TrueWhenZip64ExtendedInfoIsNotNull() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();
        assertThat(fileHeader.getExtraField().getExtendedInfo()).isSameAs(Zip64.ExtendedInfo.NULL);
        assertThat(fileHeader.isZip64()).isFalse();

        Zip64.ExtendedInfo extendedInfo = Zip64.ExtendedInfo.builder().uncompressedSize(1).compressedSize(2)
                                                            .localFileHeaderRelativeOffs(3)
                                                            .diskNo(4).build();

        fileHeader.setExtraField(PkwareExtraField.builder().addRecord(extendedInfo).build());
        assertThat(fileHeader.isZip64()).isTrue();
    }

    public void shouldWriteZip64WhenLocalFileHeaderOffsIsOverLimit() {
        CentralDirectory.FileHeader fileHeader = new CentralDirectory.FileHeader();

        fileHeader.setLocalFileHeaderRelativeOffs(Zip64.LIMIT_DWORD);
        assertThat(fileHeader.isWriteZip64OffsetLocalHeader()).isFalse();

        fileHeader.setLocalFileHeaderRelativeOffs(Zip64.LIMIT_DWORD + 1);
        assertThat(fileHeader.isWriteZip64OffsetLocalHeader()).isTrue();
    }

}
