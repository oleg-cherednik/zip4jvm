package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.FileHeaderPathZipEntry;
import com.cop.zip4j.model.entry.PathZipEntry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.nio.charset.Charset;
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
    public ZipModel create() {
        ZipModel zipModel = new ZipModel(zipFile, charset);

        if (zip64 != Zip64.NULL)
            zipModel.zip64();

        zipModel.setComment(endCentralDirectory.getComment());
        zipModel.setSplitParts(endCentralDirectory.getSplitParts());
        zipModel.setCentralDirectoryOffs(getCentralDirectoryOffs(endCentralDirectory, zip64));
        zipModel.setCentralDirectorySize(endCentralDirectory.getCentralDirectorySize());
        zipModel.setStartDiskNumber(endCentralDirectory.getStartDiskNumber());
        zipModel.getEntries().addAll(createEntries());

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
}
