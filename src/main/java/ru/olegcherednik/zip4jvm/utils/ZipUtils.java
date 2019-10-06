package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.RealBigZip64NotSupportedException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * @author Oleg CHerednik
 * @since 20.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipUtils {

    private static final int DOSTIME_BEFORE_1980 = (1 << 21) | (1 << 16);

    /*
     * @see {@link java.util.zip.ZipUtils#javaToDosTime(long)}
     */
    @SuppressWarnings("deprecation")
    public static int javaToDosTime(long time) {
        Date date = new Date(time);
        int year = date.getYear() + 1900;

        if (year < 1980)
            return DOSTIME_BEFORE_1980;

        return (year - 1980) << 25 | (date.getMonth() + 1) << 21 | date.getDate() << 16 | date.getHours() << 11
                | date.getMinutes() << 5 | date.getSeconds() >> 1;
    }

    /*
     * @see {@link java.util.zip.ZipUtils#dosToJavaTime(long)}
     */
    @SuppressWarnings({ "deprecation", "MagicConstant" })
    public static long dosToJavaTime(int dtime) {
        return new Date(((dtime >> 25) & 0x7F) + 80, ((dtime >> 21) & 0x0F) - 1, (dtime >> 16) & 0x1F, (dtime >> 11) & 0x1F,
                (dtime >> 5) & 0x3F, (dtime << 1) & 0x3E).getTime();
    }

    public static void requirePositive(long value, String type) {
        if (value < 0)
            throw new RealBigZip64NotSupportedException(value, type);
    }

    public static <T> void requireNotNull(T obj, String name) {
        if (obj == null)
            throw new IllegalArgumentException("Parameter should not be null: " + name);
    }

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
    public static String getFileNameNoDirectoryMarker(@NonNull String fileName) {
        fileName = normalizeFileName(fileName);
        return StringUtils.removeEnd(normalizeFileName(fileName), "/");
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        try (InputStream in = input; OutputStream out = output) {
            return IOUtils.copyLarge(in, out);
        }
    }

}
