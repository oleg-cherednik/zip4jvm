package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 31.08.2019
 */
@RequiredArgsConstructor
public final class EndCentralDirectoryBuilder {

    @NonNull
    private final ZipModel zipModel;

    @NonNull
    public EndCentralDirectory create() {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
        endCentralDirectory.setCentralDirectoryOffs(zipModel.getCentralDirectoryOffs());
        endCentralDirectory.setTotalDisks(zipModel.getTotalDisks());
        endCentralDirectory.setStartDiskNumber(zipModel.getTotalDisks());
        endCentralDirectory.setDiskEntries(zipModel.getEntries().size());
        endCentralDirectory.setTotalEntries(getTotalEntries());
        endCentralDirectory.setComment(zipModel.getComment());
        endCentralDirectory.setCentralDirectorySize(zipModel.getCentralDirectorySize());
        return endCentralDirectory;
    }

    private int getTotalEntries() {
        return zipModel.isZip64() ? Zip64.LIMIT_INT : zipModel.getEntries().size();
    }
}
