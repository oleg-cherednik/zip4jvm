package ru.olegcherednik.zip4jvm.view.entry;

import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.view.BaseView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
public final class ZipEntriesView extends BaseView {

    private final long totalEntries;

    public ZipEntriesView(long totalEntries, int offs, int columnWidth, long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.totalEntries = totalEntries;
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, LocalFileHeader.SIGNATURE, "ZIP entries");
        printLine(out, "total entries:", totalEntries);
        return true;
    }
}
