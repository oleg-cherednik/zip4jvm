package com.cop.zip4j.io.writers;

import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.EndCentralDirectory;
import com.cop.zip4j.model.Zip64;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.utils.ZipUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class ZipModelWriter {

    private static final String MARK = "header";

    @NonNull
    private final ZipModel zipModel;

    public void write(@NonNull DataOutput out) throws IOException {
        writeCentralDirectoryHeaders(out);
        writeZip64(out);
        writeEndCentralDirectory(out);
    }

    private void writeCentralDirectoryHeaders(DataOutput out) throws IOException {
        updateEndCentralDirectory(out);
        updateFileHeaders();

        CentralDirectory dir = new LocalCentralDirectoryBuilder().create(zipModel.getEntries(), zipModel);

        out.mark(MARK);
        new CentralDirectoryWriter(zipModel.getCentralDirectory(), zipModel.getCharset()).write(out);
        zipModel.getEndCentralDirectory().setSize(out.getWrittenBytesAmount(MARK));
    }


    private void writeZip64(DataOutput out) throws IOException {
        zipModel.updateZip64(out.getCounter());
        new Zip64Writer(zipModel.getZip64()).write(out);
    }

    private void writeEndCentralDirectory(DataOutput out) throws IOException {
        EndCentralDirectory dir = zipModel.getEndCentralDirectory();
        Charset charset = zipModel.getCharset();
        new EndCentralDirectoryWriter(dir, charset).write(out);
    }

    private void updateEndCentralDirectory(DataOutput out) {
        EndCentralDirectory endCentralDirectory = zipModel.getEndCentralDirectory();
        endCentralDirectory.setOffs(out.getOffs());
        endCentralDirectory.setSplitParts(out.getCounter());
        endCentralDirectory.setStartDiskNumber(out.getCounter());
    }

    private void updateFileHeaders() {
        zipModel.getCentralDirectory().getFileHeaders().forEach(fileHeader -> {
            // TODO probably it's better to get size first and then update fileHeader
            fileHeader.getExtraField().setExtendedInfo(getExtendedInfo(fileHeader).get());
            fileHeader.setCompressedSize(getCompressedSize(fileHeader).getAsLong());
            fileHeader.setUncompressedSize(getUncompressedSize(fileHeader).getAsLong());
        });
    }

    private LongSupplier getCompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (ZipUtils.isDirectory(fileHeader.getFileName()))
            return () -> 0;
        return zipModel.getActivity().getCompressedSizeFileHeader(fileHeader::getOriginalCompressedSize);
    }

    private LongSupplier getUncompressedSize(CentralDirectory.FileHeader fileHeader) {
        if (ZipUtils.isDirectory(fileHeader.getFileName()))
            return () -> 0;
        return zipModel.getActivity().getUncompressedSizeFileHeader(fileHeader::getOriginalUncompressedSize);
    }

    private Supplier<Zip64.ExtendedInfo> getExtendedInfo(CentralDirectory.FileHeader fileHeader) {
        if (ZipUtils.isDirectory(fileHeader.getFileName()))
            return () -> Zip64.ExtendedInfo.NULL;
        return zipModel.getActivity().getExtendedInfoFileHeader(fileHeader);

    }

}
