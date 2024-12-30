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

import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
@SuppressWarnings("all")
public class Foo {

    public static void main(String[] args) throws IOException {
        final long timeFrom = System.currentTimeMillis();

        Path dstDir = Paths.get("f:/zip4jvm/zip64/multi/out_100");
        Path zip = Paths.get("f:/zip4jvm/zip64/multi/aes_100k.zip");

        FileUtils.deleteDirectory(dstDir.toFile());

        UnzipSettings settings = UnzipSettings.builder()
                                              .asyncThreadsAuto()
                                              .build();

        UnzipIt.zip(zip).settings(settings).dstDir(dstDir)
               .extract(Arrays.asList("f/g/h"));

        final long timeTo = System.currentTimeMillis();
        long millis = timeTo - timeFrom;
        long minutes = (millis / 1000) / 60;
        int seconds = (int) ((millis / 1000) % 60);
        System.out.format("Time: %02d:%02d", minutes, seconds);
    }

}
