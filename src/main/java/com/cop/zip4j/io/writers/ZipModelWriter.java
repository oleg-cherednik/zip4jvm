package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.builders.CentralDirectoryBuilder;
import com.cop.zip4j.model.builders.EndCentralDirectoryBuilder;
import com.cop.zip4j.model.builders.Zip64Builder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public final class ZipModelWriter {

    private static final String CENTRAL_DIRECTORY_OFFS = "centralDirectoryOffs";

    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull DataOutput out) throws IOException {
        zipModel.setSplitParts(out.getDisk());
        zipModel.setCentralDirectoryOffs(out.getOffs());
        zipModel.setStartDiskNumber(out.getDisk());

        writeCentralDirectoryHeaders(out);
        writeZip64(out);
        writeEndCentralDirectory(out);
    }

    private void writeCentralDirectoryHeaders(DataOutput out) throws IOException {
        out.mark(CENTRAL_DIRECTORY_OFFS);
        CentralDirectory centralDirectory = new CentralDirectoryBuilder(zipModel.getEntries()).create();
        new CentralDirectoryWriter(centralDirectory, zipModel.getCharset()).write(out);
        zipModel.setCentralDirectorySize(out.getWrittenBytesAmount(CENTRAL_DIRECTORY_OFFS));
    }

    private void writeZip64(DataOutput out) throws IOException {
        Zip64 zip64 = new Zip64Builder(zipModel, out.getDisk()).create();
        new Zip64Writer(zip64).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectoryBuilder(zipModel).create();
        new EndCentralDirectoryWriter(endCentralDirectory, zipModel.getCharset()).write(out);
    }

}
