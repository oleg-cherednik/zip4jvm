package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import java.io.PrintStream;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class ExternalFileAttributesView extends View {

    private final ExternalFileAttributes externalFileAttributes;

    public static Builder builder() {
        return new Builder();
    }

    private ExternalFileAttributesView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        externalFileAttributes = builder.externalFileAttributes;
    }

    @Override
    public void print(PrintStream out) {
        byte[] data = externalFileAttributes.getData();
        int val = data[3] << 24 | data[2] << 16 | data[1] << 8 | data[0];
        String win = ExternalFileAttributes.build(() -> ExternalFileAttributes.WIN).readFrom(data).getDetails();
        String posix = ExternalFileAttributes.build(() -> ExternalFileAttributes.UNIX).readFrom(data).getDetails();

        printLine(out, "external file attributes:", String.format("0x%08X", val));
        printLine(out, String.format("  MS-DOS file attributes (0x%02X):", val & 0xFF), win);
        printLine(out, String.format("  non-MSDOS file attributes (0x%06X):", val >> 8), posix);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private ExternalFileAttributes externalFileAttributes = ExternalFileAttributes.NULL;
        private int offs;
        private int columnWidth;

        public ExternalFileAttributesView build() {
            return new ExternalFileAttributesView(this);
        }

        public Builder externalFileAttributes(ExternalFileAttributes externalFileAttributes) {
            this.externalFileAttributes = Optional.ofNullable(externalFileAttributes).orElse(ExternalFileAttributes.NULL);
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
