package ru.olegcherednik.zip4jvm.utils.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 25.10.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnixTimestampConverterUtils {

    public static long unixToJavaTime(long utime) {
        return utime * 1000;
    }

    public static long javaToUnixTime(long ms) {
        return ms / 1000;
    }

}
