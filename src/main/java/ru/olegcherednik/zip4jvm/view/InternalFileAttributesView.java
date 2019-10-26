package ru.olegcherednik.zip4jvm.view;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.InternalFileAttributes;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 15.10.2019
 */
@Builder
public class InternalFileAttributesView {

    private final InternalFileAttributes internalFileAttributes;
    private final String prefix;

    public void print(PrintStream out) {
        byte[] data = internalFileAttributes.getData();
        int val = data[1] << 8 | data[0];

        out.format("%sinternal file attributes:                       0x%04X\n", prefix, val);
        out.format("%s  apparent file type:                           %s\n", prefix, internalFileAttributes.getApparentFileType().getTitle());

    }

}
