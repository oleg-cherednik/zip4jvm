package com.cop.zip4j.model.builders;

import com.cop.zip4j.io.readers.ZipModelReader;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.FileHeaderPathZipEntry;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
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
    // TODO is should not be here, get from FileHeader is utf8 or not
    @NonNull
    private final Charset charset;
    @NonNull
    private final EndCentralDirectory endCentralDirectory;
    @NonNull
    private final Zip64 zip64;
    @NonNull
    private final CentralDirectory centralDirectory;

    @NonNull
    // TODO do we really need it; we always know is is exists or not
    public static ZipModel readOrCreate(@NonNull Path zipFile, @NonNull Charset charset) throws IOException {
        return Files.exists(zipFile) ? new ZipModelReader(zipFile, charset).read() : new ZipModel(zipFile, charset);
    }

    @NonNull
    public ZipModel create() throws IOException {
        ZipModel zipModel = new ZipModel(zipFile, charset);

        if (zip64 != Zip64.NULL)
            zipModel.zip64();

        zipModel.setComment(endCentralDirectory.getComment());
        zipModel.setTotalDisks(endCentralDirectory.getTotalDisks());
        zipModel.setCentralDirectoryOffs(getCentralDirectoryOffs(endCentralDirectory, zip64));
        zipModel.setCentralDirectorySize(endCentralDirectory.getCentralDirectorySize());
        zipModel.setStartDiskNumber(endCentralDirectory.getStartDiskNumber());
        zipModel.getEntries().addAll(createEntries());

        if (zipModel.isSplit())
            zipModel.setSplitSize(getSplitSize(zipModel));

        return zipModel;
    }

    private List<PathZipEntry> createEntries() {
        return centralDirectory.getFileHeaders().stream()
                               .map(FileHeaderPathZipEntry::new)
                               .collect(Collectors.toList());
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
