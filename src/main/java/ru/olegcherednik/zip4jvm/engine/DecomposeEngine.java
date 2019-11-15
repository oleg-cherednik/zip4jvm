package ru.olegcherednik.zip4jvm.engine;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import ru.olegcherednik.zip4jvm.io.in.ng.BaseZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.ng.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipEntryModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

import java.io.FileNotFoundException;
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

        Path info = destDir.resolve("info.txt");

        boolean emptyLine = writeEndCentralDirectory(blockModel, info);
        emptyLine = writeCentralDirectory(blockModel, info, emptyLine);


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
                    else
                        throw new NotImplementedException();

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
                        IOUtils.copyLarge(in, out, offs, size);
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

    private boolean writeEndCentralDirectory(BlockModel blockModel, Path info) throws IOException {
        Block block = blockModel.getDiagnostic().getEndCentralDirectoryBlock();

        try (InputStream in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
            try (OutputStream out = new FileOutputStream(destDir.resolve("end_central_directory.data").toFile())) {
                IOUtils.copyLarge(in, out, block.getOffs(), block.getSize());
            }
        }

        try (PrintStream out = new PrintStream(new FileOutputStream(info.toFile(), true))) {
            return createEndCentralDirectoryView(blockModel).print(out);
        }
    }

    private boolean writeCentralDirectory(BlockModel blockModel, Path info, boolean emptyLine) throws FileNotFoundException {
        try (PrintStream out = new PrintStream(new FileOutputStream(info.toFile(), true))) {
            CentralDirectoryView view = createCentralDirectoryView(blockModel);
            return view.printHeader(out, emptyLine);
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

    private IView createEndCentralDirectoryView(BlockModel blockModel) {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getDiagnostic().getEndCentralDirectoryBlock())
                                      .charset(charset)
                                      .offs(offs)
                                      .columnWidth(columnWidth).build();
    }

}
