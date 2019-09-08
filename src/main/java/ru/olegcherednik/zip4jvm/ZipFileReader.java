package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReadSettings;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public class ZipFileReader {

    private final ZipModel zipModel;
    private final ZipFileReadSettings settings;

    public ZipFileReader(@NonNull Path zip) throws IOException {
        this(zip, ZipFileReadSettings.builder().build());
    }

    public ZipFileReader(@NonNull Path zip, ZipFileReadSettings settings) throws IOException {
        checkZipFile(zip);
        zipModel = ZipModelBuilder.read(zip);
        this.settings = settings;
    }

    public void extract(@NonNull Path destDir) throws IOException {
        for (ZipEntry entry : zipModel.getEntries())
            extractEntry(destDir, entry);
    }

    private void extractEntry(Path destDir, ZipEntry entry) throws IOException {
        entry.setPassword(settings.getPassword().apply(entry.getFileName()));
        String fileName = entry.getFileName();

        if (entry.isDirectory())
            Files.createDirectories(destDir.resolve(fileName));
        else {
            try (OutputStream out = getOutputStream(destDir.resolve(fileName))) {
                entry.write(out);
            }
            // TODO should be uncommented
//            setFileAttributes(file, entry);
//            setFileLastModifiedTime(file, fileHeader);
        }
    }

    private static FileOutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(file))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);

        return new FileOutputStream(file.toFile());
    }

    static void checkZipFile(Path zip) {
        if (!Files.exists(zip))
            throw new Zip4jException("ZipFile not exists: " + zip);
        if (!Files.isRegularFile(zip))
            throw new Zip4jException("ZipFile is not a regular file: " + zip);
    }

}
