package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.View;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

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

        requireNotNull(centralDirectory, "CentralDirectoryView.centralDirectory");
        requireNotNull(block, "CentralDirectoryView.block");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, CentralDirectory.FileHeader.SIGNATURE, "Central directory", block);
        printLine(out, "total entries:", String.valueOf(centralDirectory.getFileHeaders().size()));
        return true;
    }

}
