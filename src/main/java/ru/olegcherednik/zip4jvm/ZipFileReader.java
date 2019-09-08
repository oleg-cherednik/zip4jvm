package ru.olegcherednik.zip4jvm;

import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileReadSettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            extractEntry(destDir, entry, ZipEntry::getFileName);
    }

    public void extract(@NonNull Path destDir, @NonNull Collection<String> fileNames) throws IOException {
        for (String fileName : fileNames)
            extract(destDir, fileName);
    }

    public void extract(@NonNull Path destDir, @NonNull String fileName) throws IOException {
        fileName = ZipUtils.normalizeFileName(fileName);
        List<ZipEntry> entries = getEntriesWithFileNamePrefix(fileName + '/');

        if (entries.isEmpty())
            extractEntry(destDir, zipModel.getEntryByFileName(fileName), e -> FilenameUtils.getName(e.getFileName()));
        else {
            for (ZipEntry entry : entries)
                extractEntry(destDir, entry, ZipEntry::getFileName);
        }
    }

    private List<ZipEntry> getEntriesWithFileNamePrefix(String fileNamePrefix) {
        return zipModel.getEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(fileNamePrefix))
                       .collect(Collectors.toList());
    }

    private void extractEntry(Path destDir, ZipEntry entry, Function<ZipEntry, String> getFileName) throws IOException {
        if (entry == null)
            throw new Zip4jException("Entry not found");

        entry.setPassword(settings.getPassword().apply(entry.getFileName()));
        String fileName = getFileName.apply(entry);
        Path file = destDir.resolve(fileName);

        if (entry.isDirectory())
            Files.createDirectories(file);
        else {
            try (OutputStream out = getOutputStream(file)) {
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

    private static void checkZipFile(Path zip) {
        if (!Files.exists(zip))
            throw new Zip4jException("ZipFile not exists: " + zip);
        if (!Files.isRegularFile(zip))
            throw new Zip4jException("ZipFile is not a regular file: " + zip);
    }

}
