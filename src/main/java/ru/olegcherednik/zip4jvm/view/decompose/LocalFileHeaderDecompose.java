package ru.olegcherednik.zip4jvm.view.decompose;

import ru.olegcherednik.zip4jvm.model.DataDescriptor;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.EncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.block.crypto.PkwareEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.entry.DataDescriptorView;
import ru.olegcherednik.zip4jvm.view.entry.LocalFileHeaderView;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 09.12.2019
 */
public final class LocalFileHeaderDecompose implements Decompose {

    private final BlockModel blockModel;
    private final ZipModel zipModel;
    private final ZipInfoSettings settings;

    public LocalFileHeaderDecompose(BlockModel blockModel, ZipInfoSettings settings) {
        this.blockModel = blockModel;
        zipModel = blockModel.getZipModel();
        this.settings = settings;
    }

    @Override
    public boolean printTextInfo(PrintStream out, boolean emptyLine) {
        long pos = 0;

        for (ZipEntryBlock zipEntryBlock : blockModel.getFileNameZipEntryBlock().values()) {
            String fileName = zipEntryBlock.getFileName();

            Encryption encryption = zipModel.getZipEntryByFileName(fileName).getEncryption();

            emptyLine |= localFileHeaderView(zipEntryBlock.getLocalFileHeader(), pos).print(out, pos != 0 || emptyLine);
            emptyLine |= extraFieldDecompose(zipEntryBlock.getLocalFileHeader(), zipEntryBlock.getLocalFileHeaderBlock().getExtraFieldBlock(),
                    settings.getOffs()).printTextInfo(out, false);
            emptyLine |= encryptionHeader(encryption, zipEntryBlock.getEncryptionHeaderBlock(), pos).printTextInfo(out, emptyLine);
            emptyLine |= dataDescriptor(zipEntryBlock.getDataDescriptor(), zipEntryBlock.getDataDescriptorBlock(), pos, out, emptyLine);

            pos++;
        }

        return emptyLine;
    }

    @Override
    public void decompose(Path dir) throws IOException {
        long pos = 0;

        for (ZipEntryBlock zipEntryBlock : blockModel.getFileNameZipEntryBlock().values()) {
            String fileName = zipEntryBlock.getFileName();
            Encryption encryption = zipModel.getZipEntryByFileName(fileName).getEncryption();

            Path subDir = Utils.createSubDir(dir, zipModel.getZipEntryByFileName(fileName), pos);

            localFileHeader(subDir, zipEntryBlock.getLocalFileHeader(), pos);
            extraFieldDecompose(zipEntryBlock.getLocalFileHeader(), zipEntryBlock.getLocalFileHeaderBlock().getExtraFieldBlock(), 0)
                    .decompose(subDir);
            encryptionHeader(encryption, zipEntryBlock.getEncryptionHeaderBlock(), pos).decompose(subDir);
            dataDescriptor(subDir, zipEntryBlock.getDataDescriptor(), zipEntryBlock.getDataDescriptorBlock(), pos);
            copyPayload(subDir, zipModel.getZipEntryByFileName(fileName), zipEntryBlock.getLocalFileHeaderBlock(),
                    zipEntryBlock.getEncryptionHeaderBlock());

            pos++;
        }
    }

    private void localFileHeader(Path dir, LocalFileHeader localFileHeader, long pos) throws IOException {
        String fileName = localFileHeader.getFileName();
        ZipEntryBlock.LocalFileHeaderBlock block = blockModel.getZipEntryBlock(fileName).getLocalFileHeaderBlock();

        Utils.print(dir.resolve("local_file_header.txt"), out -> localFileHeaderView(localFileHeader, pos).print(out));
        Utils.copyLarge(zipModel, dir.resolve("local_file_header.data"), block.getContent());
    }

    private void copyPayload(Path dir, ZipEntry zipEntry, ZipEntryBlock.LocalFileHeaderBlock diagLocalFileHeader,
            EncryptionHeaderBlock encryptionHeaderBlock) throws IOException {
        if (zipEntry.getCompressedSize() == 0 || !settings.isCopyPayload())
            return;

        long size = zipEntry.getCompressedSize();
        long offs = diagLocalFileHeader.getContent().getOffs() + diagLocalFileHeader.getContent().getSize();

        if (diagLocalFileHeader.getExtraFieldBlock() != null)
            offs += diagLocalFileHeader.getExtraFieldBlock().getSize();

        Encryption encryption = zipEntry.getEncryption();

        if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
            AesEncryptionHeaderBlock encryptionHeader1 = (AesEncryptionHeaderBlock)encryptionHeaderBlock;

            offs += encryptionHeader1.getSalt().getSize();
            offs += encryptionHeader1.getPasswordChecksum().getSize();

            size -= encryptionHeader1.getSalt().getSize();
            size -= encryptionHeader1.getPasswordChecksum().getSize();
            size -= encryptionHeader1.getMac().getSize();
        } else if (encryption == Encryption.PKWARE) {
            PkwareEncryptionHeaderBlock encryptionHeader1 = (PkwareEncryptionHeaderBlock)encryptionHeaderBlock;
            offs += encryptionHeader1.getHeader().getSize();
            size -= encryptionHeader1.getHeader().getSize();
        }

        Utils.copyLarge(blockModel.getZipModel(), dir.resolve("payload.data"), offs, size);
    }

    private EncryptionHeaderDecompose encryptionHeader(Encryption encryption, EncryptionHeaderBlock encryptionHeaderBlock, long pos) {
        return new EncryptionHeaderDecompose(zipModel, settings, encryption, encryptionHeaderBlock, pos);
    }

    private boolean dataDescriptor(DataDescriptor dataDescriptor, Block block, long pos, PrintStream out, boolean emptyLine) {
        if (dataDescriptor != null)
            return dataDescriptorView(dataDescriptor, block, pos).print(out, emptyLine);

        return emptyLine;
    }

    private void dataDescriptor(Path dir, DataDescriptor dataDescriptor, Block block, long pos) throws IOException {
        if (dataDescriptor == null)
            return;

        String fileName = "data_descriptor";

        Utils.print(dir.resolve(fileName + ".txt"), out -> dataDescriptorView(dataDescriptor, block, pos).print(out));
        Utils.copyLarge(zipModel, dir.resolve(fileName + ".data"), block);
    }

    private LocalFileHeaderView localFileHeaderView(LocalFileHeader localFileHeader, long pos) {
        return LocalFileHeaderView.builder()
                                  .localFileHeader(localFileHeader)
                                  .diagLocalFileHeader(blockModel.getZipEntryBlock(localFileHeader.getFileName()).getLocalFileHeaderBlock())
                                  .pos(pos)
                                  .charset(settings.getCharset())
                                  .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private DataDescriptorView dataDescriptorView(DataDescriptor dataDescriptor, Block block, long pos) {
        return new DataDescriptorView(dataDescriptor, block, pos, settings.getOffs(), settings.getColumnWidth());
    }

    private ExtraFieldDecompose extraFieldDecompose(LocalFileHeader localFileHeader, ExtraFieldBlock block, int offs) {
        ExtraField extraField = localFileHeader.getExtraField();
        GeneralPurposeFlag generalPurposeFlag = localFileHeader.getGeneralPurposeFlag();
        return new ExtraFieldDecompose(zipModel, extraField, block, generalPurposeFlag, offs, settings.getColumnWidth());
    }

}