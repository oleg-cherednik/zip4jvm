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
import java.util.function.LongSupplier;
import java.util.function.Supplier;

@RequiredArgsConstructor
public final class ZipModelWriter {

    private static final String MARK = "header";

    @NonNull
    private final ZipModel zipModel;
    private final boolean validate;

    // TODO do we really need validate flag?
    public void finalizeZipFile(@NonNull DataOutput out) throws IOException {
        if (validate)
            processHeaderData(out);

        updateFileHeaders();

        EndCentralDirectory endCentralDirectory = zipModel.getEndCentralDirectory();
        out.mark(MARK);
        new CentralDirectoryWriter(zipModel.getCentralDirectory(), zipModel.getCharset()).write(out);
        endCentralDirectory.setSize(out.getWrittenBytesAmount(MARK));

        zipModel.updateZip64();

        if (zipModel.isZip64() && validate) {
            Zip64.EndCentralDirectoryLocator locator = zipModel.getZip64().getEndCentralDirectoryLocator();
            locator.setStartDisk(out.getCounter());
            locator.setTotalDisks(out.getCounter() + 1);
        }

        new Zip64Writer(zipModel.getZip64()).write(out);
        new EndCentralDirectoryWriter(endCentralDirectory, zipModel.getCharset()).write(out);
    }

    private void processHeaderData(DataOutput out) throws IOException {
        EndCentralDirectory endCentralDirectory = zipModel.getEndCentralDirectory();
        // TODO duplication set; see previous step
        endCentralDirectory.setOffs(out.getOffs());

        if (zipModel.isZip64()) {
            Zip64.EndCentralDirectoryLocator locator = zipModel.getZip64().getEndCentralDirectoryLocator();
            locator.setStartDisk(out.getCounter());
            locator.setTotalDisks(out.getCounter() + 1);
        }

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
