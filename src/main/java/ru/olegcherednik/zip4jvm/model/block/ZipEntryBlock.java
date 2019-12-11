package ru.olegcherednik.zip4jvm.model.block;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Oleg Cherednik
 * @since 01.12.2019
 */
@Getter
@Setter
public class ZipEntryBlock {

    private final Map<String, LocalFileHeaderBlock> localFileHeaders = new LinkedHashMap<>();
    private final Map<String, EncryptionHeaderBlock> encryptionHeaders = new LinkedHashMap<>();
    private final Map<String, ByteArrayBlock> dataDescriptors = new LinkedHashMap<>();

    @Setter(AccessLevel.NONE)
    private LocalFileHeaderBlock localFileHeader;

    public void addLocalFileHeader() {
        localFileHeader = new LocalFileHeaderBlock();
    }

    public void saveLocalFileHeader(String fileName) {
        localFileHeaders.put(fileName, localFileHeader);
        localFileHeader = null;
    }

    public void saveEncryptionHeader(String fileName, EncryptionHeaderBlock encryptionHeaderBlock) {
        encryptionHeaders.put(fileName, encryptionHeaderBlock);
    }

    public void saveDataDescriptor(String fileName, ByteArrayBlock block) {
        dataDescriptors.put(fileName, block);
    }

    public LocalFileHeaderBlock getLocalFileHeader(String fileName) {
        return localFileHeaders.get(fileName);
    }

    public EncryptionHeaderBlock getEncryptionHeader(String fileName) {
        return encryptionHeaders.get(fileName);
    }

    public ByteArrayBlock getDataDescriptor(String fileName) {
        return dataDescriptors.get(fileName);
    }

    @Getter
    @Setter
    public static final class LocalFileHeaderBlock {

        private final ByteArrayBlock content = new ByteArrayBlock();
        private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();

        private long disk;
    }

}
