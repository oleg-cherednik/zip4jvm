package ru.olegcherednik.zip4jvm.utils.time;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 17.10.2019
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NtfsTimestampConverterUtils {

    public static long ntfsToJavaTime(long ntime) {
        return (ntime / 10000L) - +11644473600000L;
    }

    public static long javaToNtfsTime(long ms) {
        return (ms + 11644473600000L) * 10000L;
    }

}
