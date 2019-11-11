package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.utils.time.DosTimestampConverter;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class LastModifiedTimeView extends View {

    private final int lastModifiedTime;

    public static Builder builder() {
        return new Builder();
    }

    private LastModifiedTimeView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        lastModifiedTime = builder.lastModifiedTime;
    }

    @Override
    public boolean print(PrintStream out) {
        int date = lastModifiedTime >> 16;
        int time = lastModifiedTime & 0xFFFF;
        long ms = DosTimestampConverter.dosToJavaTime(lastModifiedTime);

        printLine(out, String.format("file last modified on (0x%04X 0x%04X):", date, time), String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", ms));
        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private int lastModifiedTime;
        private int offs;
        private int columnWidth;

        public LastModifiedTimeView build() {
            return new LastModifiedTimeView(this);
        }

        public Builder lastModifiedTime(int lastModifiedTime) {
            this.lastModifiedTime = lastModifiedTime;
            return this;
        }

        public Builder offs(int offs) {
            this.offs = offs;
            return this;
        }

        public Builder columnWidth(int columnWidth) {
            this.columnWidth = columnWidth;
            return this;
        }
    }
}
