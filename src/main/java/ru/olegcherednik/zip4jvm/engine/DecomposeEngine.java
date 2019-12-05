package ru.olegcherednik.zip4jvm.engine;

import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.readers.block.BlockModelReader;
import ru.olegcherednik.zip4jvm.io.readers.block.aes.AesEncryptionHeaderBlock;
import ru.olegcherednik.zip4jvm.io.readers.block.pkware.PkwareEncryptionHeader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ExtraField;
import ru.olegcherednik.zip4jvm.model.GeneralPurposeFlag;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.block.Block;
import ru.olegcherednik.zip4jvm.model.block.BlockModel;
import ru.olegcherednik.zip4jvm.model.block.BlockZipEntryModel;
import ru.olegcherednik.zip4jvm.model.block.ByteArrayBlock;
import ru.olegcherednik.zip4jvm.model.block.CentralDirectoryBlock;
import ru.olegcherednik.zip4jvm.model.block.ExtraFieldBlock;
import ru.olegcherednik.zip4jvm.model.block.Zip64Block;
import ru.olegcherednik.zip4jvm.model.block.ZipEntryBlock;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipInfoSettings;
import ru.olegcherednik.zip4jvm.view.EndCentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.IView;
import ru.olegcherednik.zip4jvm.view.Zip64View;
import ru.olegcherednik.zip4jvm.view.centraldirectory.CentralDirectoryView;
import ru.olegcherednik.zip4jvm.view.centraldirectory.FileHeaderView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryListView;
import ru.olegcherednik.zip4jvm.view.entry.ZipEntryView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldRecordView;
import ru.olegcherednik.zip4jvm.view.extrafield.ExtraFieldView;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author Oleg Cherednik
 * @since 15.11.2019
 */
@RequiredArgsConstructor
public final class DecomposeEngine {

    private final Path zip;
    private final ZipInfoSettings settings;

    public void decompose(Path destDir) throws IOException {
        BlockModel blockModel = new BlockModelReader(zip, settings.getCustomizeCharset()).readWithEntries();

        Files.createDirectories(destDir);

        writeEndCentralDirectory(destDir, blockModel);
        writeZip64(destDir, blockModel);
        writeCentralDirectory(destDir, blockModel);
        writeZipEntries(destDir, blockModel);
    }

    public void decompose(PrintStream out) throws IOException {
        BlockModel blockModel = new BlockModelReader(zip, settings.getCustomizeCharset()).readWithEntries();

        boolean emptyLine = createEndCentralDirectoryView(blockModel).print(out);
        emptyLine = createZip64View(blockModel).print(out, emptyLine);
        emptyLine = createCentralDirectoryView(blockModel).print(out, emptyLine);
        createZipEntriesView(blockModel).print(out, emptyLine);
    }

