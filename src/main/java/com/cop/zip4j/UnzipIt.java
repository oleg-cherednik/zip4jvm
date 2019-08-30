package com.cop.zip4j;

import lombok.Builder;
import lombok.NonNull;
import com.cop.zip4j.engine.UnzipEngine;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.CreateZipModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
    private final Charset charset = StandardCharsets.UTF_8;
    private final char[] password;

    public void extract(@NonNull Path dstDir) throws IOException {
        checkZipFile(zipFile);
        checkOutputFolder(dstDir);

        ZipModel zipModel = new CreateZipModel(zipFile, charset).get();
        zipModel.getEntries().forEach(entry -> entry.setPassword(password));
        new UnzipEngine(zipModel, password).extractEntries(dstDir, zipModel.getEntryNames());
    }

    public void extract(@NonNull Path dstDir, @NonNull String entryName) throws IOException {
        extract(dstDir, Collections.singleton(entryName));
    }

    public void extract(@NonNull Path dstDir, @NonNull Collection<String> entries) throws IOException {
        checkZipFile(zipFile);
        checkOutputFolder(dstDir);

        ZipModel zipModel = new CreateZipModel(zipFile, charset).get();
        zipModel.getEntries().forEach(entry -> entry.setPassword(password));
        new UnzipEngine(zipModel, password).extractEntries(dstDir, entries);
    }

    public InputStream extract(@NonNull String entryName) throws IOException {
        ZipModel zipModel = new CreateZipModel(zipFile, charset).get();
        zipModel.getEntries().forEach(entry -> entry.setPassword(password));
        return new UnzipEngine(zipModel, password).extractEntry(entryName);
    }

    static void checkZipFile(Path zipFile) {
        if (!Files.exists(zipFile))
            throw new Zip4jException("ZipFile not exists: " + zipFile);
        if (!Files.isRegularFile(zipFile))
            throw new Zip4jException("ZipFile is not a regular file: " + zipFile);
    }

    static void checkOutputFolder(@NonNull Path dir) {

//        if(!Files.isDirectory(dir))
//            throw new ZipException("dstination path is not a directory: " + dir);
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
//                    throw new ZipException("no write access to dstination folder");
//                }
//
////				SecurityManager manager = new SecurityManager();
////				try {
////					manager.checkWrite(file.getAbsolutePath());
////				} catch (Exception e) {
////					e.printStackTrace();
////					throw new ZipException("no write access to dstination folder");
////				}
//            } catch(Exception e) {
//                throw new ZipException("Cannot create dstination folder");
//            }
//        }
    }

}
