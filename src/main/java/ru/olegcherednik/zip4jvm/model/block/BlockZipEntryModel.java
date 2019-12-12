package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@Getter
@RequiredArgsConstructor
public final class BlockZipEntryModel {

    private final Map<String, Data> fileNameData = new LinkedHashMap<>();

    public Set<String> getFileNames() {
        return fileNameData.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(fileNameData.keySet());
    }

    public void addLocalFileHeader(LocalFileHeader localFileHeader) {
        String fileName = localFileHeader.getFileName();
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setLocalFileHeader(localFileHeader);
    }

    public void addDataDescriptor(String fileName, DataDescriptor dataDescriptor) {
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setDataDescriptor(dataDescriptor);
    }

    public LocalFileHeader getLocalFileHeader(String fileName) {
        return fileNameData.get(fileName).getLocalFileHeader();
    }

    public DataDescriptor getDataDescriptor(String fileName) {
        return fileNameData.get(fileName).getDataDescriptor();
    }


    public void saveDataDescriptorBlock(String fileName, ByteArrayBlock block) {
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setDataDescriptorBlock(block);
    }

    public ByteArrayBlock getDataDescriptorBlock(String fileName) {
        return fileNameData.get(fileName).getDataDescriptorBlock();
    }

    public void saveEncryptionHeader(String fileName, EncryptionHeaderBlock encryptionHeaderBlock) {
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setEncryptionHeaderBlock(encryptionHeaderBlock);
    }

    public EncryptionHeaderBlock getEncryptionHeader(String fileName) {
        return fileNameData.get(fileName).getEncryptionHeaderBlock();
    }

    public void saveLocalFileHeader(String fileName, BlockZipEntryModel.LocalFileHeaderBlock localFileHeaderBlock) {
        fileNameData.computeIfAbsent(fileName, Data::new);
        fileNameData.get(fileName).setLocalFileHeaderBlock(localFileHeaderBlock);
    }

    public BlockZipEntryModel.LocalFileHeaderBlock getLocalFileHeaderBlock(String fileName) {
        return fileNameData.get(fileName).getLocalFileHeaderBlock();
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static final class Data {

        private final String fileName;

        private LocalFileHeader localFileHeader;
        private DataDescriptor dataDescriptor;

        private BlockZipEntryModel.LocalFileHeaderBlock localFileHeaderBlock;
        private EncryptionHeaderBlock encryptionHeaderBlock;
        private ByteArrayBlock dataDescriptorBlock;
    }

    @Getter
    @Setter
    public static final class LocalFileHeaderBlock {

        private final ByteArrayBlock content = new ByteArrayBlock();
        private final ExtraFieldBlock extraFieldBlock = new ExtraFieldBlock();

        private long disk;
    }

}
