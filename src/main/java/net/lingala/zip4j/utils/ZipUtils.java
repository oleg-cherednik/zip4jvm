/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.utils;

import lombok.experimental.UtilityClass;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.function.Function;

/**
 * @author Oleg CHerednik
 * @since 20.03.2019
 */
@UtilityClass
public class ZipUtils {

    /**
     * Converts input time from Java to DOS format
     *
     * @param time
     * @return time in DOS format
     */
    public long javaToDosTime(long time) {

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

    /**
     * Converts time in dos format to Java format
     *
     * @param dosTime
     * @return time in java format
     */
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

    public static boolean isDirectory(String fileName) {
        return fileName != null && (fileName.endsWith("/") || fileName.endsWith("\\"));
    }

    @SuppressWarnings("FieldNamingConvention")
    public static final Function<String, String> normalizeComment = comment -> {
        if (StringUtils.isBlank(comment))
            return null;

        comment = StringUtils.trimToNull(comment);

        if (StringUtils.length(comment) > InternalZipConstants.MAX_ALLOWED_ZIP_COMMENT_LENGTH)
            throw new ZipException("comment length exceeds maximum length");

        return comment;
    };

    @SuppressWarnings("FieldNamingConvention")
    public static final Function<String, String> normalizeFileName = fileName -> FilenameUtils.normalize(fileName, true);

    public static void prepareBuffAESIVBytes(byte[] buff, int nonce, int length) {
        buff[0] = (byte)nonce;
        buff[1] = (byte)(nonce >> 8);
        buff[2] = (byte)(nonce >> 16);
        buff[3] = (byte)(nonce >> 24);
        buff[4] = 0;
        buff[5] = 0;
        buff[6] = 0;
        buff[7] = 0;
        buff[8] = 0;
        buff[9] = 0;
        buff[10] = 0;
        buff[11] = 0;
        buff[12] = 0;
        buff[13] = 0;
        buff[14] = 0;
        buff[15] = 0;
    }

}
