package ru.olegcherednik.zip4jvm.engine.decompose;

import org.apache.commons.io.FileUtils;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 06.12.2019
 */
final class ZipEntriesDecompose {

    private final BlockModel blockModel;
    private final ZipInfoSettings settings;

    public ZipEntriesDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        this.settings = settings;
    }

    public boolean print(PrintStream out, boolean emptyLine) {
        if (blockModel.getZipEntryModel() == null)
            return false;
        return createView().print(out, emptyLine);
    }

    public void write(Path destDir) throws IOException {
        BlockZipEntryModel zipEntryModel = blockModel.getZipEntryModel();

        if (zipEntryModel == null)
            return;

        Path dir = destDir.resolve("entries");
        Files.createDirectories(dir);

        int pos = 0;

        for (LocalFileHeader localFileHeader : zipEntryModel.getLocalFileHeaders().values()) {
            String fileName = localFileHeader.getFileName();
            ZipEntry zipEntry = blockModel.getZipModel().getZipEntryByFileName(fileName);
            ZipEntryBlock block = zipEntryModel.getZipEntryBlock();
            ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader = block.getLocalFileHeader(fileName);
            ZipEntryBlock.EncryptionHeader encryptionHeader = block.getEncryptionHeader(fileName);

            String str = fileName;

            if (zipEntry.isDirectory())
                str = str.substring(0, str.length() - 1);

            str = "#" + (pos + 1) + " - " + str.replaceAll("[\\/]", "_-_");

            Path subDir = dir.resolve(str);
            Files.createDirectories(subDir);

            printInfoFile(subDir, pos, localFileHeader, diagLocalFileHeader, encryptionHeader, zipEntryModel.getDataDescriptors().get(fileName),
                    block.getDataDescriptor(fileName));
            writeZipEntry(subDir, diagLocalFileHeader);
            printExtraField(localFileHeader, diagLocalFileHeader, subDir);
            printEncryptionHeader(zipEntry, encryptionHeader, subDir);
            printDataDescriptor(zipEntry, block, fileName, subDir);
//            copyPayload(zipEntry, diagLocalFileHeader, encryptionHeader, subDir);

            pos++;
        }
    }

    private void printInfoFile(Path subDir, int pos, LocalFileHeader localFileHeader, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader,
            ZipEntryBlock.EncryptionHeader encryptionHeader, DataDescriptor dataDescriptor, Block blockDataDescriptor) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(new FileOutputStream(subDir.resolve("info.txt").toFile()))) {
            ZipEntryView.builder()
                        .pos(pos)
                        .localFileHeader(localFileHeader)
                        .diagLocalFileHeader(diagLocalFileHeader)
                        .encryptionHeader(encryptionHeader)
                        .dataDescriptor(dataDescriptor)
                        .blockDataDescriptor(blockDataDescriptor)
                        .getDataFunc(Utils.getDataFunc(blockModel.getZipModel()))
                        .charset(settings.getCharset())
                        .offs(settings.getOffs())
                        .columnWidth(settings.getColumnWidth()).build().print(out);
        }
    }

    private static void writeZipEntry(Path subDir, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader) throws IOException {
        FileUtils.writeByteArrayToFile(subDir.resolve("local_file_header.data").toFile(), diagLocalFileHeader.getContent().getData());
    }

    private void printExtraField(LocalFileHeader localFileHeader, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader, Path subDir)
            throws IOException {
        new ExtraFieldDecompose(blockModel.getZipModel(), settings, localFileHeader.getExtraField(), diagLocalFileHeader.getExtraFieldBlock(),
                localFileHeader.getGeneralPurposeFlag()).write(subDir);
    }

    private static void printEncryptionHeader(ZipEntry zipEntry, ZipEntryBlock.EncryptionHeader encryptionHeader, Path subDir) throws IOException {
        Encryption encryption = zipEntry.getEncryption();

        // TODO probably same with block reader
        if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
            AesEncryptionHeaderBlock encryptionHeader1 = (AesEncryptionHeaderBlock)encryptionHeader;

            FileUtils.writeByteArrayToFile(subDir.resolve("aes_salt.data").toFile(), encryptionHeader1.getSalt().getData());
            FileUtils.writeByteArrayToFile(subDir.resolve("aes_password_checksum.data").toFile(),
                    encryptionHeader1.getPasswordChecksum().getData());
            FileUtils.writeByteArrayToFile(subDir.resolve("aes_mac.data").toFile(), encryptionHeader1.getMac().getData());
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeader encryptionHeader1 = (PkwareEncryptionHeader)encryptionHeader;
            FileUtils.writeByteArrayToFile(subDir.resolve("pkware_header.data").toFile(), encryptionHeader1.getData().getData());
        }
    }

    private static void printDataDescriptor(ZipEntry zipEntry, ZipEntryBlock block, String fileName, Path subDir) throws IOException {
        if (zipEntry.isDataDescriptorAvailable()) {
            ByteArrayBlock dataDescriptor = block.getDataDescriptor(fileName);
            FileUtils.writeByteArrayToFile(subDir.resolve("data_descriptor.data").toFile(), dataDescriptor.getData());
        }
    }

    private void copyPayload(ZipEntry zipEntry, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader,
            ZipEntryBlock.EncryptionHeader encryptionHeader, Path subDir) throws IOException {
        if (zipEntry.getCompressedSize() == 0)
            return;

        long size = zipEntry.getCompressedSize();
        long offs = diagLocalFileHeader.getContent().getOffs() + diagLocalFileHeader.getContent().getSize();

        if (diagLocalFileHeader.getExtraFieldBlock() != null)
            offs += diagLocalFileHeader.getExtraFieldBlock().getSize();

        Encryption encryption = zipEntry.getEncryption();

        if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
            AesEncryptionHeaderBlock encryptionHeader1 = (AesEncryptionHeaderBlock)encryptionHeader;

            offs += encryptionHeader1.getSalt().getSize();
            offs += encryptionHeader1.getPasswordChecksum().getSize();

            size -= encryptionHeader1.getSalt().getSize();
            size -= encryptionHeader1.getPasswordChecksum().getSize();
            size -= encryptionHeader1.getMac().getSize();
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeader encryptionHeader1 = (PkwareEncryptionHeader)encryptionHeader;
            offs += encryptionHeader1.getData().getSize();
            size -= encryptionHeader1.getData().getSize();
        }

        Utils.copyLarge(blockModel.getZipModel(), subDir.resolve("payload.data"), offs, size);
    }

    private ZipEntryListView createView() {
        return ZipEntryListView.builder()
                               .blockZipEntryModel(blockModel.getZipEntryModel())
                               .getDataFunc(Utils.getDataFunc(blockModel.getZipModel()))
                               .charset(settings.getCharset())
                               .position(settings.getOffs(), settings.getColumnWidth()).build();
    }


}
