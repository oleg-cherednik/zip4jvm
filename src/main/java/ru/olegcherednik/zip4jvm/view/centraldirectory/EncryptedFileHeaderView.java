package ru.olegcherednik.zip4jvm.view.centraldirectory;

import lombok.Builder;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;

import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * @author Oleg Cherednik
 * @since 03.01.2023
 */
public class EncryptedFileHeaderView extends FileHeaderView {

    public EncryptedFileHeaderView(CentralDirectory.FileHeader fileHeader,
                                   CentralDirectoryBlock.FileHeaderBlock block,
                                   long pos,
                                   Charset charset,
                                   int offs,
                                   int columnWidth,
                                   long totalDisks) {
        super(fileHeader, block, pos, charset, offs, columnWidth, totalDisks);
    }

    @Override
    protected void printLocationTitle(PrintStream out, Block block) {
        printLine(out, "- location in central directory:", String.format("%1$d (0x%1$08X) bytes", block.getRelativeOffs()));
    }

}
