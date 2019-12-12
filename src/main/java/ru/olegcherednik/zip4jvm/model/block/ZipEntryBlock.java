package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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

    private LocalFileHeaderBlock localFileHeaderBlock;
    private EncryptionHeaderBlock encryptionHeaderBlock;
    private ByteArrayBlock dataDescriptorBlock;

    public void setLocalFileHeader(LocalFileHeader localFileHeader, LocalFileHeaderBlock block) {
        this.localFileHeader = localFileHeader;
        localFileHeaderBlock = block;
    }

    public void setEncryptionHeaderBlock(EncryptionHeaderBlock encryptionHeaderBlock) {
        this.encryptionHeaderBlock = encryptionHeaderBlock;
    }

    public void setDataDescriptor(DataDescriptor dataDescriptor, ByteArrayBlock block) {
        this.dataDescriptor = dataDescriptor;
        dataDescriptorBlock = block;
    }

    @Getter
    @Setter
    public static final class LocalFileHeaderBlock {

        private final Block content = new Block();
        private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();

        private long disk;
    }

}