    private EndCentralDirectoryView createEndCentralDirectoryView(BlockModel blockModel) {
        return EndCentralDirectoryView.builder()
                                      .endCentralDirectory(blockModel.getEndCentralDirectory())
                                      .block(blockModel.getEndCentralDirectoryBlock())
                                      .charset(settings.getCharset())
                                      .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private IView createZip64View(BlockModel blockModel) {
        return Zip64View.builder()
                        .zip64(blockModel.getZip64())
                        .block(blockModel.getZip64Block())
                        .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private CentralDirectoryView createCentralDirectoryView(BlockModel blockModel) {
        return CentralDirectoryView.builder()
                                   .centralDirectory(blockModel.getCentralDirectory())
                                   .diagCentralDirectory(blockModel.getCentralDirectoryBlock())
                                   .getDataFunc(getDataFunc(blockModel))
                                   .charset(settings.getCharset())
                                   .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private IView createZipEntriesView(BlockModel blockModel) {
        return ZipEntryListView.builder()
                               .blockZipEntryModel(blockModel.getZipEntryModel())
                               .getDataFunc(getDataFunc(blockModel))
                               .charset(settings.getCharset())
                               .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private void writeEndCentralDirectory(Path destDir, BlockModel blockModel) throws IOException {
        try (PrintStream out = new PrintStream(destDir.resolve("end_central_directory.txt").toFile())) {
            createEndCentralDirectoryView(blockModel).print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), destDir.resolve("end_central_directory.data"), blockModel.getEndCentralDirectoryBlock());
    }

    private void writeZip64(Path destDir, BlockModel blockModel) throws IOException {
        if (blockModel.getZip64() == Zip64.NULL)
            return;

        Path dir = destDir.resolve("zip64");
        Files.createDirectories(dir);

        // (PK0607) ZIP64 End of Central directory locator
        try (PrintStream out = new PrintStream(dir.resolve("zip64_end_central_directory_locator.txt").toFile())) {
            createZip64EndCentralDirectoryLocatorView(blockModel.getZip64(), blockModel.getZip64Block()).print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), dir.resolve("zip64_end_central_directory_locator.data"),
                blockModel.getZip64Block().getEndCentralDirectoryLocatorBlock());

        // (PK0606) ZIP64 End of Central directory record
        try (PrintStream out = new PrintStream(dir.resolve("zip64_end_central_directory.txt").toFile())) {
            createZip64EndCentralDirectoryView(blockModel.getZip64(), blockModel.getZip64Block()).print(out);
        }

        copyLarge(blockModel.getZipModel().getFile(), dir.resolve("zip64_end_central_directory.data"),
                blockModel.getZip64Block().getEndCentralDirectoryBlock());
    }

    private void writeCentralDirectory(Path destDir, BlockModel blockModel) throws IOException {
        Path dir = destDir.resolve("central_directory");
        Files.createDirectories(dir);

        try (PrintStream out = new PrintStream(new FileOutputStream(dir.resolve("central_directory.txt").toFile(), true))) {
            createCentralDirectoryView(blockModel).printHeader(out);
        }

        int pos = 0;

        for (CentralDirectory.FileHeader fileHeader : blockModel.getCentralDirectory().getFileHeaders()) {
            String fileName = fileHeader.getFileName();
            ZipEntry zipEntry = blockModel.getZipModel().getZipEntryByFileName(fileName);
            CentralDirectoryBlock.FileHeaderBlock block = blockModel.getCentralDirectoryBlock().getFileHeaderBlock(fileName);

            if (zipEntry.isDirectory())
                fileName = fileName.substring(0, fileName.length() - 1);

            fileName = "#" + (pos + 1) + " - " + fileName.replaceAll("[\\/]", "_-_");

            Path subDir = dir.resolve(fileName);
            Files.createDirectories(subDir);

            try (PrintStream out = new PrintStream(new FileOutputStream(subDir.resolve("file_header.txt").toFile()))) {
                FileHeaderView.builder()
                              .fileHeader(fileHeader)
                              .diagFileHeader(block)
                              .getDataFunc(getDataFunc(blockModel))
                              .pos(pos)
                              .charset(settings.getCharset())
                              .offs(settings.getOffs())
                              .columnWidth(settings.getColumnWidth()).build().print(out);
            }

            copyLarge(blockModel.getZipModel().getFile(), subDir.resolve("file_header.data"), block);
            writeExtraField(blockModel, fileHeader.getExtraField(), block.getExtraFieldBlock(), fileHeader.getGeneralPurposeFlag(), subDir);

            pos++;
        }
    }

    private void writeZipEntries(Path destDir, BlockModel blockModel) throws IOException {
        BlockZipEntryModel zipEntryModel = blockModel.getZipEntryModel();

        if (blockModel.getZipEntryModel() == null)
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
                            .getDataFunc(getDataFunc(blockModel))
                            .charset(settings.getCharset())
                            .offs(settings.getOffs())
                            .columnWidth(settings.getColumnWidth()).build().print(out);
            }

            // print zip entry

            FileUtils.writeByteArrayToFile(subDir.resolve("local_file_header.data").toFile(), diagLocalFileHeader.getContent().getData());

            // print extra filed

            LocalFileHeader localFileHeader = zipEntryModel.getLocalFileHeaders().get(fileName);
            writeExtraField(blockModel, localFileHeader.getExtraField(), diagLocalFileHeader.getExtraFieldBlock(),
                    localFileHeader.getGeneralPurposeFlag(),
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

    private Zip64View.EndCentralDirectoryLocatorView createZip64EndCentralDirectoryLocatorView(Zip64 zip64, Zip64Block block) {
        return Zip64View.EndCentralDirectoryLocatorView.builder()
                                                       .locator(zip64.getEndCentralDirectoryLocator())
                                                       .block(block.getEndCentralDirectoryLocatorBlock())
                                                       .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private Zip64View.EndCentralDirectoryView createZip64EndCentralDirectoryView(Zip64 zip64, Zip64Block block) {
        return Zip64View.EndCentralDirectoryView.builder()
                                                .endCentralDirectory(zip64.getEndCentralDirectory())
                                                .block(block.getEndCentralDirectoryBlock())
                                                .position(settings.getOffs(), settings.getColumnWidth()).build();
    }

    private static void copyLarge(Path in, Path out, Block block) throws IOException {
        copyLarge(in, out, block.getOffs(), block.getSize());
    }

    private static void copyLarge(Path in, Path out, long offs, long size) throws IOException {
        try (FileInputStream fis = new FileInputStream(in.toFile()); FileOutputStream fos = new FileOutputStream(out.toFile())) {
            copyLarge(fis, fos, offs, size);
        }
    }

    private static void copyLarge(FileInputStream in, FileOutputStream out, long offs, long size) throws IOException {
        in.skip(offs);
        IOUtils.copyLarge(in, out, 0, size);
    }

    @RequiredArgsConstructor
    static class Task implements Runnable {

        private static final ThreadLocal<byte[]> THREAD_LOCAL_BUFFER = ThreadLocal.withInitial(() -> new byte[1024 * 4]);

        private final long i;
        private final Path src;
        private final Path dest;
        private final CountDownLatch barrier;
        private final long srcOffs;
        private final long destOffs;
        private final long s;

        @Override
        public void run() {
            try {
                System.out.format("%d: offs = %d (%d)\tsize = %d\n", i, srcOffs, Files.size(src) - srcOffs, s);
            } catch(IOException e) {
                e.printStackTrace();
            }

            try (RandomAccessFile in = new RandomAccessFile(src.toFile(), "r");
                 RandomAccessFile out = new RandomAccessFile(dest.toFile(), "rw")) {
                in.seek(srcOffs);
                out.seek(destOffs);

                byte[] buf = THREAD_LOCAL_BUFFER.get();
                int total = 0;

                while (total < s) {
                    int n = in.read(buf);

                    if (n == IOUtils.EOF)
                        break;

                    out.write(buf, 0, n);
                    total += n;
                }

                barrier.countDown();
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void main(String... args) throws Exception {
//        Path src = Paths.get("f:/1997.The.Swan.Princess.and.the.Secret.of.the.Castle.DVDRip.XviD.AC3.Dub.Eng.avi");
//        Path dest = Paths.get("e:/1997.The.Swan.Princess.and.the.Secret.of.the.Castle.DVDRip.XviD.AC3.Dub.Eng.avi");
        Path src = Paths.get("d:/zip4jvm/ferdinand.mkv");
        Path dest = Paths.get("e:/ferdinand.mkv");

        final long srcSize = Files.size(src);

        try (RandomAccessFile file = new RandomAccessFile(dest.toFile(), "rw")) {
            file.setLength(srcSize);
        }

        final int size = 1024 * 1024 * 100;
        final int count = (int)(srcSize / size + (srcSize % size == 0 ? 0 : 1));

        long time = System.currentTimeMillis();

        ExecutorService pool = Executors.newFixedThreadPool(16);
        CountDownLatch barrier = new CountDownLatch(count);

        long pos = 0;
        long i = 0;

        while (pos < srcSize) {
            System.err.println(i + "run offs: " + pos + ", more: " + (srcSize - pos));
            int n = (int)Math.min(size, srcSize - size);
            pool.submit(new Task(i, src, dest, barrier, pos, pos, n));
            pos += n;
            i++;
        }

        barrier.await();
        pool.shutdown();
        System.out.println("complete: " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time));
    }

    private void writeExtraField(BlockModel blockModel, ExtraField extraField, ExtraFieldBlock block, GeneralPurposeFlag generalPurposeFlag,
            Path parent) throws IOException {
        if (extraField == ExtraField.NULL)
            return;

        Path dir = parent.resolve("extra_fields");
        Files.createDirectories(dir);

        ExtraFieldView extraFieldView = ExtraFieldView.builder()
                                                      .extraField(extraField)
                                                      .block(block)
                                                      .generalPurposeFlag(generalPurposeFlag)
                                                      .getDataFunc(getDataFunc(blockModel))
                                                      .position(0, settings.getColumnWidth()).build();

        for (int signature : extraField.getSignatures()) {
            ExtraField.Record record = extraField.getRecord(signature);
            ExtraFieldRecordView<?> recordView = extraFieldView.getView(record);

            // print .txt
            try (PrintStream out = new PrintStream(new FileOutputStream(dir.resolve(recordView.getFileName() + ".txt").toFile()))) {
                recordView.print(out);
            }

            // print .data
            copyLarge(blockModel.getZipModel().getFile(), dir.resolve(recordView.getFileName() + ".data"), block.getRecordBlock(signature));
        }
    }

    private static Function<Block, byte[]> getDataFunc(BlockModel blockModel) {
        return block -> {
            if (block.getSize() > Integer.MAX_VALUE)
                return ArrayUtils.EMPTY_BYTE_ARRAY;

            try (DataInput in = new SingleZipInputStream(blockModel.getZipModel().getFile())) {
                in.skip(block.getOffs());
                return in.readBytes((int)block.getSize());
            } catch(Exception e) {
                e.printStackTrace();
                return ArrayUtils.EMPTY_BYTE_ARRAY;
            }
        };
    }

}
