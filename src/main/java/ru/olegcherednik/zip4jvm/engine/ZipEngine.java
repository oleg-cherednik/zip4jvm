package ru.olegcherednik.zip4jvm.engine;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.io.out.SingleZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.io.out.entry.EntryOutputStream;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;

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

    public void addEntries(@NonNull Collection<ZipEntry> entries) {
        if (entries.isEmpty())
            return;

        updateZip64(entries);

        try (DataOutput out = createDataOutput(zipModel)) {
            entries.stream()
                   .filter(entry -> !entry.isRoot())
                   .forEach(entry -> writeEntry(entry, out, zipModel));
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private void updateZip64(Collection<ZipEntry> entries) {
        if (zipModel.getEntries().size() + entries.size() > ZipModel.MAX_TOTAL_ENTRIES)
            zipModel.setZip64(true);
    }

    public static DataOutput createDataOutput(ZipModel zipModel) throws IOException {
        Path parent = zipModel.getZip().getParent();

        if (parent != null)
            Files.createDirectories(parent);

        return zipModel.isSplit() ? SplitZipOutputStream.create(zipModel) : SingleZipOutputStream.create(zipModel);
    }

    public static void writeEntry(ZipEntry entry, DataOutput out, ZipModel zipModel) {
        try (OutputStream os = EntryOutputStream.create(entry, zipModel, out)) {
            entry.write(os);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

}
