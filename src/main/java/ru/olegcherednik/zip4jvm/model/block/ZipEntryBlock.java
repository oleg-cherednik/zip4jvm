package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;

/**
 * @author Oleg Cherednik
 * @since 12.12.2019
 */
@Getter
@RequiredArgsConstructor
public class ZipEntryBlock {

    private final String fileName;

    private LocalFileHeader localFileHeader;
    private DataDescriptor dataDescriptor;
    private DecryptionHeader decryptionHeader;

    private LocalFileHeaderBlock localFileHeaderBlock;
    @Setter
    private EncryptionHeaderBlock encryptionHeaderBlock;
    private Block dataDescriptorBlock;

    public void setLocalFileHeader(LocalFileHeader localFileHeader, LocalFileHeaderBlock block) {
        this.localFileHeader = localFileHeader;
        localFileHeaderBlock = block;
    }

    public void setDecryptionHeader(DecryptionHeader decryptionHeader, EncryptionHeaderBlock encryptionHeaderBlock) {
        this.decryptionHeader = decryptionHeader;
        this.encryptionHeaderBlock = encryptionHeaderBlock;
    }

    public void setDataDescriptor(DataDescriptor dataDescriptor, Block block) {
        this.dataDescriptor = dataDescriptor;
        dataDescriptorBlock = block;
    }

    @Override
    public String toString() {
        return fileName;
    }

    @Getter
    @Setter
    public static final class LocalFileHeaderBlock {

        private final Block content = new Block();
        private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();
    }

}
