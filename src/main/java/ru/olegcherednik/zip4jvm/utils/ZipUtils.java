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
package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.utils.function.SupplierWithException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * @author Oleg Cherednik
 * @since 20.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipUtils {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");

    public static boolean isDirectory(String fileName) {
        return fileName.endsWith("/") || fileName.endsWith("\\");
    }

    public static boolean isRegularFile(String fileName) {
        return !isDirectory(fileName);
    }

    public static String normalizeFileName(String fileName) {
        return StringUtils.removeStart(FilenameUtils.normalize(fileName, true), "/");
    }

    public static String toString(long offs) {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public static String getFileName(String fileName, boolean directory) {
        fileName = getFileNameNoDirectoryMarker(fileName);
        return directory ? fileName + '/' : fileName;
    }

    public static String getFileName(ZipFile.Entry entry) {
        return getFileName(entry.getName(), entry.isDirectory());
    }

    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public static String getFileNameNoDirectoryMarker(String fileName) {
        fileName = normalizeFileName(fileName);
        return StringUtils.removeEnd(normalizeFileName(fileName), "/");
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        try (InputStream in = input; OutputStream out = output) {
            return IOUtils.copyLarge(in, out);
        }
    }

    public static String utcDateTime(long time) {
        return DF.format(Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC));
    }

    public static <T> T readQuietly(SupplierWithException<T> supplier) {
        try {
            return supplier.get();
        } catch (Zip4jvmException e) {
            throw e;
        } catch (Exception e) {
            throw new Zip4jvmException(e);
        }
    }

}
