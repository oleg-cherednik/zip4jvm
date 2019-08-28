package com.cop.zip4j.engine;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.exception.Zip4jIncorrectPasswordException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.io.in.SingleZipInputStream;
import com.cop.zip4j.io.in.SplitZipInputStream;
import com.cop.zip4j.io.in.entry.EntryInputStream;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Encryption;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import com.cop.zip4j.utils.ZipUtils;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;

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

    public void extractEntries(@NonNull Path dstDir, @NonNull Collection<String> entries) {
        getEntries(entries).forEach(entry -> extractEntry(dstDir, entry));
    }

    private List<PathZipEntry> getEntries(@NonNull Collection<String> entries) {
        return entries.stream()
                      .map(prefix -> {
                          String name = ZipUtils.normalizeFileName.apply(prefix.toLowerCase());

                          return zipModel.getEntries().stream()
                                         .filter(entry -> entry.getName().toLowerCase().startsWith(name))
                                         .collect(Collectors.toList());
                      })
                      .flatMap(List::stream)
                      .filter(Objects::nonNull)
                      .collect(Collectors.toList());
    }

    private void extractEntry(Path dstDir, PathZipEntry entry) {
        checkPassword(entry);

        if (entry.isDirectory())
            extractDirectory(dstDir, entry);
        else {
            Path file = dstDir.resolve(entry.getName());
            extractFile(file, entry);
            // TODO should be uncommented
//            setFileAttributes(file, entry);
//            setFileLastModifiedTime(file, fileHeader);
        }
    }

    private void checkPassword(PathZipEntry entry) {
        Encryption encryption = entry.getEncryption();
        boolean passwordEmpty = ArrayUtils.isEmpty(password);

        if (encryption != Encryption.OFF && passwordEmpty)
            throw new Zip4jIncorrectPasswordException(entry.getName());
    }

    private static void extractDirectory(Path dstDir, PathZipEntry entry) {
        try {
            Files.createDirectories(dstDir.resolve(entry.getName()));
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private void extractFile(Path file, PathZipEntry entry) {
        try (InputStream in = extractEntryAsStream(entry); OutputStream out = getOutputStream(file)) {
            IOUtils.copyLarge(in, out);
        } catch(IOException e) {
            throw new Zip4jException(e);
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
        PathZipEntry en = zipModel.getEntries().stream()
                                  .filter(entry -> entry.getName().equalsIgnoreCase(entryName))
                                  .findFirst().orElseThrow(() -> new Zip4jException("File header with entry name '" + entryName + "' was not found"));

        return extractEntryAsStream(en);
    }

    @NonNull
    private InputStream extractEntryAsStream(@NonNull PathZipEntry entry) throws IOException {
        DataInput in = zipModel.isSplitArchive() ? SplitZipInputStream.create(zipModel, entry.getDisc()) : SingleZipInputStream.create(zipModel);
        return EntryInputStream.create(entry, password, in);
    }

    private static FileOutputStream getOutputStream(Path file) {
        try {
            Path parent = file.getParent();

            if (!Files.exists(file))
                Files.createDirectories(parent);

            Files.deleteIfExists(file);

            return new FileOutputStream(file.toFile());
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

}
