package ru.olegcherednik.zip4jvm.view;

import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
public final class ExternalFileAttributesView extends BaseView {

    private final ExternalFileAttributes externalFileAttributes;

    public ExternalFileAttributesView(ExternalFileAttributes externalFileAttributes, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.externalFileAttributes = externalFileAttributes;

        requireNotNull(externalFileAttributes, "ExternalFileAttributesView.externalFileAttributes");
    }

    @Override
    public boolean print(PrintStream out) {
        byte[] data = externalFileAttributes.getData();
        int val = data[3] << 24 | data[2] << 16 | data[1] << 8 | data[0];
        String win = ExternalFileAttributes.build(() -> ExternalFileAttributes.WIN).readFrom(data).getDetails();
        String posix = ExternalFileAttributes.build(() -> ExternalFileAttributes.UNIX).readFrom(data).getDetails();

        printLine(out, "external file attributes:", String.format("0x%08X", val));
        printLine(out, String.format("  WINDOWS (0x%02X):", val & 0xFF), win);
        printLine(out, String.format("  POSIX (0x%06X):", val >> 8), posix);

        return true;
    }
}
