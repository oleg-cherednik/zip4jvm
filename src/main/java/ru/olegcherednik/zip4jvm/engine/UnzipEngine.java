package ru.olegcherednik.zip4jvm.engine;

import org.apache.commons.io.FilenameUtils;
import ru.olegcherednik.zip4jvm.ZipFile;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.ZipModelBuilder;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.settings.UnzipSettings;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 07.09.2019
 */
public final class UnzipEngine implements ZipFile.Reader {

    private final ZipModel zipModel;
    private final UnzipSettings settings;

    public UnzipEngine(Path zip, UnzipSettings settings) throws IOException {
        checkZipFile(zip);
        zipModel = ZipModelBuilder.read(zip, settings.getCharsetCustomizer());
        this.settings = settings;
    }

    @Override
    public void extract(Path destDir) throws IOException {
        for (ZipEntry entry : zipModel.getEntries())
            extractEntry(destDir, entry, ZipEntry::getFileName);
    }

    @Override
    @SuppressWarnings("PMD.AvoidReassigningParameters")
    public void extract(Path destDir, String fileName) throws IOException {
        fileName = ZipUtils.getFileNameNoDirectoryMarker(fileName);

        if (zipModel.hasEntry(fileName))
            extractEntry(destDir, zipModel.getEntryByFileName(fileName), e -> FilenameUtils.getName(e.getFileName()));
        else {
            List<ZipEntry> subEntries = getEntriesWithFileNamePrefix(fileName + '/');

            if (subEntries.isEmpty())
                throw new Zip4jvmException("Zip entry not found: " + fileName);

            for (ZipEntry zipEntry : subEntries)
                extractEntry(destDir, zipEntry, ZipEntry::getFileName);
        }
    }

    private List<ZipEntry> getEntriesWithFileNamePrefix(String fileNamePrefix) {
        return zipModel.getEntries().stream()
                       .filter(entry -> entry.getFileName().startsWith(fileNamePrefix))
                       .collect(Collectors.toList());
    }

    @Override
    public ZipFile.Entry extract(String fileName) throws IOException {
        ZipEntry zipEntry = zipModel.getEntryByFileName(ZipUtils.normalizeFileName(fileName));

        if (zipEntry == null)
            throw new FileNotFoundException("Entry '" + fileName + "' was not found");

        zipEntry.setPassword(settings.getPasswordProvider().apply(zipEntry.getFileName()));
        return zipEntry.createImmutableEntry();
    }

    @Override
    public String getComment() {
        return zipModel.getComment();
    }

    @Override
    public boolean isSplit() {
        return zipModel.isSplit();
    }

    @Override
    public boolean isZip64() {
        return zipModel.isZip64();
    }

    @Override
    public Iterator<ZipFile.Entry> iterator() {
        return new Iterator<ZipFile.Entry>() {
            private final Iterator<String> it = zipModel.getEntryNames().iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ZipFile.Entry next() {
                return zipModel.getEntryByFileName(it.next()).createImmutableEntry();
            }
        };
    }

    private void extractEntry(Path destDir, ZipEntry zipEntry, Function<ZipEntry, String> getFileName) throws IOException {
        String fileName = getFileName.apply(zipEntry);
        Path file = destDir.resolve(fileName);

        if (zipEntry.isDirectory())
            Files.createDirectories(file);
        else {
            zipEntry.setPassword(settings.getPasswordProvider().apply(ZipUtils.getFileNameNoDirectoryMarker(zipEntry.getFileName())));
            ZipUtils.copyLarge(zipEntry.getInputStream(), getOutputStream(file));
        }

        setFileAttributes(file, zipEntry);
        setFileLastModifiedTime(file, zipEntry);
    }

    private static void setFileLastModifiedTime(Path path, ZipEntry zipEntry) {
        try {
            long lastModifiedTime = ZipUtils.dosToJavaTime(zipEntry.getLastModifiedTime());
            Files.setLastModifiedTime(path, FileTime.fromMillis(lastModifiedTime));
        } catch(IOException ignored) {
        }
    }

    private static void setFileAttributes(Path path, ZipEntry zipEntry) {
        try {
            zipEntry.getExternalFileAttributes().apply(path);
        } catch(IOException ignored) {
        }
    }

    private static FileOutputStream getOutputStream(Path file) throws IOException {
        Path parent = file.getParent();

        if (!Files.exists(file))
            Files.createDirectories(parent);

        Files.deleteIfExists(file);

        return new FileOutputStream(file.toFile());
    }

    private static void checkZipFile(Path zip) throws IOException {
        if (!Files.exists(zip))
            throw new FileNotFoundException("ZipFile not exists: " + zip);
        if (!Files.isRegularFile(zip))
            throw new IOException("ZipFile is not a regular file: " + zip);
    }

}
