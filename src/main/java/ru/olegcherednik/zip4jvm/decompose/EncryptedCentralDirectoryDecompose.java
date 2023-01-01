package ru.olegcherednik.zip4jvm.decompose;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.EncryptedCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.crypto.DecryptionHeaderView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
public final class EncryptedCentralDirectoryDecompose extends CentralDirectoryDecompose {

    private static final String DECRYPTION_HEADER = "decryption_header";

    private final EncryptedCentralDirectoryBlock block;

    public EncryptedCentralDirectoryDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
        block = (EncryptedCentralDirectoryBlock)blockModel.getCentralDirectoryBlock();
    }

    @Override
    public Path decompose(Path dir) throws IOException {
        dir = super.decompose(dir);
        decryptionHeader(dir);
        encryptedCentralDirectory(dir);
        compressedCentralDirectory(dir);
        return dir;
    }

    @Override
    protected void centralDirectory(Path dir) throws IOException {
        Utils.print(dir.resolve(CENTRAL_DIRECTORY + ".txt"), out -> centralDirectoryView().print(out));
        Utils.copyByteArray(dir.resolve(CENTRAL_DIRECTORY + ".data"),
                            Optional.ofNullable(block.getDecompressedCentralDirectory())
                                    .orElse(block.getDecompressedCentralDirectory()));
    }

    @Override
    protected CentralDirectoryView centralDirectoryView() {
        return new EncryptedCentralDirectoryView(centralDirectory,
                                                 extensibleDataSector,
                                                 block.getEncryptedCentralDirectoryBlock(),
                                                 settings.getOffs(),
                                                 settings.getColumnWidth(),
                                                 zipModel.getTotalDisks());
    }

    private void decryptionHeader(Path dir) throws IOException {
        Utils.print(dir.resolve(DECRYPTION_HEADER + ".txt"), out -> decryptionHeaderView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(DECRYPTION_HEADER + ".data"), block.getDecryptionHeaderBlock());
    }

    private void encryptedCentralDirectory(Path dir) throws IOException {
        String fileName = CENTRAL_DIRECTORY
                + '_' + extensibleDataSector.getEncryptionAlgorithm().getTitle().toLowerCase();
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEncryptedCentralDirectoryBlock());
    }

    private void compressedCentralDirectory(Path dir) throws IOException {
        if (block.getDecryptedCentralDirectory() != null) {
            Compression compression = Compression.parseCompressionMethod(extensibleDataSector.getCompressionMethod());
            String fileName = (CENTRAL_DIRECTORY + '_' + compression.getTitle()).toLowerCase();
            Utils.copyByteArray(dir.resolve(fileName + ".data"), block.getDecryptedCentralDirectory());
        }
    }

    private DecryptionHeaderView decryptionHeaderView() {
        return new DecryptionHeaderView(centralDirectory.getDecryptionHeader(),
                                        block.getDecryptionHeaderBlock(),
                                        null,
                                        settings.getOffs(),
                                        settings.getColumnWidth(),
                                        zipModel.getTotalDisks());
    }
}
