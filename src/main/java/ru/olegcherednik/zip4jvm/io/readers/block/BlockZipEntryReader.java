package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.crypto.strong.DecryptionHeader;
import ru.olegcherednik.zip4jvm.io.in.data.DataInput;
import ru.olegcherednik.zip4jvm.io.readers.block.crypto.BlockAesHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.block.crypto.BlockDecryptionHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.block.crypto.BlockPkwareHeaderReader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.EncryptionMethod;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class BlockZipEntryReader {

    private final ZipModel zipModel;
    private final Function<Charset, Charset> customizeCharset;
    private final Map<String, ZipEntryBlock> fileNameZipEntryBlock = new LinkedHashMap<>();

    public Map<String, ZipEntryBlock> read() throws IOException {
        for (ZipEntry zipEntry : zipModel.getZipEntries()) {
            try (DataInput in = zipModel.createDataInput(zipEntry.getFileName())) {
                readLocalFileHeader(zipEntry, in);
                readEncryptionHeader(zipEntry, in);
                readDataDescriptor(zipEntry, in);
            }
        }

        return fileNameZipEntryBlock.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(fileNameZipEntryBlock);
    }

    private void readLocalFileHeader(ZipEntry zipEntry, DataInput in) throws IOException {
        int disk = (int)zipEntry.getDisk();
        long relativeOffs = zipEntry.getLocalFileHeaderOffs();
        long absoluteOffs = in.convertToAbsoluteOffs(disk, relativeOffs);

        BlockLocalFileHeaderReader reader = new BlockLocalFileHeaderReader(absoluteOffs, customizeCharset);
        LocalFileHeader localFileHeader = reader.read(in);

        requireBlockExists(localFileHeader.getFileName());
        fileNameZipEntryBlock.get(localFileHeader.getFileName()).setLocalFileHeader(localFileHeader, reader.getBlock());
    }

    private void readEncryptionHeader(ZipEntry zipEntry, DataInput in) throws IOException {
        String fileName = zipEntry.getFileName();
        EncryptionMethod encryptionMethod = zipEntry.getEncryptionMethod();

        if (zipEntry.isStrongEncryption()) {
            BlockDecryptionHeaderReader reader = new BlockDecryptionHeaderReader();
            DecryptionHeader decryptionHeader = reader.read(in);
            requireBlockExists(fileName);
            fileNameZipEntryBlock.get(fileName).setDecryptionHeader(decryptionHeader, reader.getDecryptionHeaderBlock());
        } else if (encryptionMethod.isAes()) {
            EncryptionHeaderBlock block = new BlockAesHeaderReader(AesEngine.getStrength(encryptionMethod), zipEntry.getCompressedSize()).read(in);
            requireBlockExists(fileName);
            fileNameZipEntryBlock.get(fileName).setEncryptionHeaderBlock(block);
        } else if (zipEntry.getEncryptionMethod() == EncryptionMethod.PKWARE) {
            EncryptionHeaderBlock block = new BlockPkwareHeaderReader().read(in);
            in.skip(zipEntry.getCompressedSize() - ((Block)block).getSize());
            requireBlockExists(fileName);
            fileNameZipEntryBlock.get(fileName).setEncryptionHeaderBlock(block);
        } else
            in.skip(zipEntry.getCompressedSize());
    }

    private void readDataDescriptor(ZipEntry zipEntry, DataInput in) throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        BlockDataDescriptorReader reader = new BlockDataDescriptorReader(zipEntry.isZip64());
        DataDescriptor dataDescriptor = reader.read(in);
        requireBlockExists(zipEntry.getFileName());
        fileNameZipEntryBlock.get(zipEntry.getFileName()).setDataDescriptor(dataDescriptor, reader.getBlock());
    }

    private void requireBlockExists(String fileName) {
        fileNameZipEntryBlock.computeIfAbsent(fileName, ZipEntryBlock::new);
    }
}
