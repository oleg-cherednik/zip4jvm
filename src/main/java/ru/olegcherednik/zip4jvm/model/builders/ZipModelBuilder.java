package ru.olegcherednik.zip4jvm.model.builders;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jZipFileSettingsNotSetException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@RequiredArgsConstructor
public final class ZipModelBuilder {

    @NonNull
    private final Path zip;
    @NonNull
    private final EndCentralDirectory endCentralDirectory;
    @NonNull
    private final Zip64 zip64;
    @NonNull
    private final CentralDirectory centralDirectory;

    public static ZipModel read(Path zip) throws IOException {
        return new ZipModelReader(zip).read();
    }

    public static ZipModel readOrCreate(Path zip, ZipFileWriterSettings zipFileSettings) throws IOException {
        if (Files.exists(zip))
            return new ZipModelReader(zip).read();
        if (zipFileSettings == null)
            throw new Zip4jZipFileSettingsNotSetException(zip);

        ZipModel zipModel = new ZipModel(zip);
        zipModel.setSplitSize(zipFileSettings.getSplitSize());
        zipModel.setComment(zipFileSettings.getComment());
        zipModel.setZip64(zipFileSettings.isZip64());

        return zipModel;
    }

    @NonNull
    // TODO do we really need it; we always know is it exists or not
    public static ZipModel readOrCreate(@NonNull Path zipFile) throws IOException {
        return Files.exists(zipFile) ? new ZipModelReader(zipFile).read() : new ZipModel(zipFile);
    }

    @NonNull
    public ZipModel create() throws IOException {
        ZipModel zipModel = new ZipModel(zip);

        zipModel.setZip64(zip64 != Zip64.NULL);
        zipModel.setComment(endCentralDirectory.getComment());
        zipModel.setTotalDisks(getTotalDisks());
        zipModel.setMainDisk(getMainDisks());
        zipModel.setCentralDirectoryOffs(getCentralDirectoryOffs(endCentralDirectory, zip64));
        zipModel.setCentralDirectorySize(endCentralDirectory.getCentralDirectorySize());
        createAndAddEntries(zipModel);

        if (zipModel.isSplit())
            zipModel.setSplitSize(getSplitSize(zipModel));

        return zipModel;
    }

    private void createAndAddEntries(ZipModel zipModel) {
        centralDirectory.getFileHeaders().stream()
                        .map(fileHeader -> ZipEntryBuilder.create(fileHeader, zipModel))
                        .forEach(zipModel::addEntry);
    }

    private long getTotalDisks() {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getTotalDisks();
        return zip64.getEndCentralDirectoryLocator().getTotalDisks();
    }

    private long getMainDisks() {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getMainDisk();
        return zip64.getEndCentralDirectoryLocator().getMainDisk();
    }

    public static long getCentralDirectoryOffs(@NonNull EndCentralDirectory endCentralDirectory, @NonNull Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getCentralDirectoryOffs();
        return zip64.getEndCentralDirectory().getCentralDirectoryOffs();
    }

    public static long getTotalEntries(@NonNull EndCentralDirectory endCentralDirectory, @NonNull Zip64 zip64) {
        if (zip64 == Zip64.NULL)
            return endCentralDirectory.getTotalEntries();
        return zip64.getEndCentralDirectory().getTotalEntries();
    }

    private static long getSplitSize(ZipModel zipModel) throws IOException {
        long size = 0;

        for (long i = 0; i <= zipModel.getTotalDisks(); i++)
            size = Math.max(size, Files.size(zipModel.getPartFile(i)));

        return size;
    }

}
