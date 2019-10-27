package ru.olegcherednik.zip4jvm;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import ru.olegcherednik.zip4jvm.crypto.aes.AesEngine;
import ru.olegcherednik.zip4jvm.io.in.ng.BaseZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.ng.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockZipEntryModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.BlockAesEncryptionHeader;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.Charsets;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.Diagnostic;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.view.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.Zip64View;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;

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

        Diagnostic diagnostic = new Diagnostic();
        BlockModel blockModel = new BlockModelReader(zip, charsetCustomizer, diagnostic).read();
        BlockZipEntryModel zipEntryModel = new BlockZipEntryModelReader(blockModel.getZipModel(), charsetCustomizer,
                diagnostic.getZipEntryBlock()).read();

        PrintStream out = System.out;

        printEndCentralDirectoryView(blockModel, charset, prefix, out);
        out.println();
        printZip64View(blockModel, charset, prefix, out);
        out.println();
        printCentralDirectory(blockModel, charset, prefix, out);
        out.println();
        printZipEntries(zipEntryModel, charset, prefix, out);
    }

    private static void printEndCentralDirectoryView(BlockModel blockModel, Charset charset, String prefix, PrintStream out) {
        EndCentralDirectoryView.builder()
                               .block(blockModel.getDiagnostic().getEndCentralDirectory())
                               .dir(blockModel.getEndCentralDirectory())
                               .charset(charset)
                               .prefix(prefix).build().print(out);
    }

    @SuppressWarnings("NewMethodNamingConvention")
    private static void printZip64View(BlockModel blockModel, Charset charset, String prefix, PrintStream out) {
        Zip64View.builder()
                 .zip64(blockModel.getZip64())
                 .diagZip64(blockModel.getDiagnostic().getZip64())
                 .charset(charset)
                 .prefix(prefix).build().print(out);
    }

    private static void printCentralDirectory(BlockModel blockModel, Charset charset, String prefix, PrintStream out) {
        CentralDirectoryView.builder()
                            .centralDirectory(blockModel.getCentralDirectory())
                            .diagCentralDirectory(blockModel.getDiagnostic().getCentralDirectory())
                            .charset(charset)
                            .prefix(prefix).build().print(out);
    }

    private static void printZipEntries(BlockZipEntryModel zipEntryModel, Charset charset, String prefix, PrintStream out) throws IOException {
        ZipEntryListView.builder()
                        .blockZipEntryModel(zipEntryModel)
                        .charset(charset)
                        .prefix(prefix).build().print(out);
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
            Diagnostic.ZipEntryBlock.LocalFileHeader diagLocalFileHeader = block.getLocalFileHeader(fileName);

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
                            .prefix(prefix)
                            .build().print(out);
            }

            // print zip entry

            try (InputStream in = createDataInput(blockModel.getZipModel(), zipEntry)) {
                // print local file header


                try (OutputStream out = new FileOutputStream(dir.resolve("local_file_header.data").toFile())) {
                    long offs = diagLocalFileHeader.getOffs();
                    long length = diagLocalFileHeader.getSize();
                    IOUtils.copyLarge(in, out, offs, length);
                }

                // print encryption header

                Encryption encryption = zipEntry.getEncryption();

                // TODO probably same with block reader
                if (encryption == Encryption.AES_128 || encryption == Encryption.AES_192 || encryption == Encryption.AES_256) {
                    BlockAesEncryptionHeader encryptionHeader = (BlockAesEncryptionHeader)block.getEncryptionHeader(fileName);

                    //                    FileUtils.writeByteArrayToFile(dir.resolve("aes_salt.data").toFile(), encryptionHeader.getSalt().getData());
//                    FileUtils.writeByteArrayToFile(dir.resolve("aes_password_checksum.data").toFile(),
//                            encryptionHeader.getPasswordChecksum().getData());

                    try (OutputStream out = new FileOutputStream(dir.resolve("aes_salt.data").toFile())) {
                        long length = encryptionHeader.getSalt().getSize();
                        IOUtils.copyLarge(in, out, 0, length);
                    }

                    try (OutputStream out = new FileOutputStream(dir.resolve("aes_password_checksum.data").toFile())) {
                        long length = encryptionHeader.getPasswordChecksum().getSize();
                        IOUtils.copyLarge(in, out, 0, length);
                    }

                    try (OutputStream out = new FileOutputStream(dir.resolve("compressed_content.data").toFile())) {
                        long size = AesEngine.getDataCompressedSize(zipEntry.getCompressedSize(), zipEntry.getStrength().saltLength());
                        IOUtils.copyLarge(in, out, 0, size);
                    }

                    try (OutputStream out = new FileOutputStream(dir.resolve("aes_mac.data").toFile())) {
                        long length = encryptionHeader.getMac().getSize();
                        IOUtils.copyLarge(in, out, 0, length);
                    }

//                    FileUtils.writeByteArrayToFile(dir.resolve("aes_mac.data").toFile(), encryptionHeader.getMac().getData());
                } else if (encryption == Encryption.PKWARE) {
                    PkwareEncryptionHeader encryptionHeader = (PkwareEncryptionHeader)block.getEncryptionHeader(fileName);

                    try (OutputStream out = new FileOutputStream(dir.resolve("pkware_header.data").toFile())) {
                        //                    long offs = encryptionHeader.getData().getOffs();
                        long length = encryptionHeader.getData().getSize();
                        IOUtils.copyLarge(in, out, 0, length);
                    }

                    try (OutputStream out = new FileOutputStream(dir.resolve("compressed_content.data").toFile())) {
                        long size = zipEntry.getCompressedSize() - encryptionHeader.getData().getData().length;
                        IOUtils.copyLarge(in, out, 0, size);
                    }
                }

                // print data descriptor

                if (zipEntry.isDataDescriptorAvailable()) {
                    Block dataDescriptor = block.getDataDescriptor(fileName);

                    try (OutputStream out = new FileOutputStream(dir.resolve("data_descriptor.data").toFile())) {
                        long size = dataDescriptor.getSize();
                        IOUtils.copyLarge(in, out, 0, size);
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
