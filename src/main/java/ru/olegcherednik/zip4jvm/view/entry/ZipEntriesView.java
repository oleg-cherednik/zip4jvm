package ru.olegcherednik.zip4jvm.view.entry;

import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public final class ZipEntriesView extends View {

    private final long totalEntries;

    public ZipEntriesView(long totalEntries, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.totalEntries = totalEntries;
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, LocalFileHeader.SIGNATURE, "ZIP entries");
        printLine(out, "total entries:", totalEntries);
        return true;
    }
}
