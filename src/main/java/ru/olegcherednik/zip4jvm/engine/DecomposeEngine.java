package ru.olegcherednik.zip4jvm.engine;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.io.in.ng.BaseZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.ng.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipEntryModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.Zip64View;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class DecomposeEngine {

    private final Path zip;
    private final Path destDir;
    private final Charset charset;
    private final int offs;
    private final int columnWidth;

    public void decompose() throws IOException {
        Charset charset = Charsets.UTF_8;
        Diagnostic diagnostic = new Diagnostic();
        BlockModel blockModel = new BlockModelReader(zip, Charsets.STANDARD_ZIP_CHARSET, diagnostic).read();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(blockModel.getZipModel(), Charsets.STANDARD_ZIP_CHARSET,
                diagnostic.getZipEntryBlock()).read();

        Files.createDirectories(destDir);

        writeEndCentralDirectory(blockModel);
        writeZip64(blockModel);
        writeCentralDirectory(blockModel);

        int pos = 0;

        for (String fileName : zipEntryModel.getLocalFileHeaders().keySet()) {
            ZipEntry zipEntry = blockModel.getZipModel().getZipEntryByFileName(fileName);
            Diagnostic.ZipEntryBlock block = zipEntryModel.getZipEntryBlock();
            Diagnostic.ZipEntryBlock.LocalFileHeaderB diagLocalFileHeader = block.getLocalFileHeader(fileName);

            String str = fileName;

            if (zipEntry.isDirectory())
                str = str.substring(0, str.length() - 1);

            str = str.replaceAll("[\\/]", "_-_");

            Path dir = destDir.resolve(str);
            Files.createDirectories(dir);

            // info

            try (PrintStream out = new PrintStream(new FileOutputStream(dir.resolve("info.txt").toFile()))) {
                ZipEntryView.builder()
                            .pos(pos)
                            .localFileHeader(zipEntryModel.getLocalFileHeaders().get(fileName))
                            .diagLocalFileHeader(diagLocalFileHeader)
                            .encryptionHeader(block.getEncryptionHeader(fileName))
                            .dataDescriptor(zipEntryModel.getDataDescriptors().get(fileName))
                            .blockDataDescriptor(block.getDataDescriptor(fileName))
                            .charset(charset)
                            .offs(4)
                            .columnWidth(52).build().print(out);
            }

            // print zip entry

            FileUtils.writeByteArrayToFile(dir.resolve("local_file_header.data").toFile(), diagLocalFileHeader.getContent().getData());

            // print extra filed

            if (diagLocalFileHeader.getExtraField() != null && !diagLocalFileHeader.getExtraField().getRecords().isEmpty()) {
                Path extraFieldDir = dir.resolve("extra_fields");
                Files.createDirectories(extraFieldDir);

                ExtraFieldBlock diagExtraField = diagLocalFileHeader.getExtraField();
                LocalFileHeader localFileHeader = zipEntryModel.getLocalFileHeaders().get(fileName);

                for (int signature : diagExtraField.getRecords().keySet()) {
                    ByteArrayBlock block1 = diagExtraField.getRecord(signature);
                    ExtraField.Record rec = localFileHeader.getExtraField().getRecords().stream()
                                                           .filter(r -> r.getSignature() == signature)
                                                           .findFirst().orElse(null);

                    String title = null;

                    if (rec instanceof ExtendedTimestampExtraField)
                        title = "(0x5455)_universal_time.data";
                    else if (rec instanceof InfoZipNewUnixExtraField)
                        title = "(0x7875)_new_InfoZIP_Unix_OS2_NT.data";
                    else if (rec instanceof AesExtraDataRecord)
                        title = "(0x9901)_AES_Encryption_Tag.data";
                    else {
                        System.err.println("unknown extra field");
                        continue;
                    }

                    FileUtils.writeByteArrayToFile(extraFieldDir.resolve(title).toFile(), block1.getData());
                }
            }

            // print encryption header

            Encryption encryption = zipEntry.getEncryption();

            // TODO probably same with block reader
            if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
                AesEncryptionHeaderBlock encryptionHeader = (AesEncryptionHeaderBlock)block.getEncryptionHeader(fileName);

                FileUtils.writeByteArrayToFile(dir.resolve("aes_salt.data").toFile(), encryptionHeader.getSalt().getData());
                FileUtils.writeByteArrayToFile(dir.resolve("aes_password_checksum.data").toFile(),
                        encryptionHeader.getPasswordChecksum().getData());
                FileUtils.writeByteArrayToFile(dir.resolve("aes_mac.data").toFile(), encryptionHeader.getMac().getData());
            } else if (encryption == Encryption.PKWARE) {
                PkwareEncryptionHeader encryptionHeader = (PkwareEncryptionHeader)block.getEncryptionHeader(fileName);
                FileUtils.writeByteArrayToFile(dir.resolve("pkware_header.data").toFile(), encryptionHeader.getData().getData());
            }

            // print data descriptor

            if (zipEntry.isDataDescriptorAvailable()) {
                ByteArrayBlock dataDescriptor = block.getDataDescriptor(fileName);
                FileUtils.writeByteArrayToFile(dir.resolve("data_descriptor.data").toFile(), dataDescriptor.getData());
            }

            // payload

            if (zipEntry.getCompressedSize() != 0) {
                long size = zipEntry.getCompressedSize();
                long offs = diagLocalFileHeader.getContent().getOffs() + diagLocalFileHeader.getContent().getSize();

                if (diagLocalFileHeader.getExtraField() != null)
                    offs += diagLocalFileHeader.getExtraField().getSize();

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

                try (InputStream in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
                    try (OutputStream out = new FileOutputStream(dir.resolve("payload.data").toFile())) {
                        copyLarge(in, out, offs, size);
                    }
                }
            }

            pos++;
        }


    }

    private static BaseZipInputStream createDataInput(ZipModel zipModel, ZipEntry zipEntry) throws IOException {
        return new SingleZipInputStream(zipModel.getFile());
//        if (zipModel.isSplit())
//            return new SplitZipInputStream(zipModel, zipEntry.getDisk());
//        return new SingleZipInputStream(zipModel.getFile());
    }

    private void writeEndCentralDirectory(BlockModel blockModel) throws IOException {
        try (PrintStream out = new PrintStream(destDir.resolve("end_central_directory.txt").toFile())) {
            createEndCentralDirectoryView(blockModel).print(out);
        }

        try (InputStream in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
            try (OutputStream out = new FileOutputStream(destDir.resolve("end_central_directory.data").toFile())) {
                copyLarge(in, out, blockModel.getDiagnostic().getEndCentralDirectoryBlock());
            }
        }
    }

    private void writeZip64(BlockModel blockModel) throws IOException {
        if (blockModel.getZip64() == Zip64.NULL)
            return;

        Path dir = destDir.resolve("zip64");
        Files.createDirectories(dir);

        // (PK0607) ZIP64 End of Central directory locator
        try (PrintStream out = new PrintStream(dir.resolve("zip64_end_central_directory_locator.txt").toFile())) {
            createZip64EndCentralDirectoryLocatorView(blockModel.getZip64(), blockModel.getDiagnostic().getZip64Block()).print(out);
        }

        try (InputStream in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
            try (OutputStream out = new FileOutputStream(dir.resolve("zip64_end_central_directory_locator.data").toFile())) {
                copyLarge(in, out, blockModel.getDiagnostic().getZip64Block().getEndCentralDirectoryLocatorBlock());
            }
        }

        // (PK0606) ZIP64 End of Central directory record
        try (PrintStream out = new PrintStream(dir.resolve("zip64_end_central_directory.txt").toFile())) {
            createZip64EndCentralDirectoryView(blockModel.getZip64(), blockModel.getDiagnostic().getZip64Block()).print(out);
        }

        try (InputStream in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
            try (OutputStream out = new FileOutputStream(dir.resolve("zip64_end_central_directory.data").toFile())) {
                copyLarge(in, out, blockModel.getDiagnostic().getZip64Block().getEndCentralDirectoryBlock());
            }
        }
    }

    private void writeCentralDirectory(BlockModel blockModel) throws IOException {
        Path dir = destDir.resolve("central_directory");
        Files.createDirectories(dir);

        try (PrintStream out = new PrintStream(new FileOutputStream(dir.resolve("info.txt").toFile(), true))) {
            createCentralDirectoryView(blockModel).printHeader(out);
        }

        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : blockModel.getCentralDirectory().getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            ZipEntry zipEntry = blockModel.getZipModel().getZipEntryByFileName(fileName);
            CentralDirectoryBlock.FileHeaderBlock block = blockModel.getDiagnostic().getCentralDirectoryBlock().getFileHeader(fileName);

            if (zipEntry.isDirectory())
                fileName = fileName.substring(0, fileName.length() - 1);

            fileName = "#" + (pos + 1) + " - " + fileName.replaceAll("[\\/]", "_-_");

            Path subDir = dir.resolve(fileName);
            Files.createDirectories(subDir);

            try (PrintStream out = new PrintStream(new FileOutputStream(subDir.resolve("info.txt").toFile()))) {
                FileHeaderView.builder()
                              .fileHeader(fileHeader)
                              .diagFileHeader(block)
                              .pos(pos)
                              .charset(charset)
                              .offs(offs)
                              .columnWidth(columnWidth).build().print(out);
            }

            try (InputStream in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
                try (OutputStream out = new FileOutputStream(subDir.resolve("data").toFile())) {
                    copyLarge(in, out, block);
                }
            }

            pos++;
        }
    }

    private CentralDirectoryView createCentralDirectoryView(BlockModel blockModel) {
        return CentralDirectoryView.builder()
                                   .centralDirectory(blockModel.getCentralDirectory())
                                   .diagCentralDirectory(blockModel.getDiagnostic().getCentralDirectoryBlock())
                                   .charset(charset)
                                   .offs(offs)
                                   .columnWidth(columnWidth).build();
    }

    private EndCentralDirectoryView createEndCentralDirectoryView(BlockModel blockModel) {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getDiagnostic().getEndCentralDirectoryBlock())
                                      .charset(charset)
                                      .offs(offs)
                                      .columnWidth(columnWidth).build();
    }

    private Zip64View.EndCentralDirectoryLocatorView createZip64EndCentralDirectoryLocatorView(Zip64 zip64, Zip64Block block) {
        return Zip64View.EndCentralDirectoryLocatorView.builder()
                                                       .locator(zip64.getEndCentralDirectoryLocator())
                                                       .block(block.getEndCentralDirectoryLocatorBlock())
                                                       .offs(offs)
                                                       .columnWidth(columnWidth).build();
    }

    private Zip64View.EndCentralDirectoryView createZip64EndCentralDirectoryView(Zip64 zip64, Zip64Block block) {
        return Zip64View.EndCentralDirectoryView.builder()
                                                .endCentralDirectory(zip64.getEndCentralDirectory())
                                                .block(block.getEndCentralDirectoryBlock())
                                                .offs(offs)
                                                .columnWidth(columnWidth).build();
    }

    private static void copyLarge(InputStream in, OutputStream out, Block block) throws IOException {
        in.skip(block.getOffs());
        IOUtils.copyLarge(in, out, 0, block.getSize());
    }

    private static void copyLarge(InputStream in, OutputStream out, long offs, long length) throws IOException {
        in.skip(offs);
        IOUtils.copyLarge(in, out, 0, length);
    }

}
