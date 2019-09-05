package ru.olegcherednik.zip4jvm.io.writers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.olegcherednik.zip4jvm.io.out.DataOutput;
import ru.olegcherednik.zip4jvm.model.CentralDirectory;
import ru.olegcherednik.zip4jvm.model.EndCentralDirectory;
import ru.olegcherednik.zip4jvm.model.Zip64;
import ru.olegcherednik.zip4jvm.model.ZipModel;
import ru.olegcherednik.zip4jvm.model.builders.CentralDirectoryBuilder;
import ru.olegcherednik.zip4jvm.model.builders.EndCentralDirectoryBuilder;
import ru.olegcherednik.zip4jvm.model.builders.Zip64Builder;

import java.io.IOException;

@RequiredArgsConstructor
public final class ZipModelWriter {

    private static final String CENTRAL_DIRECTORY_OFFS = "centralDirectoryOffs";

    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull DataOutput out) throws IOException {
        zipModel.setTotalDisks(out.getDisk());
        zipModel.setCentralDirectoryOffs(out.getOffs());
        zipModel.setMainDisk(out.getDisk());

        if (zipModel.getTotalDisks() > ZipModel.MAX_TOTAL_DISKS)
            zipModel.setZip64(true);

        writeCentralDirectoryHeaders(out);
        // TODO see 4.4.1.5 - these sections must be on the same disk (probably add function to block the split)
        writeZip64(out);
        writeEndCentralDirectory(out);
    }

    private void writeCentralDirectoryHeaders(DataOutput out) throws IOException {
        out.mark(CENTRAL_DIRECTORY_OFFS);
        CentralDirectory centralDirectory = new CentralDirectoryBuilder(zipModel.getEntries()).create();
        new CentralDirectoryWriter(centralDirectory).write(out);
        zipModel.setCentralDirectorySize(out.getWrittenBytesAmount(CENTRAL_DIRECTORY_OFFS));
    }

    private void writeZip64(DataOutput out) throws IOException {
        Zip64 zip64 = new Zip64Builder(zipModel, out.getDisk()).create();
        new Zip64Writer(zip64).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        EndCentralDirectory endCentralDirectory = new EndCentralDirectoryBuilder(zipModel).create();
        new EndCentralDirectoryWriter(endCentralDirectory).write(out);
    }

}
