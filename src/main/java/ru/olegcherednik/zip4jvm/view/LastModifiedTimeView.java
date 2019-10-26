package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverter;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Builder
public class LastModifiedTimeView {

    private final int lastModifiedTime;
    private final String prefix;

    public void print(PrintStream out) {
        int date = lastModifiedTime >> 16;
        int time = lastModifiedTime & 0xFFFF;
        long ms = DosTimestampConverter.dosToJavaTime(lastModifiedTime);

        out.format("%sfile last modified on (0x%04X 0x%04X):          %4$tY-%4$tm-%4$td %4$tH:%4$tM:%4$tS\n", prefix, date, time, ms);
    }
}
