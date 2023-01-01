package ru.olegcherednik.zip4jvm.view.zip64;

import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.BaseView;
import ru.olegcherednik.zip4jvm.view.VersionView;

import java.io.PrintStream;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;

/**
 * @author Oleg Cherednik
 * @since 29.12.2022
 */
public class EndCentralDirectoryView extends BaseView {

    private final Zip64.EndCentralDirectory ecd;
    private final Block block;

    public EndCentralDirectoryView(Zip64.EndCentralDirectory ecd,
                                   Block block,
                                   int offs,
                                   int columnWidth,
                                   long totalDisks) {
        super(offs, columnWidth, totalDisks);
        this.ecd = requireNotNull(ecd, "EndCentralDirectoryView.ecd");
        this.block = requireNotNull(block, "EndCentralDirectoryView.block");
    }

    @Override
    public boolean print(PrintStream out) {
        printTitle(out, Zip64.EndCentralDirectory.SIGNATURE, "ZIP64 End of Central directory record", block);
        printLine(out, "number of bytes in rest of record:", String.format("%d bytes", ecd.getEndCentralDirectorySize()));
        printVersion(out);
        printLine(out, String.format("part number of this part (%04d):", ecd.getDiskNo()), ecd.getDiskNo() + 1);
        printLine(out, String.format("part number of start of central dir (%04d):", ecd.getMainDiskNo()), ecd.getMainDiskNo() + 1);
        printLine(out, "number of entries in central dir in this part:", ecd.getDiskEntries());
        printLine(out, "total number of entries in central dir:", ecd.getTotalEntries());
        printLine(out, "size of central dir:", String.format("%d bytes", ecd.getCentralDirectorySize()));
        printLine(out, "relative offset of central dir:", String.format("%1$d (0x%1$08X) bytes", ecd.getCentralDirectoryRelativeOffs()));
        return true;
    }

    private void printVersion(PrintStream out) {
        new VersionView(ecd.getVersionMadeBy(), ecd.getVersionToExtract(), offs, columnWidth).print(out);
    }

}
