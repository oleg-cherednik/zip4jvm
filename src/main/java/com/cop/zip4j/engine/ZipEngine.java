package com.cop.zip4j.engine;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.DataOutputStream;
import com.cop.zip4j.io.SingleZipFileOutputStream;
import com.cop.zip4j.io.SplitOutputStream;
import com.cop.zip4j.io.entry.EntryOutputStream;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.io.OutputStream;
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

        try (DataOutputStream out = createOutputStream()) {
            entries.stream()
                   .filter(entry -> !entry.isRoot())
                   .forEach(entry -> writeEntry(entry, out));
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private DataOutputStream createOutputStream() throws IOException {
        if (zipModel.isSplitArchive())
            return SplitOutputStream.create(zipModel);
        return SingleZipFileOutputStream.create(zipModel);
    }

    private void writeEntry(@NonNull PathZipEntry entry, @NonNull DataOutputStream out) {
        try (OutputStream delegate = EntryOutputStream.create(entry, zipModel, out)) {
            entry.write(delegate);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

}
