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
        endCentralDirectory.setTotalDisks(getTotalDisks());
        endCentralDirectory.setMainDisk(getTotalDisks());
        endCentralDirectory.setDiskEntries(getDiskEntries());
        endCentralDirectory.setTotalEntries(getTotalEntries());
        endCentralDirectory.setCentralDirectorySize(zipModel.getCentralDirectorySize());
        endCentralDirectory.setCentralDirectoryOffs(getCentralDirectoryOffs());
        endCentralDirectory.setComment(zipModel.getComment());
        return endCentralDirectory;
    }

    private int getTotalDisks() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_DISKS : (int)zipModel.getTotalDisks();
    }

    private int getDiskEntries() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_ENTRIES : zipModel.getEntries().size();
    }

    private int getTotalEntries() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_ENTRIES : zipModel.getEntries().size();
    }

    private long getCentralDirectoryOffs() {
        return zipModel.isZip64() ? Zip64.LIMIT : zipModel.getCentralDirectoryOffs();
    }

}
