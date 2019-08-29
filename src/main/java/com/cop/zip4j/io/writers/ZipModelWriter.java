package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.ZipModel;
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

        endCentralDirectory.setOffs(out.getOffs());
        endCentralDirectory.setSplitParts(out.getCounter());
        endCentralDirectory.setStartDiskNumber(out.getCounter());
        endCentralDirectory.setDiskEntries(zipModel.getEntries().size());
        endCentralDirectory.setTotalEntries(zipModel.getEntries().size());
        endCentralDirectory.setComment(zipModel.getComment());


        updateEndCentralDirectory(out);

        out.mark(MARK);
        new CentralDirectoryWriter(createCentralDirectory(), zipModel.getCharset()).write(out);

        endCentralDirectory.setSize(out.getWrittenBytesAmount(MARK));

        zipModel.getEndCentralDirectory().setSize(out.getWrittenBytesAmount(MARK));
    }

    private CentralDirectory createCentralDirectory() throws IOException {
        return new LocalCentralDirectoryBuilder().create(zipModel.getEntries(), zipModel);
    }

    private void writeZip64(DataOutput out) throws IOException {
        zipModel.updateZip64(out.getCounter());
        new Zip64Writer(zipModel.getZip64()).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        new EndCentralDirectoryWriter(endCentralDirectory, zipModel.getCharset()).write(out);
    }

    private void updateEndCentralDirectory(DataOutput out) {
        EndCentralDirectory endCentralDirectory = zipModel.getEndCentralDirectory();
        endCentralDirectory.setOffs(out.getOffs());
        endCentralDirectory.setSplitParts(out.getCounter());
        endCentralDirectory.setStartDiskNumber(out.getCounter());
    }

}
