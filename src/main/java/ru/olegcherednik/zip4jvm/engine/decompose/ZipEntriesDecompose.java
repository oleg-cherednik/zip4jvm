package ru.olegcherednik.zip4jvm.engine.decompose;

import org.apache.commons.io.FileUtils;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.crypto.EncryptionHeaderView;
import ru.olegcherednik.zip4jvm.view.entry.LocalFileHeaderView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

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
    private final ZipModel zipModel;
    private final ZipInfoSettings settings;

    public ZipEntriesDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        this.zipModel = blockModel.getZipModel();
        this.settings = settings;
    }

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        return blockModel.getZipEntryModel() != null && createView().print(out, emptyLine);
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
            ZipEntry zipEntry = zipModel.getZipEntryByFileName(fileName);
            ZipEntryBlock block = zipEntryModel.getZipEntryBlock();
            ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader = block.getLocalFileHeader(fileName);
            ZipEntryBlock.EncryptionHeader encryptionHeader = block.getEncryptionHeader(fileName);

            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            ZipEntryView zipEntryView = ZipEntryView.builder()
                                                    .pos(pos)
                                                    .localFileHeader(localFileHeader)
                                                    .diagLocalFileHeader(diagLocalFileHeader)
                                                    .encryptionHeader(encryptionHeader)
                                                    .dataDescriptor(zipEntryModel.getDataDescriptors().get(fileName))
                                                    .blockDataDescriptor(block.getDataDescriptor(fileName))
                                                    .getDataFunc(Utils.getDataFunc(blockModel.getZipModel()))
                                                    .charset(settings.getCharset())
                                                    .offs(settings.getOffs())
                                                    .columnWidth(settings.getColumnWidth()).build();


            writeLocalFileHeader(subDir, zipEntryView.createLocalFileHeaderView(), diagLocalFileHeader);
            printExtraField(subDir, localFileHeader, diagLocalFileHeader.getExtraFieldBlock());
            printEncryptionHeader(subDir, zipEntry, zipEntryView, encryptionHeader);
            printDataDescriptor(zipEntry, block, fileName, subDir);
//            copyPayload(zipEntry, diagLocalFileHeader, encryptionHeader, subDir);

            pos++;
        }
    }

    private void writeLocalFileHeader(Path dir, LocalFileHeaderView view, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader)
            throws IOException {
        String fileName = "local_file_header";

        Utils.print(dir.resolve(fileName + ".txt"), view::print);
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), diagLocalFileHeader.getContent());
    }

    private void printExtraField(Path dir, LocalFileHeader localFileHeader, ExtraFieldBlock block) throws IOException {
        ExtraField extraField = localFileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();
        new ExtraFieldDecompose(blockModel.getZipModel(), settings, extraField, block, generalPurposeFlag).write(dir);
    }

    private static void printEncryptionHeader(Path dir, ZipEntry zipEntry, ZipEntryView zipEntryView, ZipEntryBlock.EncryptionHeader encryptionHeader)
            throws IOException {
        Encryption encryption = zipEntry.getEncryption();

        if (encryption == Encryption.OFF)
            return;

        Path subDir = Files.createDirectories(dir.resolve("encryption"));

        EncryptionHeaderView view = zipEntryView.createEncryptionHeaderView();

        // TODO probably same with block reader
        if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
            AesEncryptionHeaderBlock block = (AesEncryptionHeaderBlock)encryptionHeader;
            Utils.print(subDir.resolve("aes_encryption_header.txt"), out -> view.createView(block).print(out));

            FileUtils.writeByteArrayToFile(subDir.resolve("aes_salt.data").toFile(), block.getSalt().getData());
            FileUtils.writeByteArrayToFile(subDir.resolve("aes_password_checksum.data").toFile(), block.getPasswordChecksum().getData());
            FileUtils.writeByteArrayToFile(subDir.resolve("aes_mac.data").toFile(), block.getMac().getData());
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeader block = (PkwareEncryptionHeader)encryptionHeader;
            Utils.print(dir.resolve("pkware_encryption_header.txt"), out -> view.createView(block).print(out));
            FileUtils.writeByteArrayToFile(subDir.resolve("pkware_encryption_header.data").toFile(), block.getData().getData());
        } else {
            // TODO print unknown header
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
