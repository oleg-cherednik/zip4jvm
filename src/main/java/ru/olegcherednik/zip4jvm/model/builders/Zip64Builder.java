package ru.olegcherednik.zip4jvm.model.builders;

import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;

/**
 * @author Oleg Cherednik
 * @since 31.08.2019
 */
@RequiredArgsConstructor
public final class Zip64Builder {

    private final ZipModel zipModel;
    private final long disk;

    public Zip64 build() {
        return zipModel.isZip64() ? Zip64.of(createLocator(), createEndCentralDirectory()) : Zip64.NULL;
    }

    private Zip64.EndCentralDirectoryLocator createLocator() {
        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setOffs(zipModel.getCentralDirectoryOffs() + zipModel.getCentralDirectorySize());
        locator.setMainDisk(disk);
        locator.setTotalDisks(disk);
        return locator;
    }

    private Zip64.EndCentralDirectory createEndCentralDirectory() {
        Zip64.EndCentralDirectory endCentralDirectory = new Zip64.EndCentralDirectory();
        endCentralDirectory.setEndCentralDirectorySize(getEndCentralDirectorySize());
        endCentralDirectory.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        endCentralDirectory.setVersionToExtract(CentralDirectory.FileHeader.VERSION);
        endCentralDirectory.setTotalDisks(zipModel.getTotalDisks());
        endCentralDirectory.setMainDisk(zipModel.getMainDisk());
        endCentralDirectory.setDiskEntries(countNumberOfFileHeaderEntriesOnDisk());
        endCentralDirectory.setTotalEntries(zipModel.getTotalEntries());
        endCentralDirectory.setCentralDirectorySize(zipModel.getCentralDirectorySize());
        endCentralDirectory.setCentralDirectoryOffs(zipModel.getCentralDirectoryOffs());
        endCentralDirectory.setExtensibleDataSector(new byte[getExtensibleDataSectorSize()]);
        return endCentralDirectory;
    }

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplit())
            return (int)zipModel.getEntries().stream()
                                .filter(entry -> entry.getDisk() == zipModel.getTotalDisks())
                                .count();

        return zipModel.getTotalEntries();
    }

    /** see 4.3.14.1 */
    private static long getEndCentralDirectorySize() {
        return Zip64.EndCentralDirectory.SIZE + getExtensibleDataSectorSize();
    }

    /** see 4.4.27 */
    private static int getExtensibleDataSectorSize() {
        return 0;
    }

}
