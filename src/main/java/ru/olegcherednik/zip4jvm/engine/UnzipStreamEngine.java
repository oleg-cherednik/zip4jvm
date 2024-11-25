package ru.olegcherednik.zip4jvm.engine;

import ru.olegcherednik.zip4jvm.io.in.ConsecutiveAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.file.consecutive.SolidSequentialAccessDataInput;
import ru.olegcherednik.zip4jvm.io.in.file.random.SplitRandomAccessDataInputFile;
import ru.olegcherednik.zip4jvm.io.readers.LocalFileHeaderReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.LocalFileHeader;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.model.src.SrcZip;

import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 24.11.2024
 */
public final class UnzipStreamEngine extends BaseUnzipEngine {

    private final SrcZip srcZip;
    private final UnzipSettings settings;

    public UnzipStreamEngine(SrcZip srcZip, UnzipSettings settings) {
        super(settings.getPasswordProvider());
        this.srcZip = srcZip;
        this.settings = settings;
    }

    public void extract(Path destDir) throws IOException {
        try (ConsecutiveAccessDataInput in = new SolidSequentialAccessDataInput(srcZip)) {
            while (true) {
                boolean found = findNextLocalHeader(in);

                if (!found)
                    break;

                LocalFileHeader localFileHeader = new LocalFileHeaderReader(settings.getCharsetCustomizer()).read(in);
                int a = 0;
                a++;
//                EncryptionHeaderDecompose


                ZipEntry zipEntry = ZipEntryBuilder.build(localFileHeader,
                                                          srcZip,
                                                          settings.getCharsetCustomizer(),
                                                          in);

                extractEntry(destDir, zipEntry, ZipEntry::getFileName);

                // TODO read and check DataDescriptor

                int b = 0;
                b++;

            }

            int a = 0;
            a++;
        }
//        for (ZipEntry zipEntry : zipModel.getZipEntries())
//            extractEntry(destDir, zipEntry, ZipEntry::getFileName);
    }

    private static boolean findNextLocalHeader(ConsecutiveAccessDataInput in) throws IOException {
        try {
            while (true) {
                int sig = readDwordSignature(in);

                if (sig == LocalFileHeader.SIGNATURE)
                    return true;
                if (sig == CentralDirectory.FileHeader.SIGNATURE)
                    return false;

                in.skip(1);
            }
        } catch (EOFException e) {
            return false;
        }
    }

    private static int readDwordSignature(ConsecutiveAccessDataInput in) throws IOException {
        in.mark();
        int sig = in.readDwordSignature();
        in.markReset();
        return sig;
    }

//    private boolean isEncryptionHeaderExists() {
//        try {
//
//
//        } catch (Exception e) {
//            return false;
//        }
//    }

    public static DataInput createDataInput(SrcZip srcZip) throws IOException {
        return srcZip.isSolid() ? new SolidSequentialAccessDataInput(srcZip)
                                : new SplitRandomAccessDataInputFile(srcZip);
    }


}
