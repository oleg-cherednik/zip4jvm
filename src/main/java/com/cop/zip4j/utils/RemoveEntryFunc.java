package com.cop.zip4j.utils;

import com.cop.zip4j.exception.Zip4jException;
import com.cop.zip4j.io.out.DataOutput;
import com.cop.zip4j.io.out.DataOutputStreamDecorator;
import com.cop.zip4j.io.out.SingleZipOutputStream;
import com.cop.zip4j.model.CentralDirectory;
import com.cop.zip4j.model.ZipModel;
import com.cop.zip4j.model.entry.PathZipEntry;
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

        try (DataOutput out = SingleZipOutputStream.create(tmpZipFile, zipModel)) {
            writeFileHeaders(new DataOutputStreamDecorator(out), entries);
        } catch(IOException e) {
            throw new Zip4jException(e);
        }

        restoreFileName(tmpZipFile);
    }

    private Set<String> getExistedEntries(Collection<String> entryNames) {
        return entryNames.stream()
                         .filter(Objects::nonNull)
                         .map(entryName -> ZipUtils.normalizeFileName.apply(entryName.toLowerCase()))
                         .map(entryName -> zipModel.getEntries().stream()
                                                   .filter(entry -> entry.getName().equalsIgnoreCase(entryName))
                                                   .map(PathZipEntry::getName)
                                                   .collect(Collectors.toList()))
                         .flatMap(List::stream)
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
        List<PathZipEntry> zipEntries = new ArrayList<>();
        CentralDirectory.FileHeader prv = null;
        PathZipEntry prvEntry = null;

        long offsIn = 0;
        long offsOut = 0;
        long skip = 0;

        try (InputStream in = new FileInputStream(zipModel.getZipFile().toFile())) {
            int total = zipModel.getFileHeaders().size();

            for (int i = 0; i < total; i++) {
                CentralDirectory.FileHeader header = zipModel.getFileHeaders().get(i);
                PathZipEntry zipEntry = zipModel.getEntries().get(i);

                if (prvEntry != null) {
                    long curOffs = offsOut;
                    long length = zipEntry.getOffsLocalFileHeader() - prvEntry.getOffsLocalFileHeader();
                    offsIn += skip + IOUtils.copyLarge(in, out, skip, length);
                    offsOut += length;
                    fileHeaders.add(prv);
                    zipEntries.add(prvEntry);
                    prv.setOffsLocalFileHeader(curOffs);
                    prvEntry.setOffsLocalFileHeader(curOffs);
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

                if (entries.contains(zipEntry.getName())) {
                    prv = null;
                    prvEntry = null;
                } else {
                    prv = header;
                    prvEntry = zipEntry;
                }

                skip = zipEntry.getOffsLocalFileHeader() - offsIn;
            }

            if (prvEntry != null) {
                long curOffs = offsOut;
                long length = zipModel.getCentralDirectoryOffs() - prvEntry.getOffsLocalFileHeader();
                offsOut += IOUtils.copyLarge(in, out, skip, length);
                fileHeaders.add(prv);
                zipEntries.add(prvEntry);
                prv.setOffsLocalFileHeader(curOffs);
                prvEntry.setOffsLocalFileHeader(curOffs);
            }
        }

        zipModel.setFileHeaders(fileHeaders);
        zipModel.getEntries().clear();
        zipModel.getEntries().addAll(zipEntries);
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
