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
        zipModel.updateZip64(out.getCounter());
        new Zip64Writer(zipModel.getZip64()).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        new EndCentralDirectoryWriter(endCentralDirectory, zipModel.getCharset()).write(out);
    }

}
