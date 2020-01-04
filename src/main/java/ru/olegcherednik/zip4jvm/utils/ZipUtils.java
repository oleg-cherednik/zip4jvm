package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.ZipFile;

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
    public static String getFileName(String fileName, boolean regularFile) {
        fileName = getFileNameNoDirectoryMarker(fileName);
        return regularFile ? fileName : fileName + '/';
    }

    public static String getFileName(ZipFile.Entry entry) {
        return getFileName(entry.getFileName(), entry.isRegularFile());
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

}
