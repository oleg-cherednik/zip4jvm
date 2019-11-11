package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.StringUtils;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class GeneralPurposeFlagView extends View {

    private final GeneralPurposeFlag generalPurposeFlag;
    private final CompressionMethod compressionMethod;

    public static Builder builder() {
        return new Builder();
    }

    private GeneralPurposeFlagView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        generalPurposeFlag = builder.generalPurposeFlag;
        compressionMethod = builder.compressionMethod;
    }

    @Override
    public boolean print(PrintStream out) {
        int val = generalPurposeFlag.getAsInt(compressionMethod);

        printLine(out, String.format("general purpose bit flag (0x%04X) (bit 15..0):", val),
                String.format("%s.%s %s.%s",
                        StringUtils.leftPad(Integer.toBinaryString(val >> 12 & 0xF), 4, '0'),
                        StringUtils.leftPad(Integer.toBinaryString(val >> 8 & 0xF), 4, '0'),
                        StringUtils.leftPad(Integer.toBinaryString(val >> 4 & 0xF), 4, '0'),
                        StringUtils.leftPad(Integer.toBinaryString(val & 0xF), 4, '0')));
        printLine(out, "  file security status  (bit 0):", generalPurposeFlag.isEncrypted() ? "encrypted" : "not encrypted");
        printLine(out, "  data descriptor       (bit 3):", generalPurposeFlag.isDataDescriptorAvailable() ? "yes" : "no");
        printLine(out, "  strong encryption     (bit 6):", generalPurposeFlag.isStrongEncryption() ? "yes" : "no");
        printLine(out, "  UTF-8 names          (bit 11):", generalPurposeFlag.isUtf8() ? "yes" : "no");

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private GeneralPurposeFlag generalPurposeFlag;
        private CompressionMethod compressionMethod;
        private int offs;
        private int columnWidth;

        public GeneralPurposeFlagView build() {
            return new GeneralPurposeFlagView(this);
        }

        public Builder generalPurposeFlag(GeneralPurposeFlag generalPurposeFlag) {
            this.generalPurposeFlag = generalPurposeFlag;
            return this;
        }

        public Builder compressionMethod(CompressionMethod compressionMethod) {
            this.compressionMethod = compressionMethod;
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
