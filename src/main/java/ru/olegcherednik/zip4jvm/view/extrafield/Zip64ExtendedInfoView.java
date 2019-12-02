package ru.olegcherednik.zip4jvm.view.extrafield;

import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.Zip64;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 26.10.2019
 */
final class Zip64ExtendedInfoView extends ExtraFieldRecordView<Zip64.ExtendedInfo> {

    public static Builder builder() {
        return new Builder();
    }

    private Zip64ExtendedInfoView(Builder builder) {
        super(builder);
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        if (record.getUncompressedSize() != ExtraField.NO_DATA)
            printLine(out, "  original compressed size:", String.format("%d bytes", record.getUncompressedSize()));
        if (record.getCompressedSize() != ExtraField.NO_DATA)
            printLine(out, "  original uncompressed size:", String.format("%d bytes", record.getCompressedSize()));
        if (record.getLocalFileHeaderOffs() != ExtraField.NO_DATA)
            printLine(out, "  original relative offset of local header:", String.format("%1$d (0x%1$08X) bytes", record.getLocalFileHeaderOffs()));
        if (record.getDisk() != ExtraField.NO_DATA)
            printLine(out, String.format("  original part number of this part (%04X):", record.getDisk()), record.getDisk());

        return true;
    }

    public static final class Builder extends BaseBuilder<Builder, Zip64.ExtendedInfo> {

        public Zip64ExtendedInfoView build() {
            check();
            return new Zip64ExtendedInfoView(this);
        }

    }
}
