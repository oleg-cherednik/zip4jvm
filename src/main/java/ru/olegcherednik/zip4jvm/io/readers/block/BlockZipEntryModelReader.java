package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.SplitZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.BlockPkwareHeaderReader;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class BlockZipEntryModelReader {

    private final ZipModel zipModel;
    private final Function<Charset, Charset> charsetCustomizer;
    private final ZipEntryBlock zipEntryBlock;

    public BlockZipEntryModel read() throws IOException {
        Map<String, LocalFileHeader> localFileHeaders = new LinkedHashMap<>();
        Map<String, DataDescriptor> dataDescriptors = new LinkedHashMap<>();

        for (ZipEntry zipEntry : zipModel.getZipEntries()) {
            try (DataInput in = createDataInput(zipModel, zipEntry)) {
                localFileHeaders.put(zipEntry.getFileName(), readLocalFileHeader(zipEntry, in));
                readEncryptionHeader(zipEntry, in);

                if (zipEntry.isDataDescriptorAvailable())
                    dataDescriptors.put(zipEntry.getFileName(), readDataDescriptor(zipEntry, in));
            }
        }

        return new BlockZipEntryModel(zipEntryBlock, localFileHeaders, dataDescriptors);
    }

    private LocalFileHeader readLocalFileHeader(ZipEntry zipEntry, DataInput in) throws IOException {
        zipEntryBlock.addLocalFileHeader();
        zipEntryBlock.getLocalFileHeader().setDisk(zipEntry.getDisk());
        long offs = zipEntry.getLocalFileHeaderOffs();
        ZipEntryBlock.LocalFileHeaderBlock blockLocalFileHeader = zipEntryBlock.getLocalFileHeader();
        LocalFileHeader localFileHeader = new BlockLocalFileHeaderReader(offs, charsetCustomizer, blockLocalFileHeader).read(in);
        zipEntryBlock.saveLocalFileHeader(localFileHeader.getFileName());
        return localFileHeader;
    }

    private void readEncryptionHeader(ZipEntry zipEntry, DataInput in) throws IOException {
        Encryption encryption = zipEntry.getEncryption();

        if (encryption == Encryption.AES_256 || encryption == Encryption.AES_192 || encryption == Encryption.AES_128) {
            AesEncryptionHeaderBlock encryptionHeader = new BlockAesHeaderReader(zipEntry.getStrength(), zipEntry.getCompressedSize()).read(in);
            zipEntryBlock.saveEncryptionHeader(zipEntry.getFileName(), encryptionHeader);
        } else if (zipEntry.getEncryption() == Encryption.PKWARE) {
            PkwareEncryptionHeader encryptionHeader = new BlockPkwareHeaderReader().read(in);
            zipEntryBlock.saveEncryptionHeader(zipEntry.getFileName(), encryptionHeader);
            in.skip(zipEntry.getCompressedSize() - encryptionHeader.getData().getData().length);
        } else
            in.skip(zipEntry.getCompressedSize());
    }

    private DataDescriptor readDataDescriptor(ZipEntry zipEntry, DataInput in) throws IOException {
        ByteArrayBlock block = new ByteArrayBlock();
        DataDescriptor dataDescriptor = new BlockDataDescriptorReader(zipEntry.isZip64(), block).read(in);
        zipEntryBlock.saveDataDescriptor(zipEntry.getFileName(), block);
        return dataDescriptor;
    }

    // TODO duplication with ZipInfo
    private static DataInput createDataInput(ZipModel zipModel, ZipEntry zipEntry) throws IOException {
        if (zipModel.isSplit())
            return new SplitZipInputStream(zipModel, zipEntry.getDisk());
        return new SingleZipInputStream(zipModel.getFile());
    }
}
