package ru.olegcherednik.zip4jvm.model.block;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

    private final Map<String, ZipEntryBlock.Data> fileNameData = new LinkedHashMap<>();

    public Set<String> getFileNames() {
        return fileNameData.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(fileNameData.keySet());
    }

    public void addLocalFileHeader(LocalFileHeader localFileHeader, ZipEntryBlock.LocalFileHeaderBlock block) {
        String fileName = localFileHeader.getFileName();
        fileNameData.computeIfAbsent(fileName, ZipEntryBlock.Data::new);
        fileNameData.get(fileName).setLocalFileHeader(localFileHeader, block);
    }

    public void addDataDescriptor(String fileName, DataDescriptor dataDescriptor, ByteArrayBlock block) {
        fileNameData.computeIfAbsent(fileName, ZipEntryBlock.Data::new);
        fileNameData.get(fileName).setDataDescriptor(dataDescriptor, block);
    }

    public LocalFileHeader getLocalFileHeader(String fileName) {
        return fileNameData.get(fileName).getLocalFileHeader();
    }

    public DataDescriptor getDataDescriptor(String fileName) {
        return fileNameData.get(fileName).getDataDescriptor();
    }

    public ByteArrayBlock getDataDescriptorBlock(String fileName) {
        return fileNameData.get(fileName).getDataDescriptorBlock();
    }

    public void saveEncryptionHeaderBlock(String fileName, EncryptionHeaderBlock block) {
        fileNameData.computeIfAbsent(fileName, ZipEntryBlock.Data::new);
        fileNameData.get(fileName).setEncryptionHeaderBlock(block);
    }

    public EncryptionHeaderBlock getEncryptionHeader(String fileName) {
        return fileNameData.get(fileName).getEncryptionHeaderBlock();
    }

    public ZipEntryBlock.LocalFileHeaderBlock getLocalFileHeaderBlock(String fileName) {
        return fileNameData.get(fileName).getLocalFileHeaderBlock();
    }

}
