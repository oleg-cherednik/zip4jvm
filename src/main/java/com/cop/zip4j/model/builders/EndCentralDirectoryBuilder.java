package com.cop.zip4j.model.builders;

import com.cop.zip4j.model.EndCentralDirectory;
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
        endCentralDirectory.setSplitParts(zipModel.getSplitParts());
        endCentralDirectory.setStartDiskNumber(zipModel.getSplitParts());
        endCentralDirectory.setDiskEntries(zipModel.getEntries().size());
        endCentralDirectory.setTotalEntries(zipModel.getActivity().getTotalEntriesECD(zipModel));
        endCentralDirectory.setComment(zipModel.getComment());
        endCentralDirectory.setCentralDirectorySize(zipModel.getCentralDirectorySize());
        return endCentralDirectory;
    }
}
