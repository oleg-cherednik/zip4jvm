package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.ExternalFileAttributes;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Builder
public class ExternalFileAttributesView {

    private final ExternalFileAttributes externalFileAttributes;
    private final String prefix;

    public void print(PrintStream out) {
        byte[] data = externalFileAttributes.getData();
        int val = data[3] << 24 | data[2] << 16 | data[1] << 8 | data[0];
        String win = ExternalFileAttributes.build(() -> ExternalFileAttributes.WIN).readFrom(data).getDetails();
        String posix = ExternalFileAttributes.build(() -> ExternalFileAttributes.UNIX).readFrom(data).getDetails();

        out.format("%sexternal file attributes:                       0x%08X\n", prefix, val);
        out.format("%s  MS-DOS file attributes (0x%02X)                 %s\n", prefix, val & 0xFF, win);
        out.format("%s  non-MSDOS file attributes (0x%06X):       %s\n", prefix, val >> 8, posix);
    }
}
