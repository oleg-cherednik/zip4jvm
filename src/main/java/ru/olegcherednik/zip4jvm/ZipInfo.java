package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import ru.olegcherednik.zip4jvm.io.in.ng.BaseZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.ng.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipEntryModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesEncryptionHeader;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.AesExtraDataRecord;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.os.ExtendedTimestampExtraField;
import ru.olegcherednik.zip4jvm.model.os.InfoZipNewUnixExtraField;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;
import ru.olegcherednik.zip4jvm.view.zip64.Zip64View;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;

import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireExists;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireNotNull;
import static ru.olegcherednik.zip4jvm.utils.ValidationUtils.requireRegularFile;

/**
 * @author Oleg Cherednik
 * @since 11.10.2019
 */
@RequiredArgsConstructor
public final class ZipInfo {

    private final Path zip;

    public static ZipInfo zip(Path zip) {
        requireNotNull(zip, "ZipInfo.zip");
        requireExists(zip);
        requireRegularFile(zip, "ZipInfo.zip");

        return new ZipInfo(zip);
    }

    public void getShortInfo() throws IOException {
        Function<Charset, Charset> charsetCustomizer = charset -> Charsets.UTF_8;//Charsets.SYSTEM_CHARSET;
//        Function<Charset, Charset> charsetCustomizer = Charsets.SYSTEM_CHARSET;
        Charset charset = charsetCustomizer.apply(Charsets.IBM437);
        final String prefix = "    ";
        final int offs = prefix.length();
        final int columnWidth = 52;

        Diagnostic diagnostic = new Diagnostic();
        BlockModel blockModel = new BlockModelReader(zip, charsetCustomizer, diagnostic).read();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(blockModel.getZipModel(), charsetCustomizer,
                diagnostic.getZipEntryBlock()).read();

        PrintStream out = System.out;

        boolean emptyLine = createEndCentralDirectoryView(blockModel, charset, offs, columnWidth).print(out);
        emptyLine = createZip64View(blockModel, offs, columnWidth).print(out, emptyLine);
        emptyLine = createCentralDirectoryView(blockModel, charset, offs, columnWidth).print(out, emptyLine);
        createZipEntriesView(zipEntryModel, charset, offs, columnWidth).print(out, emptyLine);
    }

    private static IView createEndCentralDirectoryView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getDiagnostic().getEndCentralDirectory())
                                      .charset(charset)
                                      .offs(offs)
                                      .columnWidth(columnWidth).build();
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static IView createZip64View(BlockModel blockModel, int offs, int columnWidth) {
        return Zip64View.builder()
                        .zip64(blockModel.getZip64())
                        .diagZip64(blockModel.getDiagnostic().getZip64())
                        .offs(offs)
                        .columnWidth(columnWidth).build();
    }

    private static IView createCentralDirectoryView(BlockModel blockModel, Charset charset, int offs, int columnWidth) {
        return CentralDirectoryView.builder()
                                   .centralDirectory(blockModel.getCentralDirectory())
                                   .diagCentralDirectory(blockModel.getDiagnostic().getCentralDirectory())
                                   .charset(charset)
                                   .offs(offs)
                                   .columnWidth(columnWidth).build();
    }

    private static IView createZipEntriesView(BlockZipEntryModel zipEntryModel, Charset charset, int offs, int columnWidth) {
        return ZipEntryListView.builder()
                               .blockZipEntryModel(zipEntryModel)
                               .charset(charset)
                               .offs(offs)
                               .columnWidth(columnWidth).build();
    }

    public void decompose(Path destDir) throws IOException {
        Charset charset = Charsets.UTF_8;
        Diagnostic diagnostic = new Diagnostic();
        BlockModel blockModel = new BlockModelReader(zip, Charsets.STANDARD_ZIP_CHARSET, diagnostic).read();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(blockModel.getZipModel(), Charsets.STANDARD_ZIP_CHARSET,
                diagnostic.getZipEntryBlock()).read();

        String prefix = "";

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
                            .offs(prefix.length())
                            .columnWidth(52).build().print(out);
            }

            // print zip entry

            FileUtils.writeByteArrayToFile(dir.resolve("local_file_header.data").toFile(), diagLocalFileHeader.getContent().getData());

            // print extra filed

            if (diagLocalFileHeader.getExtraField() != null && !diagLocalFileHeader.getExtraField().getRecords().isEmpty()) {
                Path extraFieldDir = dir.resolve("extra_fields");
                Files.createDirectories(extraFieldDir);

                Diagnostic.ExtraField diagExtraField = diagLocalFileHeader.getExtraField();
                LocalFileHeader localFileHeader = zipEntryModel.getLocalFileHeaders().get(fileName);

                for (int signature : diagExtraField.getRecords().keySet()) {
                    Diagnostic.ByteArrayBlockB block1 = diagExtraField.getRecord(signature);
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
                BlockAesEncryptionHeader encryptionHeader = (BlockAesEncryptionHeader)block.getEncryptionHeader(fileName);

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
                Diagnostic.ByteArrayBlockB dataDescriptor = block.getDataDescriptor(fileName);
                FileUtils.writeByteArrayToFile(dir.resolve("data_descriptor.data").toFile(), dataDescriptor.getData());
            }

            // payload

            if (zipEntry.getCompressedSize() != 0) {
                long size = zipEntry.getCompressedSize();
                long offs = diagLocalFileHeader.getContent().getOffs() + diagLocalFileHeader.getContent().getSize();

                if (diagLocalFileHeader.getExtraField() != null)
                    offs += diagLocalFileHeader.getExtraField().getSize();

                if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
                    BlockAesEncryptionHeader encryptionHeader = (BlockAesEncryptionHeader)block.getEncryptionHeader(fileName);

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

                try (InputStream in = createDataInput(blockModel.getZipModel(), zipEntry)) {
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

}
