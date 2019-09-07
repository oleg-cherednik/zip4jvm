package ru.olegcherednik.zip4jvm;

import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReadSettings;

import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public class ZipFileReader {

    private final ZipModel zipModel;
    private final ZipFileReadSettings settings;

    public ZipFileReader(Path zip, ZipFileReadSettings settings) throws IOException {
        zipModel = ZipModelBuilder.read(zip);
        this.settings = settings;
    }

//    public void extract(@NonNull Path destDir) {
//        zipModel.getEntries().forEach(entry -> extractEntry(destDir, entry));
//    }

//    private void extractEntry(Path dstDir, ZipEntry entry) {
//        entry.setPassword(settings.getPassword());
//        checkPassword(entry);
//
//        if (entry.isDirectory())
//            extractDirectory(dstDir, entry);
//        else {
//            Path file = dstDir.resolve(entry.getFileName());
//            extractFile(file, entry);
//            // TODO should be uncommented
////            setFileAttributes(file, entry);
////            setFileLastModifiedTime(file, fileHeader);
//        }
//    }
//
//    private void checkPassword(ZipEntry entry) {
//        Encryption encryption = entry.getEncryption();
//        boolean passwordEmpty = ArrayUtils.isEmpty(entry.getPassword());
//
//        if (encryption != Encryption.OFF && passwordEmpty)
//            throw new Zip4jIncorrectPasswordException(entry.getFileName());
//    }
//
//    private static void extractDirectory(Path dstDir, ZipEntry entry) {
//        try {
//            Files.createDirectories(dstDir.resolve(entry.getFileName()));
//        } catch(IOException e) {
//            throw new Zip4jException(e);
//        }
//    }
//
//    private void extractFile(Path file, ZipEntry entry) {
//        try (InputStream in = extractEntryAsStream(entry); OutputStream out = getOutputStream(file)) {
//            IOUtils.copyLarge(in, out);
//        } catch(IOException e) {
//            throw new Zip4jException(e);
//        }
//    }


}
