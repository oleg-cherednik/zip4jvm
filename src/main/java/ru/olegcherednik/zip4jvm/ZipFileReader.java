package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
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

    public ZipFileReader(Path zip, ZipFileReadSettings settings) throws IOException {
        zipModel = ZipModelBuilder.read(zip);
        this.settings = settings;
    }

    public void extract(@NonNull Path destDir) throws IOException {
        for (ZipEntry entry : zipModel.getEntries())
            extractEntry(destDir, entry);
    }

    private void extractEntry(Path destDir, ZipEntry entry) throws IOException {
        entry.setPassword(settings.getPassword());

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

}
