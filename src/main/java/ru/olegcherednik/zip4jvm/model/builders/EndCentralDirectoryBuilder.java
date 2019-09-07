package ru.olegcherednik.zip4jvm.model.builders;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;

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
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_ENTRIES : zipModel.getTotalEntries();
    }

    private int getTotalEntries() {
        return zipModel.isZip64() ? ZipModel.MAX_TOTAL_ENTRIES : zipModel.getTotalEntries();
    }

    private long getCentralDirectoryOffs() {
        return zipModel.isZip64() ? Zip64.LIMIT : zipModel.getCentralDirectoryOffs();
    }

}
