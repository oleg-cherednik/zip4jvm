package ru.olegcherednik.zip4jvm.view;

import org.apache.commons.lang3.StringUtils;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class GeneralPurposeFlagView extends BaseView {

    private final GeneralPurposeFlag generalPurposeFlag;
    private final CompressionMethod compressionMethod;

    public GeneralPurposeFlagView(GeneralPurposeFlag generalPurposeFlag, CompressionMethod compressionMethod, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.generalPurposeFlag = generalPurposeFlag;
        this.compressionMethod = compressionMethod;

        Objects.requireNonNull(generalPurposeFlag, "'generalPurposeFlag' must not be null");
        Objects.requireNonNull(compressionMethod, "'compressionMethod' must not be null");
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

}
