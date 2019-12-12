package ru.olegcherednik.zip4jvm.io.readers.block;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 20.10.2019
 */
@RequiredArgsConstructor
public class BlockZipEntryModelReader {

    private final ZipModel zipModel;
    private final Function<Charset, Charset> customizeCharset;
    private final BlockZipEntryModel model = new BlockZipEntryModel();

    public BlockZipEntryModel read() throws IOException {
        for (ZipEntry zipEntry : zipModel.getZipEntries()) {
            try (DataInput in = zipModel.createDataInput(zipEntry.getFileName())) {
                readLocalFileHeader(zipEntry, in);
                readEncryptionHeader(zipEntry, in);
                readDataDescriptor(zipEntry, in);
            }
        }

        return model;
    }

    private void readLocalFileHeader(ZipEntry zipEntry, DataInput in) throws IOException {
        BlockLocalFileHeaderReader reader = new BlockLocalFileHeaderReader(zipEntry, customizeCharset);
        LocalFileHeader localFileHeader = reader.read(in);
        model.addLocalFileHeader(localFileHeader, reader.getBlock());
    }

    private void readEncryptionHeader(ZipEntry zipEntry, DataInput in) throws IOException {
        Encryption encryption = zipEntry.getEncryption();

        if (encryption == Encryption.AES_256 || encryption == Encryption.AES_192 || encryption == Encryption.AES_128) {
            AesEncryptionHeaderBlock encryptionHeader = new BlockAesHeaderReader(zipEntry.getStrength(), zipEntry.getCompressedSize()).read(in);
            model.saveEncryptionHeaderBlock(zipEntry.getFileName(), encryptionHeader);
        } else if (zipEntry.getEncryption() == Encryption.PKWARE) {
            PkwareEncryptionHeaderBlock encryptionHeader = new BlockPkwareHeaderReader().read(in);
            model.saveEncryptionHeaderBlock(zipEntry.getFileName(), encryptionHeader);
            in.skip(zipEntry.getCompressedSize() - encryptionHeader.getData().getData().length);
        } else
            in.skip(zipEntry.getCompressedSize());
    }

    private void readDataDescriptor(ZipEntry zipEntry, DataInput in) throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        ByteArrayBlock block = new ByteArrayBlock();
        DataDescriptor dataDescriptor = new BlockDataDescriptorReader(zipEntry.isZip64(), block).read(in);
        model.addDataDescriptor(zipEntry.getFileName(), dataDescriptor, block);
    }
}
