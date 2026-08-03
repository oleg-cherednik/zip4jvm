/*
 * Copyright 2019 Oleg Cherednik (oleg.cherednik@gmail.com)
 *
 * Licensed under The Apache Software License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

/**
 * @author Oleg Cherednik
 * @since 20.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipUtils {

    private static final DateTimeFormatter DF = DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss");
    private static final Pattern PATH_SEPARATOR = Pattern.compile("\\\\+|/+");

    public static boolean isDirectory(String entryName) {
        return entryName.endsWith("/") || entryName.endsWith("\\");
    }

    public static boolean isRegularFile(String entryName) {
        return !isDirectory(entryName);
    }

    public static String normalizeFileName(String entryName) {
        entryName = PATH_SEPARATOR.matcher(entryName).replaceAll("/");
        return StringUtils.removeStart(entryName, "/");
    }

    public static String toString(long offs) {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }

    public static String getFileName(String fileName, boolean directory) {
        fileName = getFileNameNoDirectoryMarker(fileName);
        return directory ? fileName + '/' : fileName;
    }

    public static String getFileName(ZipFile.Entry entry) {
        return getFileName(entry.getName(), entry.isDir());
    }

    public static String getFileNameNoDirectoryMarker(String fileName) {
        // TODO here also multiple '/' or '\\' should be removed
        fileName = StringUtils.removeEnd(fileName, "/");
        fileName = StringUtils.removeEnd(fileName, "\\");
        return fileName;
    }

    public static long copyLarge(InputStream input, OutputStream output) {
        try (InputStream in = input;
             OutputStream out = output) {
            return IOUtils.copyLarge(in, out);
        } catch (IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    public static String utcDateTime(long time) {
        return DF.format(Instant.ofEpochMilli(time).atZone(ZoneOffset.UTC));
    }

}
