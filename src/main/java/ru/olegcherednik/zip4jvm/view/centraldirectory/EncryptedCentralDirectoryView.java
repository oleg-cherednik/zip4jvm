package ru.olegcherednik.zip4jvm.view.centraldirectory;

import ru.olegcherednik.zip4jvm.crypto.strong.EncryptionAlgorithm;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.CompressionMethod;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.view.CompressionMethodView;

import java.io.PrintStream;

/**
 * @author Oleg Cherednik
 * @since 01.01.2023
 */
public final class EncryptedCentralDirectoryView extends CentralDirectoryView {

    private final Zip64.ExtensibleDataSector extensibleDataSector;

    public EncryptedCentralDirectoryView(CentralDirectory centralDirectory,
                                         Zip64.ExtensibleDataSector extensibleDataSector,
                                         Block block,
                                         int offs,
                                         int columnWidth,
                                         long totalDisks) {
        super(centralDirectory, block, offs, columnWidth, totalDisks);
        this.extensibleDataSector = extensibleDataSector;
    }

    @Override
    public boolean print(PrintStream out) {
        super.print(out);

        if (extensibleDataSector != null) {
            new CompressionMethodView(extensibleDataSector.getCompressionMethod(), offs, columnWidth).print(out);
            printEncryptionAlgorithm(out);
        }

        return true;
    }

    private void printEncryptionAlgorithm(PrintStream out) {
        int code = extensibleDataSector.getEncryptionAlgorithmCode();
        EncryptionAlgorithm encryptionAlgorithm = extensibleDataSector.getEncryptionAlgorithm();
        printLine(out,
                  String.format("encryption algorithm (0x%04X):", code),
                  String.format("%s", encryptionAlgorithm.getTitle()));
    }

}
