package ru.olegcherednik.zip4jvm.engine.decompose;

import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.DataDescriptorView;
import ru.olegcherednik.zip4jvm.view.entry.LocalFileHeaderView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
final class LocalFileHeaderDecompose {

    private final BlockModel blockModel;
    private final ZipModel zipModel;
    private final ZipInfoSettings settings;
    private final BlockZipEntryModel block;

    public LocalFileHeaderDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        zipModel = blockModel.getZipModel();
        this.settings = settings;
        block = blockModel.getZipEntryModel();

        Objects.requireNonNull(blockModel.getZipEntryModel(), "'zipEntryModel' must not be null");
    }

    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        long pos = 0;

        for (LocalFileHeader localFileHeader : block.getLocalFileHeaders().values()) {
            String fileName = localFileHeader.getFileName();
            ZipEntryBlock zipEntryBlock = block.getZipEntryBlock();
            ZipEntryBlock.LocalFileHeaderBlock localFileHeaderBlock = zipEntryBlock.getLocalFileHeader(fileName);

            ZipEntryBlock.EncryptionHeader encryptionHeader = zipEntryBlock.getEncryptionHeader(fileName);
            Encryption encryption = zipModel.getZipEntryByFileName(fileName).getEncryption();

            emptyLine |= localFileHeaderView(localFileHeader, localFileHeaderBlock, pos).print(out, pos != 0 || emptyLine);
            emptyLine |= extraFieldDecompose(localFileHeader, localFileHeaderBlock.getExtraFieldBlock()).printTextInfo(out, false);
            emptyLine |= encryptionHeader(encryptionHeader, encryption, pos).printTextInfo(out, emptyLine);
            emptyLine |= dataDescriptor(zipEntryBlock, fileName, pos, out, emptyLine);

            pos++;
        }

        return emptyLine;
    }

    public void write(Path dir) throws IOException {
        BlockZipEntryModel zipEntryModel = blockModel.getZipEntryModel();

        long pos = 0;

        for (LocalFileHeader localFileHeader : zipEntryModel.getLocalFileHeaders().values()) {
            String fileName = localFileHeader.getFileName();
            ZipEntryBlock.LocalFileHeaderBlock localFileHeaderBlock = zipEntryModel.getZipEntryBlock().getLocalFileHeader(fileName);

            ZipEntry zipEntry = zipModel.getZipEntryByFileName(fileName);
            ZipEntryBlock block = zipEntryModel.getZipEntryBlock();
            ZipEntryBlock.EncryptionHeader encryptionHeader = block.getEncryptionHeader(fileName);

            Encryption encryption = zipModel.getZipEntryByFileName(fileName).getEncryption();

            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            ZipEntryView zipEntryView = ZipEntryView.builder()
                                                    .pos(pos)
                                                    .localFileHeader(localFileHeader)
                                                    .diagLocalFileHeader(localFileHeaderBlock)
                                                    .encryptionHeader(encryptionHeader)
                                                    .dataDescriptor(zipEntryModel.getDataDescriptors().get(fileName))
                                                    .blockDataDescriptor(block.getDataDescriptor(fileName))
                                                    .getDataFunc(Utils.getDataFunc(blockModel.getZipModel()))
                                                    .charset(settings.getCharset())
                                                    .position(settings.getOffs(), settings.getColumnWidth()).build();


            writeLocalFileHeader(subDir, localFileHeader, localFileHeaderBlock, pos);
            printExtraField(subDir, localFileHeader, localFileHeaderBlock.getExtraFieldBlock());
            encryptionHeader(encryptionHeader, encryption, pos).decompose(subDir);
            printDataDescriptor(subDir, zipEntry, zipEntryView, block.getDataDescriptor(fileName));
            copyPayload(zipEntry, localFileHeaderBlock, encryptionHeader, subDir);

            pos++;
        }
    }

    private void writeLocalFileHeader(Path dir, LocalFileHeader localFileHeader, ZipEntryBlock.LocalFileHeaderBlock block, long pos)
            throws IOException {
        String fileName = "local_file_header";

        Utils.print(dir.resolve(fileName + ".txt"), out -> localFileHeaderView(localFileHeader, block, pos).print(out));
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block.getContent());
    }

    private void printExtraField(Path dir, LocalFileHeader localFileHeader, ExtraFieldBlock block) throws IOException {
        ExtraField extraField = localFileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();
        new ExtraFieldDecompose(blockModel.getZipModel(), settings, extraField, block, generalPurposeFlag).write(dir);
    }

    private void printDataDescriptor(Path dir, ZipEntry zipEntry, ZipEntryView zipEntryView, ByteArrayBlock block) throws IOException {
        if (!zipEntry.isDataDescriptorAvailable())
            return;

        String fileName = "data_descriptor";

        Utils.print(dir.resolve(fileName + ".txt"), out -> zipEntryView.createDataDescriptorView().print(out));
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block);
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

//        Utils.copyLarge(blockModel.getZipModel(), subDir.resolve("payload.data"), offs, size);
    }

    private EncryptionHeaderDecompose encryptionHeader(ZipEntryBlock.EncryptionHeader encryptionHeader, Encryption encryption, long pos) {
        return new EncryptionHeaderDecompose(encryptionHeader, pos, settings, encryption, zipModel);
    }

    private boolean dataDescriptor(ZipEntryBlock zipEntryBlock, String fileName, long pos, PrintStream out, boolean emptyLine) {
        DataDescriptor dataDescriptor = block.getDataDescriptors().get(fileName);

        if (dataDescriptor != null)
            return dataDescriptorView(dataDescriptor, zipEntryBlock.getDataDescriptor(fileName), pos).print(out, emptyLine);

        return emptyLine;
    }

    private LocalFileHeaderView localFileHeaderView(LocalFileHeader localFileHeader, ZipEntryBlock.LocalFileHeaderBlock block, long pos) {
        return LocalFileHeaderView.builder()
                                  .localFileHeader(localFileHeader)
                                  .diagLocalFileHeader(block)
                                  .pos(pos)
                                  .getDataFunc(Utils.getDataFunc(zipModel))
                                  .charset(settings.getCharset())
                                  .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private DataDescriptorView dataDescriptorView(DataDescriptor dataDescriptor, Block block, long pos) {
        return new DataDescriptorView(dataDescriptor, block, pos, settings.getOffs(), settings.getColumnWidth());
    }

    private ExtraFieldDecompose extraFieldDecompose(LocalFileHeader localFileHeader, ExtraFieldBlock block) {
        ExtraField extraField = localFileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();
        return new ExtraFieldDecompose(zipModel, settings, extraField, block, generalPurposeFlag);
    }

}
