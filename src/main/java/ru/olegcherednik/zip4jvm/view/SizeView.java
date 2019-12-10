package ru.olegcherednik.zip4jvm.view;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 10.12.2019
 */
public final class SizeView extends View {

    private final String name;
    private final long size;

    public SizeView(String name, long size, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.name = name;
        this.size = size;

        Objects.requireNonNull(name, "'name' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printLine(out, name, String.format("%d %s", size, size == 1 ? "byte" : "bytes"));
        return true;
    }
}
