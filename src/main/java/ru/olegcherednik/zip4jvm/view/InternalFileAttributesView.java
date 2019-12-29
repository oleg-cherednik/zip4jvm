package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class InternalFileAttributesView extends BaseView {

    private final InternalFileAttributes internalFileAttributes;

    public InternalFileAttributesView(InternalFileAttributes internalFileAttributes, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.internalFileAttributes = internalFileAttributes;

        Objects.requireNonNull(internalFileAttributes, "'internalFileAttributes' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        byte[] data = internalFileAttributes.getData();

        printLine(out, "internal file attributes:", String.format("0x%04X", data[1] << 8 | data[0]));
        printLine(out, "  apparent file type: ", internalFileAttributes.getApparentFileType().getTitle());

        return true;
    }

}
