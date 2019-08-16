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
        getFileHeaders(entries).forEach(fileHeader -> extractEntry(dstDir, fileHeader));
    }

    private List<CentralDirectory.FileHeader> getFileHeaders(@NonNull Collection<String> entries) {
        return entries.stream()
                      .map(entryName -> zipModel.getCentralDirectory().getFileHeadersByPrefix(entryName))
                      .flatMap(List::stream)
                      .filter(Objects::nonNull)
                      .collect(Collectors.toList());
    }

    private void extractEntry(Path dstDir, CentralDirectory.FileHeader fileHeader) {
        checkPassword(fileHeader);

        if (fileHeader.isDirectory())
            extractDirectory(dstDir, fileHeader);
        else {
            Path file = dstDir.resolve(fileHeader.getFileName());
            extractFile(file, fileHeader);
            setFileAttributes(file, fileHeader);
            setFileLastModifiedTime(file, fileHeader);
        }
    }

    private void checkPassword(CentralDirectory.FileHeader fileHeader) {
        Encryption encryption = fileHeader.getEncryption();
        boolean passwordEmpty = ArrayUtils.isEmpty(password);

        if (encryption != Encryption.OFF && passwordEmpty)
            throw new Zip4jIncorrectPasswordException(fileHeader.getFileName());
    }

    private static void extractDirectory(Path dstDir, CentralDirectory.FileHeader fileHeader) {
        try {
            Files.createDirectories(dstDir.resolve(fileHeader.getFileName()));
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private void extractFile(Path file, CentralDirectory.FileHeader fileHeader) {
        try (InputStream in = extractEntryAsStream(fileHeader); OutputStream out = getOutputStream(file)) {
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
        return extractEntryAsStream(zipModel.getCentralDirectory().getFileHeaderByEntryName(entryName));
    }

    @NonNull
    private InputStream extractEntryAsStream(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        DataInput in = createInputStream(fileHeader);
        return EntryInputStream.create(fileHeader, password, in);
    }

    private DataInput createInputStream(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        return zipModel.isSplitArchive() ? SplitZipInputStream.create(zipModel, fileHeader.getDiskNumber()) : SingleZipInputStream.create(zipModel);
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
