package net.lingala.zip4j;

import lombok.Builder;
import lombok.NonNull;
import net.lingala.zip4j.engine.UnzipEngine;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipModel;
import net.lingala.zip4j.utils.CreateZipModelSup;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@Builder
public class UnzipIt {

    @NonNull
    private final Path zipFile;
    @NonNull
    @Builder.Default
    private final Charset charset = Charset.defaultCharset();
    private final char[] password;

    public void extract(@NonNull Path destDir) throws IOException {
        checkZipFile(zipFile);
        checkOutputFolder(destDir);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();
        new UnzipEngine(zipModel, password).extractEntries(destDir, zipModel.getEntryNames());
    }

    public void extract(@NonNull Path destDir, @NonNull String entryName) throws IOException {
        extract(destDir, Collections.singleton(entryName));
    }

    public void extract(@NonNull Path destDir, @NonNull Collection<String> entries) throws IOException {
        checkZipFile(zipFile);
        checkOutputFolder(destDir);

        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();
        new UnzipEngine(zipModel, password).extractEntries(destDir, entries);
    }

    public InputStream extract(@NonNull String entryName) throws IOException {
        ZipModel zipModel = new CreateZipModelSup(zipFile, charset).get();
        return new UnzipEngine(zipModel, password).extractEntry(entryName);
    }

    static void checkZipFile(Path zipFile) {
        if (!Files.exists(zipFile))
            throw new ZipException("ZipFile not exists: " + zipFile);
        if (!Files.isRegularFile(zipFile))
            throw new ZipException("ZipFile is not a regular file: " + zipFile);
    }

    static void checkOutputFolder(@NonNull Path dir) {

//        if(!Files.isDirectory(dir))
//            throw new ZipException("Destination path is not a directory: " + dir);
//
//        if (Files.exists(dir)) {
//
//            if (!file.isDirectory()) {
//                throw new ZipException("output folder is not valid");
//            }
//
//            if (!file.canWrite()) {
//                throw new ZipException("no write access to output folder");
//            }
//        } else {
//            try {
//                file.mkdirs();
//                if (!file.isDirectory()) {
//                    throw new ZipException("output folder is not valid");
//                }
//
//                if (!file.canWrite()) {
//                    throw new ZipException("no write access to destination folder");
//                }
//
////				SecurityManager manager = new SecurityManager();
////				try {
////					manager.checkWrite(file.getAbsolutePath());
////				} catch (Exception e) {
////					e.printStackTrace();
////					throw new ZipException("no write access to destination folder");
////				}
//            } catch(Exception e) {
//                throw new ZipException("Cannot create destination folder");
//            }
//        }
    }
}
