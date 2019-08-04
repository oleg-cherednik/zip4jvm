package com.cop.zip4j.engine;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.in.DataInput;
import com.cop.zip4j.io.in.SingleZipInputStream;
import com.cop.zip4j.io.in.SplitZipInputStream;
import com.cop.zip4j.io.in.entry.EntryInputStream;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public void extractEntries(@NonNull Path destDir, @NonNull Collection<String> entries) {
        getFileHeaders(entries).forEach(fileHeader -> extractEntry(destDir, fileHeader));
    }

    private List<CentralDirectory.FileHeader> getFileHeaders(@NonNull Collection<String> entries) {
        return entries.stream()
                      .map(entryName -> zipModel.getCentralDirectory().getFileHeadersByPrefix(entryName))
                      .flatMap(List::stream)
                      .filter(Objects::nonNull)
                      .collect(Collectors.toList());
    }

    private void extractEntry(Path destDir, CentralDirectory.FileHeader fileHeader) {
        if (fileHeader.isDirectory())
            extractDirectory(destDir, fileHeader);
        else
            extractFile(destDir, fileHeader);
    }

    private static void extractDirectory(Path destDir, CentralDirectory.FileHeader fileHeader) {
        try {
            Files.createDirectories(destDir.resolve(fileHeader.getFileName()));
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private void extractFile(Path destDir, CentralDirectory.FileHeader fileHeader) {
        try (InputStream in = extractEntryAsStream(fileHeader); OutputStream out = getOutputStream(destDir, fileHeader)) {
            IOUtils.copyLarge(in, out);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    @NonNull
    public InputStream extractEntry(@NonNull String entryName) {
        return extractEntryAsStream(zipModel.getCentralDirectory().getFileHeaderByEntryName(entryName));
    }

    @NonNull
    private InputStream extractEntryAsStream(@NonNull CentralDirectory.FileHeader fileHeader) {
        try {
            DataInput in = createInputStream(fileHeader);
            return EntryInputStream.create(fileHeader, password, in, zipModel);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private DataInput createInputStream(@NonNull CentralDirectory.FileHeader fileHeader) throws IOException {
        return zipModel.isSplitArchive() ? SplitZipInputStream.create(zipModel, fileHeader.getDiskNumber()) : SingleZipInputStream.create(zipModel);
    }

    private static FileOutputStream getOutputStream(@NonNull Path destDir, @NonNull CentralDirectory.FileHeader fileHeader) {
        try {
            Path file = destDir.resolve(fileHeader.getFileName());
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
