package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 14.10.2019
 */
public final class CentralDirectoryView extends View {

    private final CentralDirectory centralDirectory;
    private final Block block;

    public CentralDirectoryView(CentralDirectory centralDirectory, Block block, int offs, int columnWidth) {
        super(offs, columnWidth);
        this.centralDirectory = centralDirectory;
        this.block = block;

        Objects.requireNonNull(centralDirectory, "'centralDirectory' must not be null");
        Objects.requireNonNull(block, "'block' must not be null");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.FileHeader.SIGNATURE, "Central directory", block);
        printLine(out, "total entries:", String.valueOf(centralDirectory.getFileHeaders().size()));
        return true;
    }

}
