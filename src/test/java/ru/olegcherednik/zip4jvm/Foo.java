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

import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
@SuppressWarnings("all")
public class Foo {

    public static void main(String[] args) throws IOException {
        final long timeFrom = System.currentTimeMillis();
        int[][] token = new int[3][3];

//        Path zip = Paths.get("d:/zip4jvm/zip64/split/ferdinand.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/split/ducati.zip");

//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199-ecd.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/bikes.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199-dcl.zip");
//        Path zip = Paths.get("d:/zip4jvm/aaa/app.apk");
//        Path zip = Paths.get("d:/zip4jvm/aaa/android.apk");

//        Path zip = Paths.get("d:/zip4jvm/aaa/ducati-panigale-1199.zip");
//        Path zip = Paths.get("d:/zip4jvm/zip64/bzip2-aes256-strong.zip");

//        Path zip = Paths.get("d:/zip4jvm/zip64/bzip2-aes256-strong.zip");
//        Path zip = Paths.get("d:/Programming/GitHub/zip4jvm/src/test/resources/secure-zip/strong/store_solid_aes256_strong_ecd.zip");

        //Path zip = Paths.get("d:/zip4jvm/zip64/src.zip");
//        Path zip = Paths.get("d:/zip4jvm/scd/aes256bit.zip");
//        Path zip = Paths.get("d:/zip4jvm/scd/P1AA4B3C.zip");
        Path zip = Paths.get("d:/zip4jvm/scd/onetwo.zip");
//        Path zip = Paths.get("D:/Programming/GitHub/zip4jvm/src/test/resources/symlink/win/unique-symlink-target.zip");
        Path dstDir = Paths.get("d:/zip4jvm/scd/xxx");

//        ZipIt.zip(zip).settings(settings).add(dirSrcData);


//        for (Path zip : Arrays.asList(zip1, zip2)) {
//        System.out.println(zip);
//        UnzipIt.zip(zip).dstDir(dstDir)
//               .settings(UnzipSettings.builder()
//                                      .password(password)
//                                      .build())
//               .extract();
//        ZipInfo.zip(zip).password("1".toCharArray()).printShortInfo();
        ZipInfo.zip(zip)
               .settings(ZipInfoSettings.builder()
                                        .copyPayload(true)
                                        .readEntries(true)
                                        .build())
               .password("1".toCharArray())
               .decompose(Paths.get(dstDir.toString(), zip.getFileName().toString()));

        final long timeTo = System.currentTimeMillis();
        System.out.format("Time: %d sec", TimeUnit.MILLISECONDS.toSeconds(timeTo - timeFrom));
    }
}
