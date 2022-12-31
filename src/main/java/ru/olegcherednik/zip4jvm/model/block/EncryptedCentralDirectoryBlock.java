package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.io.in.data.DataInputLocation;
import ru.olegcherednik.zip4jvm.model.block.crypto.DecryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.utils.function.LocalSupplier;

/**
 * @author Oleg Cherednik
 * @since 25.12.2022
 */
@Getter
public class EncryptedCentralDirectoryBlock extends BaseCentralDirectoryBlock {

    private final CentralDirectoryBlock centralDirectoryBlock;
    private final DecryptionHeaderBlock decryptionHeaderBlock = new DecryptionHeaderBlock();
    private final Block encryptedCentralDirectoryBlock = new Block();
    @Setter
    private byte[] decryptedCentralDirectory;
    @Setter
    private byte[] decompressedCentralDirectory;

    public EncryptedCentralDirectoryBlock(CentralDirectoryBlock centralDirectoryBlock) {
        this.centralDirectoryBlock = centralDirectoryBlock;
    }

    @Override
    public <T> T calcSize(DataInputLocation dataInputLocation, LocalSupplier<T> task) {
        return super.calcSize(dataInputLocation, task);
    }

    @Deprecated
    public void calcSize(DataInputLocation in) {
        super.calcSize(in);
    }

    @Override
    public void addFileHeader(String fileName, CentralDirectoryBlock.FileHeaderBlock block) {
        centralDirectoryBlock.addFileHeader(fileName, block);
    }

    @Override
    public void setDigitalSignature(Block block) {
        centralDirectoryBlock.setDigitalSignature(block);
    }

    @Override
    public Block getDigitalSignature() {
        return centralDirectoryBlock.getDigitalSignature();
    }

    @Override
    public CentralDirectoryBlock.FileHeaderBlock getFileHeader(String fileName) {
        return centralDirectoryBlock.getFileHeader(fileName);
    }

}
