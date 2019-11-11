package ru.olegcherednik.zip4jvm.view;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public class InternalFileAttributesView extends View {

    private final InternalFileAttributes internalFileAttributes;

    public static Builder builder() {
        return new Builder();
    }

    private InternalFileAttributesView(Builder builder) {
        super(builder.offs, builder.columnWidth);
        internalFileAttributes = builder.internalFileAttributes;
    }

    @Override
    public boolean print(PrintStream out) {
        byte[] data = internalFileAttributes.getData();

        printLine(out, "internal file attributes:", String.format("0x%04X", data[1] << 8 | data[0]));
        printLine(out, "  apparent file type: ", internalFileAttributes.getApparentFileType().getTitle());

        return true;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Builder {

        private InternalFileAttributes internalFileAttributes;
        private int offs;
        private int columnWidth;

        public InternalFileAttributesView build() {
            return new InternalFileAttributesView(this);
        }

        public Builder internalFileAttributes(InternalFileAttributes internalFileAttributes) {
            this.internalFileAttributes = internalFileAttributes;
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
