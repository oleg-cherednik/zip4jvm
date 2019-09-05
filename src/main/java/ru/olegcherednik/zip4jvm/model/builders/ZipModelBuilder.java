package ru.olegcherednik.zip4jvm.model.builders;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jZipFileSettingsNotSetException;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipFileSettings;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Oleg Cherednik
 * @since 01.09.2019
 */
@RequiredArgsConstructor
public final class ZipModelBuilder {

    @NonNull
    private final Path zipFile;
    @NonNull
    private final EndCentralDirectory endCentralDirectory;
    @NonNull
    private final Zip64 zip64;
    @NonNull
    private final CentralDirectory centralDirectory;

    public static ZipModel readOrCreate(Path file, ZipFileSettings zipFileSettings) throws IOException {
        if (Files.exists(file))
            return new ZipModelReader(file).read();

        if(zipFileSettings == null)
            throw new Zip4jZipFileSettingsNotSetException(file);

        ZipModel zipModel = new ZipModel(file);
        zipModel.setSplitSize(zipFileSettings.getSplitSize());
        zipModel.setComment(zipFileSettings.getComment());
        zipModel.setZip64(zipFileSettings.isZip64());

//        private final ZipEntrySettings entrySettings;

        return zipModel;
    }

    @NonNull
    // TODO do we really need it; we always know is it exists or not
    public static ZipModel readOrCreate(@NonNull Path zipFile) throws IOException {
        return Files.exists(zipFile) ? new ZipModelReader(zipFile).read() : new ZipModel(zipFile);
    }

    @NonNull
    public ZipModel create() throws IOException {
        ZipModel zipModel = new ZipModel(zipFile);

        zipModel.setZip64(zip64 != Zip64.NULL);
        zipModel.setComment(endCentralDirectory.getComment());
        zipModel.setTotalDisks(getTotalDisks());
        zipModel.setMainDisk(getMainDisks());
        zipModel.setCentralDirectoryOffs(getCentralDirectoryOffs(endCentralDirectory, zip64));
        zipModel.setCentralDirectorySize(endCentralDirectory.getCentralDirectorySize());
        zipModel.getEntries().addAll(createEntries());

        if (zipModel.isSplit())
            zipModel.setSplitSize(getSplitSize(zipModel));

        return zipModel;
    }

    private List<ZipEntry> createEntries() {
        return centralDirectory.getFileHeaders().stream()
                               .map(ZipEntryBuilder::create)
                               .collect(Collectors.toList());
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
