package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * @author Oleg Cherednik
 * @since 31.08.2019
 */
@RequiredArgsConstructor
public final class Zip64Builder {

    @NonNull
    private final ZipModel zipModel;
    private final int disk;

    public Zip64 create() {
        Zip64 zip64 = Zip64.NULL;

        if (zipModel.isZip64()) {
            Zip64.EndCentralDirectory dir = createEndCentralDirectory();
            Zip64.EndCentralDirectoryLocator locator = createLocator();
            zip64 = Zip64.of(locator, dir);
        }

        return zip64;
    }

    private Zip64.EndCentralDirectoryLocator createLocator() {
        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setOffs(zipModel.getCentralDirectoryOffs() + zipModel.getCentralDirectorySize());
        locator.setStartDisk(disk);
        locator.setTotalDisks(disk + 1);
        return locator;
    }

    private Zip64.EndCentralDirectory createEndCentralDirectory() {
        Zip64.EndCentralDirectory endCentralDirectory = new Zip64.EndCentralDirectory();
        endCentralDirectory.setSize(Zip64.EndCentralDirectory.SIZE + endCentralDirectory.getEndCentralDirectorySize());
        endCentralDirectory.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        endCentralDirectory.setVersionNeededToExtract(CentralDirectory.FileHeader.VERSION);
        endCentralDirectory.setDisk(zipModel.getTotalDisks());
        endCentralDirectory.setStartDisk(zipModel.getStartDiskNumber());
        endCentralDirectory.setDiskEntries(countNumberOfFileHeaderEntriesOnDisk());
        endCentralDirectory.setTotalEntries(zipModel.getEntries().size());
        endCentralDirectory.setSize(zipModel.getCentralDirectorySize());
        endCentralDirectory.setCentralDirectoryOffs(zipModel.getCentralDirectoryOffs());
        return endCentralDirectory;
    }

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplit())
            return (int)zipModel.getEntries().stream()
                                .filter(entry -> entry.getDisc() == zipModel.getTotalDisks())
                                .count();

        return zipModel.getEntries().size();
    }

}
