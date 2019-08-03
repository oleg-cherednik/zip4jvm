package com.cop.zip4j.engine;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.entry.EntryOutputStream;
import com.cop.zip4j.io.out.MarkDataOutput;
import com.cop.zip4j.io.out.SingleZipFileOutputStream;
import com.cop.zip4j.io.out.SplitZipFileOutputStream;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

/**
 * @author Oleg Cherednik
 * @since 17.03.2019
 */
@RequiredArgsConstructor
public class ZipEngine {

    @NonNull
    private final ZipModel zipModel;

    public void addEntries(@NonNull Collection<PathZipEntry> entries) {
        if (entries.isEmpty())
            return;

        try (MarkDataOutput out = createOutputStream()) {
            out.seek(zipModel.getOffsCentralDirectory());
            entries.stream()
                   .filter(entry -> !entry.isRoot())
                   .forEach(entry -> writeEntry(entry, out));
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private MarkDataOutput createOutputStream() throws IOException {
        Path zipFile = zipModel.getZipFile();
        Path parent = zipFile.getParent();

        if (parent != null)
            Files.createDirectories(parent);

        return zipModel.isSplitArchive() ? SplitZipFileOutputStream.create(zipModel) : SingleZipFileOutputStream.create(zipModel);
    }

    private void writeEntry(@NonNull PathZipEntry entry, @NonNull MarkDataOutput out) {
        try (OutputStream delegate = EntryOutputStream.create(entry, zipModel, out)) {
            entry.write(delegate);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

}
