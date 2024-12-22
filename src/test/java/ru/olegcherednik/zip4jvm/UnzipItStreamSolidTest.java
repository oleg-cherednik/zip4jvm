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

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.DataDescriptorEnum;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettings;
import ru.olegcherednik.zip4jvm.model.settings.ZipEntrySettingsProvider;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.TestData.fileNameOlegCherednik;
import static ru.olegcherednik.zip4jvm.TestData.fileOlegCherednik;
import static ru.olegcherednik.zip4jvm.Zip4jvmSuite.password;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
@Test
@SuppressWarnings("FieldNamingConvention")
public class UnzipItStreamSolidTest {

    private static final Path rootDir = Zip4jvmSuite.generateSubDirName(UnzipItStreamSolidTest.class);

    private static Path createZipFile(Path dir) throws IOException {
        Path zip = dir.resolve("src.zip");
        Map<String, ZipEntrySettings> map = new HashMap<>();

        for (Compression compression : Arrays.asList(Compression.STORE, Compression.DEFLATE)) {
            for (Encryption encryption : Arrays.asList(Encryption.OFF, Encryption.PKWARE, Encryption.AES_256)) {
                for (boolean zip64 : Arrays.asList(false, true)) {
                    for (DataDescriptorEnum dataDescriptor : Arrays.asList(DataDescriptorEnum.DISABLE,
                                                                           DataDescriptorEnum.ENABLE)) {
                        String fileName = compression.getTitle()
                                + '_' + encryption.getMethod().getTitle()
                                + (zip64 ? "_zip64" : "")
                                + (dataDescriptor == DataDescriptorEnum.ENABLE ? "_dd" : "")
                                + '_' + fileNameOlegCherednik;
                        ZipEntrySettings entrySettings = ZipEntrySettings.builder()
                                                                         .compression(compression)
                                                                         .encryption(encryption, password)
                                                                         .zip64(zip64)
                                                                         .dataDescriptor(dataDescriptor)
                                                                         .build();
                        map.put(fileName, entrySettings);
                    }
                }
            }
        }

        Function<String, ZipEntrySettings> entryNameSettings = fileName ->
                map.getOrDefault(fileName, ZipEntrySettings.DEFAULT);

        try (ZipFile.Writer writer = ZipIt.zip(zip)
                                          .entrySettings(ZipEntrySettingsProvider.of(entryNameSettings))
                                          .open()) {
            map.keySet().forEach(fileName -> writer.addWithRename(fileOlegCherednik, fileName));
        }

        return zip;
    }

    public void foo() throws IOException {
        Path dstDir = Zip4jvmSuite.subDirNameAsMethodNameWithTime(rootDir);
        Path zip = createZipFile(dstDir);

        // Path zip = Paths.get("d:/zip4jvm/span/foo.zip");
        UnzipIt.zip(zip).password(password).dstDir(dstDir).extract();
//        assertThatZipFile(zipStoreSolid).exists().root().matches(rootAssert);
    }

//    @DataProvider(name = "zips")
//    public static Object[][] zips() {
//        return new Object[][] {
//                { zipStoreSolid } };
//
//
//    }

}
