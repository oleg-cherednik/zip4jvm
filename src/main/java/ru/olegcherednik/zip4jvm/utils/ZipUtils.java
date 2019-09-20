package ru.olegcherednik.zip4jvm.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.exception.RealBigZip64NotSupportedException;

import java.util.Calendar;

/**
 * @author Oleg CHerednik
 * @since 20.03.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ZipUtils {

    public static int javaToDosTime(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        int year = cal.get(Calendar.YEAR);
        if (year < 1980) {
            return (1 << 21) | (1 << 16);
        }
        return (year - 1980) << 25 | (cal.get(Calendar.MONTH) + 1) << 21 |
                cal.get(Calendar.DATE) << 16 | cal.get(Calendar.HOUR_OF_DAY) << 11 | cal.get(Calendar.MINUTE) << 5 |
                cal.get(Calendar.SECOND) >> 1;
    }

    public static long dosToJavaTme(int dosTime) {
        int sec = 2 * (dosTime & 0x1f);
        int min = (dosTime >> 5) & 0x3f;
        int hrs = (dosTime >> 11) & 0x1f;
        int day = (dosTime >> 16) & 0x1f;
        int mon = ((dosTime >> 21) & 0xf) - 1;
        int year = ((dosTime >> 25) & 0x7f) + 1980;

        Calendar cal = Calendar.getInstance();
        cal.set(year, mon, day, hrs, min, sec);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime().getTime();
    }

    public static void requirePositive(long value, String type) {
        if (value < 0)
            throw new RealBigZip64NotSupportedException(value, type);
    }

    public static boolean isDirectory(String fileName) {
        return fileName != null && (fileName.endsWith("/") || fileName.endsWith("\\"));
    }

    public static boolean isRegularFile(String fileName) {
        return fileName != null && !(fileName.endsWith("/") || fileName.endsWith("\\"));
    }

    public static String normalizeFileName(String fileName) {
        return FilenameUtils.normalize(fileName, true);
    }

    public static String toString(long offs) {
        return "offs: " + offs + " (0x" + Long.toHexString(offs) + ')';
    }

    public static String getFileName(@NonNull String fileName, boolean regularFile) {
        fileName = getFileNameNoDirectoryMarker(fileName);
        return regularFile ? fileName : fileName + '/';
    }

    public static String getFileNameNoDirectoryMarker(@NonNull String fileName) {
        fileName = normalizeFileName(fileName);
        return StringUtils.removeEnd(fileName, "/");
    }

}
