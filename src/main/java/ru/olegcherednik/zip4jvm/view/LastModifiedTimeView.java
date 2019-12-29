package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverter;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class LastModifiedTimeView extends BaseView {

    private final int lastModifiedTime;

    public LastModifiedTimeView(int lastModifiedTime, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.lastModifiedTime = lastModifiedTime;
    }

    @Override
    public boolean print(PrintStream out) {
        int date = lastModifiedTime >> 16;
        int time = lastModifiedTime & 0xFFFF;
        long ms = DosTimestampConverter.dosToJavaTime(lastModifiedTime);

        printLine(out, String.format("file last modified on (0x%04X 0x%04X):", date, time), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", ms));
        return true;
    }
}
