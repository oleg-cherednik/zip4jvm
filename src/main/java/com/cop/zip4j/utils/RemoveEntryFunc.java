package com.cop.zip4j.utils;

import com.cop.zip4j.core.writers.ZipModelWriter;
import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutputStreamDecorator;
import com.cop.zip4j.io.out.MarkDataOutput;
import com.cop.zip4j.io.out.SingleZipFileOutputStream;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public final class RemoveEntryFunc implements Consumer<Collection<String>> {

    @NonNull
    private final ZipModel zipModel;

    public void accept(@NonNull String entryName) {
        accept(Collections.singleton(entryName));
    }

    @Override
    public void accept(@NonNull Collection<String> entries) {
        entries = getExistedEntries(entries);

        if (entries.isEmpty())
            return;

        Path tmpZipFile = createTempFile();

        try (MarkDataOutput out = SingleZipFileOutputStream.create(tmpZipFile, zipModel)) {
            writeFileHeaders(new DataOutputStreamDecorator(out), entries);
            new ZipModelWriter(zipModel).finalizeZipFile(out, true);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }

        restoreFileName(tmpZipFile);
    }

    private Set<String> getExistedEntries(Collection<String> entries) {
        return entries.stream()
                      .filter(Objects::nonNull)
                      .map(entryName -> zipModel.getCentralDirectory().getFileHeadersByEntryName(entryName))
                      .flatMap(List::stream)
                      .map(CentralDirectory.FileHeader::getFileName)
                      .collect(Collectors.toSet());
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile(zipModel.getZipFile().getParent(), null, ".zip");
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }

    private void writeFileHeaders(OutputStream out, Collection<String> entries) throws IOException {
        List<CentralDirectory.FileHeader> fileHeaders = new ArrayList<>();
        CentralDirectory.FileHeader prv = null;

        long offsIn = 0;
        long offsOut = 0;
        long skip = 0;

        try (InputStream in = new FileInputStream(zipModel.getZipFile().toFile())) {
            for (CentralDirectory.FileHeader header : zipModel.getFileHeaders()) {
                if (prv != null) {
                    long curOffs = offsOut;
                    long length = header.getOffsLocalFileHeader() - prv.getOffsLocalFileHeader();
                    offsIn += skip + IOUtils.copyLarge(in, out, skip, length);
                    offsOut += length;
                    fileHeaders.add(prv);
                    prv.setOffsLocalFileHeader(curOffs);
                    skip = 0;

                    // TODO fix offs for zip64

                    //                long offsetLocalHdr = fileHeader.getOffsLocalFileHeader();
//                if (fileHeader.getZip64ExtendedInfo() != null &&
//                        fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative() != -1) {
//                    offsetLocalHdr = fileHeader.getZip64ExtendedInfo().getOffsLocalHeaderRelative();
//                }
//
//                fileHeader.setOffsLocalFileHeader(offsetLocalHdr - (offs - offsetLocalFileHeader) - 1);
                }

                prv = entries.contains(header.getFileName()) ? null : header;
                skip = header.getOffsLocalFileHeader() - offsIn;
            }

            if (prv != null) {
                long curOffs = offsOut;
                long length = zipModel.getOffsCentralDirectory() - prv.getOffsLocalFileHeader();
                offsOut += IOUtils.copyLarge(in, out, skip, length);
                fileHeaders.add(prv);
                prv.setOffsLocalFileHeader(curOffs);
            }
        }

        zipModel.setFileHeaders(fileHeaders);
        zipModel.getEndCentralDirectory().setOffs(offsOut);
    }

    private void restoreFileName(Path tmpZipFileName) {
        try {
            if (tmpZipFileName == null)
                return;
            if (Files.deleteIfExists(zipModel.getZipFile()))
                Files.move(tmpZipFileName, zipModel.getZipFile());
            else
                throw new Zip4jException("cannot delete old zip file");
        } catch(IOException e) {
            throw new Zip4jException(e);
        }
    }
}
