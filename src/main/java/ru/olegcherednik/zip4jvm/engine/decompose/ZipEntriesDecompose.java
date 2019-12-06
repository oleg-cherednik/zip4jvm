package ru.olegcherednik.zip4jvm.engine.decompose;

import org.apache.commons.io.FileUtils;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class ZipEntriesDecompose extends BaseDecompose {

    public ZipEntriesDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        super(blockModel, settings);
    }

    @Override
    protected IView createView() {
        BlockZipEntryModel zipEntryModel = blockModel.getZipEntryModel();

        if (zipEntryModel == null)
            return IView.NULL;

        return ZipEntryListView.builder()
                               .blockZipEntryModel(zipEntryModel)
                               .getDataFunc(getDataFunc(blockModel.getZipModel()))
                               .charset(settings.getCharset())
                               .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    @Override
    public void write(Path destDir) throws IOException {
        BlockZipEntryModel zipEntryModel = blockModel.getZipEntryModel();

        if (zipEntryModel == null)
            return;

        Path dir = destDir.resolve("entries");
        Files.createDirectories(dir);

        int pos = 0;

        for (String fileName : zipEntryModel.getLocalFileHeaders().keySet()) {
            ZipEntry zipEntry = blockModel.getZipModel().getZipEntryByFileName(fileName);
            ZipEntryBlock block = zipEntryModel.getZipEntryBlock();
            ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader = block.getLocalFileHeader(fileName);

            String str = fileName;

            if (zipEntry.isDirectory())
                str = str.substring(0, str.length() - 1);

            str = "#" + (pos + 1) + " - " + str.replaceAll("[\\/]", "_-_");

            Path subDir = dir.resolve(str);
            Files.createDirectories(subDir);

            // info

            try (PrintStream out = new PrintStream(new FileOutputStream(subDir.resolve("info.txt").toFile()))) {
                ZipEntryView.builder()
                            .pos(pos)
                            .localFileHeader(zipEntryModel.getLocalFileHeaders().get(fileName))
                            .diagLocalFileHeader(diagLocalFileHeader)
                            .encryptionHeader(block.getEncryptionHeader(fileName))
                            .dataDescriptor(zipEntryModel.getDataDescriptors().get(fileName))
                            .blockDataDescriptor(block.getDataDescriptor(fileName))
                            .getDataFunc(getDataFunc(blockModel.getZipModel()))
                            .charset(settings.getCharset())
                            .offs(settings.getOffs())
                            .columnWidth(settings.getColumnWidth()).build().print(out);
            }

            // print zip entry

            FileUtils.writeByteArrayToFile(subDir.resolve("local_file_header.data").toFile(), diagLocalFileHeader.getContent().getData());

            // print extra filed

            LocalFileHeader localFileHeader = zipEntryModel.getLocalFileHeaders().get(fileName);
            writeExtraField(localFileHeader.getExtraField(), diagLocalFileHeader.getExtraFieldBlock(), localFileHeader.getGeneralPurposeFlag(),
                    subDir);

            // print encryption header

            Encryption encryption = zipEntry.getEncryption();

            // TODO probably same with block reader
            if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
                AesEncryptionHeaderBlock encryptionHeader = (AesEncryptionHeaderBlock)block.getEncryptionHeader(fileName);

                FileUtils.writeByteArrayToFile(subDir.resolve("aes_salt.data").toFile(), encryptionHeader.getSalt().getData());
                FileUtils.writeByteArrayToFile(subDir.resolve("aes_password_checksum.data").toFile(),
                        encryptionHeader.getPasswordChecksum().getData());
                FileUtils.writeByteArrayToFile(subDir.resolve("aes_mac.data").toFile(), encryptionHeader.getMac().getData());
            } else if (encryption == Encryption.PKWARE) {
                PkwareEncryptionHeader encryptionHeader = (PkwareEncryptionHeader)block.getEncryptionHeader(fileName);
                FileUtils.writeByteArrayToFile(subDir.resolve("pkware_header.data").toFile(), encryptionHeader.getData().getData());
            }

            // print data descriptor

            if (zipEntry.isDataDescriptorAvailable()) {
                ByteArrayBlock dataDescriptor = block.getDataDescriptor(fileName);
                FileUtils.writeByteArrayToFile(subDir.resolve("data_descriptor.data").toFile(), dataDescriptor.getData());
            }

            // payload

            if (zipEntry.getCompressedSize() != 0) {
                long size = zipEntry.getCompressedSize();
                long offs = diagLocalFileHeader.getContent().getOffs() + diagLocalFileHeader.getContent().getSize();

                if (diagLocalFileHeader.getExtraFieldBlock() != null)
                    offs += diagLocalFileHeader.getExtraFieldBlock().getSize();

                if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
                    AesEncryptionHeaderBlock encryptionHeader = (AesEncryptionHeaderBlock)block.getEncryptionHeader(fileName);

                    offs += encryptionHeader.getSalt().getSize();
                    offs += encryptionHeader.getPasswordChecksum().getSize();

                    size -= encryptionHeader.getSalt().getSize();
                    size -= encryptionHeader.getPasswordChecksum().getSize();
                    size -= encryptionHeader.getMac().getSize();
                } else if (encryption == Encryption.PKWARE) {
                    PkwareEncryptionHeader encryptionHeader = (PkwareEncryptionHeader)block.getEncryptionHeader(fileName);
                    offs += encryptionHeader.getData().getSize();
                    size -= encryptionHeader.getData().getSize();
                }

                copyLarge(blockModel.getZipModel().getFile(), subDir.resolve("payload.data"), offs, size);
            }

            pos++;
        }
    }


}
