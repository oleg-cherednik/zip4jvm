package ru.olegcherednik.zip4jvm.model.builders;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.exception.Zip4jException;
import ru.olegcherednik.zip4jvm.io.in.DataInput;
import ru.olegcherednik.zip4jvm.io.in.LittleEndianReadFile;
import ru.olegcherednik.zip4jvm.io.out.SplitZipOutputStream;
import ru.olegcherednik.zip4jvm.io.readers.ZipModelReader;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntry;
import ru.olegcherednik.zip4jvm.model.entry.ZipEntryBuilder;
import ru.olegcherednik.zip4jvm.model.settings.ZipFileWriterSettings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public static ZipModel read(@NonNull Path zip) throws IOException {
        return new ZipModelReader(zip).read();
    }

    public static ZipModel create(Path zip, ZipFileWriterSettings zipFileSettings) {
        if (Files.exists(zip))
            throw new Zip4jException("ZipFile '" + zip.toAbsolutePath() + "' exists");

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
        setEntrySize(zipModel);
        updateSplit(zipModel);

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

    private static void updateSplit(ZipModel zipModel) throws IOException {
        if (isSplit(zipModel))
            zipModel.setSplitSize(getSplitSize(zipModel));
    }

    private static boolean isSplit(ZipModel zipModel) throws IOException {
        try (DataInput in = new LittleEndianReadFile(zipModel.getPartFile(0))) {
            return in.readSignature() == SplitZipOutputStream.SPLIT_SIGNATURE;
        }
    }

    private static long getSplitSize(ZipModel zipModel) throws IOException {
        long size = 0;

        for (long i = 0; i <= zipModel.getTotalDisks(); i++)
            size = Math.max(size, Files.size(zipModel.getPartFile(i)));

        return size;
    }

    private static void setEntrySize(ZipModel zipModel) throws IOException {
        Map<Long, Long> discSize = getDiscSizes(zipModel);
        List<ZipEntry> entries = getEntries(zipModel);

        ZipEntry prv = null;

        for (ZipEntry entry : entries) {
            if (prv != null) {
                long size = 0;

                for (long i = prv.getDisk(); i <= entry.getDisk(); i++) {
                    if (prv.getDisk() == entry.getDisk())
                        size = entry.getLocalFileHeaderOffs() - prv.getLocalFileHeaderOffs();
                    else if (i == prv.getDisk())
                        size += discSize.get(i) - prv.getLocalFileHeaderOffs();
                    else if (i == entry.getDisk())
                        size += entry.getLocalFileHeaderOffs();
                    else
                        size += discSize.get(i);
                }

                prv.setSize(size);
            }

            prv = entry;
        }

        if (prv != null) {
            long size = 0;

            for (long i = prv.getDisk(); i <= zipModel.getMainDisk(); i++) {
                if (prv.getDisk() == zipModel.getMainDisk())
                    size = zipModel.getCentralDirectoryOffs() - prv.getLocalFileHeaderOffs();
                else if (i == prv.getDisk())
                    size += discSize.get(i) - prv.getLocalFileHeaderOffs();
                else if (i == zipModel.getMainDisk())
                    size += zipModel.getCentralDirectoryOffs();
                else
                    size += discSize.get(i);
            }

            prv.setSize(size);
        }
    }

    private static Map<Long, Long> getDiscSizes(ZipModel zipModel) throws IOException {
        Map<Long, Long> diskSize = new TreeMap<>();

        for (long i = 0; i <= zipModel.getTotalDisks(); i++)
            diskSize.put(i, Files.size(zipModel.getPartFile(i)));

        return diskSize;
    }

    private static List<ZipEntry> getEntries(ZipModel zipModel) {
        return zipModel.getEntries().stream()
                       .sorted(ZipEntry.SORT_BY_DISC_LOCAL_FILE_HEADER_OFFS)
                       .collect(Collectors.toList());
    }

}
