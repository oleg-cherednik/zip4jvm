package ru.olegcherednik.zip4jvm.engine;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmException;
import ru.olegcherednik.zip4jvm.exception.Zip4jvmIncorrectPasswordException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.SingleZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.SplitZipInputStream;
import ru.olegcherednik.zip4jvm.io.in.entry.EntryInputStream;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Encryption;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.utils.ZipUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 14.03.2019
 */
@RequiredArgsConstructor
public class UnzipEngine {

    @Getter
    @NonNull
    private final ZipModel zipModel;
    private final char[] password;

    // TODO extract and get entries should be merged
    public void extractEntries(@NonNull Path dstDir, @NonNull Collection<String> entries) {
        getEntries(entries).forEach(entry -> extractEntry(dstDir, entry));
    }

    private List<ZipEntry> getEntries(@NonNull Collection<String> entries) {
        return entries.parallelStream()
                      .map(prefix -> {
                          String name = ZipUtils.normalizeFileName(prefix.toLowerCase());

                          return zipModel.getEntries().stream()
                                         .filter(entry -> entry.getFileName().toLowerCase().startsWith(name))
                                         .collect(Collectors.toList());
                      })
                      .flatMap(List::stream)
                      .filter(Objects::nonNull)
                      .collect(Collectors.toList());
    }

    private void extractEntry(Path dstDir, ZipEntry entry) {
        checkPassword(entry);

        if (entry.isDirectory())
            extractDirectory(dstDir, entry);
        else {
            Path file = dstDir.resolve(entry.getFileName());
            extractFile(file, entry);
            // TODO should be uncommented
//            setFileAttributes(file, entry);
//            setFileLastModifiedTime(file, fileHeader);
        }
    }

    private void checkPassword(ZipEntry entry) {
        Encryption encryption = entry.getEncryption();
        boolean passwordEmpty = ArrayUtils.isEmpty(password);

        if (encryption != Encryption.OFF && passwordEmpty)
            throw new Zip4jvmIncorrectPasswordException(entry.getFileName());
    }

    private static void extractDirectory(Path dstDir, ZipEntry entry) {
        try {
            Files.createDirectories(dstDir.resolve(entry.getFileName()));
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private void extractFile(Path file, ZipEntry entry) {
        try (InputStream in = extractEntryAsStream(entry); OutputStream out = getOutputStream(file)) {
            IOUtils.copyLarge(in, out);
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

    private static void setFileAttributes(Path file, CentralDirectory.FileHeader fileHeader) {
        fileHeader.getExternalFileAttributes().accept(file);
    }

    private static void setFileLastModifiedTime(Path file, CentralDirectory.FileHeader fileHeader) {
        try {
            int lastModifiedTime = fileHeader.getLastModifiedTime();

            if (lastModifiedTime > 0 && Files.exists(file))
                Files.setLastModifiedTime(file, FileTime.fromMillis(ZipUtils.dosToJavaTme(lastModifiedTime)));
        } catch(IOException ignored) {
        }
    }

    @NonNull
    public InputStream extractEntry(@NonNull String entryName) throws IOException {
        ZipEntry en = zipModel.getEntries().stream()
                              .filter(entry -> entry.getFileName().equalsIgnoreCase(entryName))
                              .findFirst().orElseThrow(() -> new Zip4jvmException("File header with entry name '" + entryName + "' was not found"));

        return extractEntryAsStream(en);
    }

    @NonNull
    private InputStream extractEntryAsStream(@NonNull ZipEntry entry) throws IOException {
        DataInput in = zipModel.isSplit() ? SplitZipInputStream.create(zipModel, entry.getDisk()) : SingleZipInputStream.create(zipModel);
        return EntryInputStream.create(entry, in);
    }

    private static FileOutputStream getOutputStream(Path file) {
        try {
            Path parent = file.getParent();

            if (!Files.exists(file))
                Files.createDirectories(parent);

            Files.deleteIfExists(file);

            return new FileOutputStream(file.toFile());
        } catch(IOException e) {
            throw new Zip4jvmException(e);
        }
    }

}
