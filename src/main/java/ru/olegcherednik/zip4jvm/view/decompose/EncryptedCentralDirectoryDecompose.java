package ru.olegcherednik.zip4jvm.view.decompose;

import ru.olegcherednik.zip4jvm.model.Compression;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.EncryptedCentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.crypto.DecryptionHeaderView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
public class EncryptedCentralDirectoryDecompose extends CentralDirectoryDecompose {

    private static final String DECRYPTION_HEADER_FILE_NAME = "decryption_header";

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
    protected void centralDirectoryData(Path dir) throws IOException {
        Utils.copyByteArray(dir.resolve(CENTRAL_DIRECTORY_FILE_NAME + ".data"),
                            Optional.ofNullable(block.getDecompressedCentralDirectory()).orElse(block.getDecompressedCentralDirectory()));
    }

    private void decryptionHeader(Path dir) throws IOException {
        Utils.print(dir.resolve(DECRYPTION_HEADER_FILE_NAME + ".txt"), out -> decryptionHeaderView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(DECRYPTION_HEADER_FILE_NAME + ".data"), block.getDecryptionHeaderBlock());
    }

    private void encryptedCentralDirectory(Path dir) throws IOException {
        String fileName = (CENTRAL_DIRECTORY_FILE_NAME
                + '_' + extensibleDataSector.getEncryptionAlgorithm().getTitle()
                + '-' + extensibleDataSector.getBitLength()).toLowerCase();
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getEncryptedCentralDirectoryBlock());
    }

    private void compressedCentralDirectory(Path dir) throws IOException {
        if (block.getDecryptedCentralDirectory() != null) {
            Compression compression = Compression.parseCompressionMethod(extensibleDataSector.getCompressionMethod());
            String fileName = (CENTRAL_DIRECTORY_FILE_NAME + '_' + compression.getTitle()).toLowerCase();
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
