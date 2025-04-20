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

import ru.olegcherednik.zip4jvm.model.charset.Charsets;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.compress.utils.IOUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * @author Oleg Cherednik
 * @since 19.10.2022
 */
@SuppressWarnings("all")
public class Foo {

    public static void main(String... args) throws IOException {
        String str = "линия_один\nлиния_два\nлиния_три";
        byte[] bytes = str.getBytes(Charsets.UTF_8);

        for (int i = 0; i < bytes.length; i++) {
            System.out.print(Integer.toHexString(bytes[i] & 0xFF).toUpperCase() + " ");
        }

        String outputZip = "f:/zip4jvm/foo/info_zip_unicode_comment_checksum.zip";

        try (FileOutputStream fos = new FileOutputStream(outputZip);
             ZipArchiveOutputStream zos = new ZipArchiveOutputStream(fos)) {

            zos.setComment("line_four\nline_five\nline_six");

            addFile(zos,
                    new FileInputStream("F:/zip4jvm/foo/src/data/Oleg Cherednik.txt"),
                    "Oleg Cherednik.txt",
                    346197863);
        }

        ZipInfo.zip(Paths.get(outputZip)).printShortInfo();
    }

    private static void addFile(ZipArchiveOutputStream zos, InputStream in, String entryName, long crc32)
            throws IOException {
        zos.setMethod(ZipMethod.STORED.getCode());
        zos.setCreateUnicodeExtraFields(ZipArchiveOutputStream.UnicodeExtraFieldPolicy.ALWAYS);

        ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
        entry.setSize(40);
        entry.setCrc(crc32);
        entry.setComment("line_one\nline_two\nline_three");
        zos.putArchiveEntry(entry);

        try (InputStream _in = in) {
            IOUtils.copy(_in, zos);
        }

        zos.closeArchiveEntry();
    }

    private static void addDirectory(ZipArchiveOutputStream zos, String directoryName)
            throws IOException {
        ZipArchiveEntry entry = new ZipArchiveEntry(directoryName);
        entry.setUnixMode(040755);
        entry.setSize(0);
        entry.setCrc(0);

        // VERY IMPORTANT: Call set directory. This signifies it is not a file
//        entry.setDirectory(true);

        zos.putArchiveEntry(entry);

        // Close the archive entry
        zos.closeArchiveEntry();
    }

}
