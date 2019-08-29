package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.activity.Zip64Activity;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public final class ZipModelWriter {

    private static final String MARK = "header";

    @NonNull
    private final ZipModel zipModel;
    private final EndCentralDirectory endCentralDirectory = new EndCentralDirectory();

    public void write(@NonNull DataOutput out) throws IOException {
        writeCentralDirectoryHeaders(out);
        writeZip64(out);
        writeEndCentralDirectory(out);
    }

    private void writeCentralDirectoryHeaders(DataOutput out) throws IOException {
        zipModel.setSplitParts(out.getCounter());
        zipModel.setCentralDirectoryOffs(out.getOffs());
        zipModel.setStartDiskNumber(out.getCounter());

        endCentralDirectory.setCentralDirectoryOffs(out.getOffs());
        endCentralDirectory.setSplitParts(out.getCounter());
        endCentralDirectory.setStartDiskNumber(out.getCounter());
        endCentralDirectory.setDiskEntries(zipModel.getEntries().size());
        endCentralDirectory.setTotalEntries(zipModel.getActivity().getTotalEntriesECD(zipModel));
        endCentralDirectory.setComment(zipModel.getComment());

        out.mark(MARK);
        new CentralDirectoryWriter(createCentralDirectory(), zipModel.getCharset()).write(out);

        zipModel.setCentralDirectorySize(out.getWrittenBytesAmount(MARK));
        endCentralDirectory.setCentralDirectorySize(out.getWrittenBytesAmount(MARK));
    }

    private CentralDirectory createCentralDirectory() throws IOException {
        return new LocalCentralDirectoryBuilder().create(zipModel.getEntries(), zipModel);
    }

    private void writeZip64(DataOutput out) throws IOException {
        Zip64 zip64 = createZip64(out.getCounter());
        new Zip64Writer(zip64).write(out);
    }

    private Zip64 createZip64(int counter) {
        if (!(zipModel.getActivity() instanceof Zip64Activity))
            return Zip64.NULL;

        Zip64.EndCentralDirectory dir = new Zip64.EndCentralDirectory();
        dir.setSize(Zip64.EndCentralDirectory.SIZE + dir.getSizeEndCentralDirectory());
        dir.setVersionMadeBy(CentralDirectory.FileHeader.VERSION);
        dir.setVersionNeededToExtract(CentralDirectory.FileHeader.VERSION);
        dir.setDisk(zipModel.getSplitParts());
        dir.setStartDisk(zipModel.getStartDiskNumber());
        dir.setDiskEntries(countNumberOfFileHeaderEntriesOnDisk());
        dir.setTotalEntries(zipModel.getEntries().size());
        dir.setSize(zipModel.getCentralDirectorySize());
        dir.setCentralDirectoryOffs(zipModel.getCentralDirectoryOffs());

        Zip64.EndCentralDirectoryLocator locator = new Zip64.EndCentralDirectoryLocator();
        locator.setOffs(zipModel.getCentralDirectoryOffs() + zipModel.getCentralDirectorySize());
        locator.setStartDisk(counter);
        locator.setTotalDisks(counter + 1);

        return Zip64.of(locator, dir);
    }

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (zipModel.isSplitArchive())
            return (int)zipModel.getEntries().stream()
                                .filter(entry -> entry.getDisc() == zipModel.getSplitParts())
                                .count();

        return zipModel.getEntries().size();
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        new EndCentralDirectoryWriter(endCentralDirectory, zipModel.getCharset()).write(out);
    }

}
