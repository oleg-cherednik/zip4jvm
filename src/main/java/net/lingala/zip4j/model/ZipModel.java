/*
 * Copyright 2010 Srikanth Reddy Lingala
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.lingala.zip4j.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ZipModel {

    public static final int NO_SPLIT = -1;

    private final List<LocalFileHeader> localFileHeaders = new ArrayList<>();
    private ArchiveExtraDataRecord archiveExtraDataRecord;
    @NonNull
    private CentralDirectory centralDirectory = new CentralDirectory();
    private EndCentralDirectory endCentralDirectory = new EndCentralDirectory();
    private Zip64 zip64;
    private long splitLength = NO_SPLIT;
    @NonNull
    private Path zipFile;
    @NonNull
    private Charset charset = Charset.defaultCharset();

    public void addLocalFileHeader(@NonNull LocalFileHeader localFileHeader) {
        localFileHeaders.add(localFileHeader);
    }

    public void setEndCentralDirectory(EndCentralDirectory endCentralDirectory) {
        this.endCentralDirectory = endCentralDirectory;
        // TODO check is it used, because 1 split length is invalid value, but it marks an archive as split
        splitLength = endCentralDirectory != null && endCentralDirectory.getDiskNumber() > 0 ? 1 : NO_SPLIT;
    }

    public Zip64EndCentralDirectoryLocator getZip64EndCentralDirectoryLocator() {
        return isZip64() ? zip64.getEndCentralDirectoryLocator() : null;
    }

    public Zip64EndCentralDirectory getZip64EndCentralDirectory() {
        return isZip64() ? zip64.getEndCentralDirectory() : null;
    }

    public boolean isSplitArchive() {
        return splitLength > 0;
    }

    public void setNoSplitArchive() {
        splitLength = NO_SPLIT;
    }

    public boolean isZip64() {
        return zip64 != null;
    }

    public void zip64() {
        zip64(new Zip64EndCentralDirectoryLocator(), new Zip64EndCentralDirectory());
    }

    public void zip64(Zip64EndCentralDirectoryLocator zip64EndCentralDirectoryLocator, Zip64EndCentralDirectory zip64EndCentralDirectory) {
        zip64 = new Zip64(zip64EndCentralDirectoryLocator, zip64EndCentralDirectory);
    }

    public void updateZip64() {
        if (!isZip64())
            return;

        Zip64EndCentralDirectory dir = zip64.getEndCentralDirectory();
        dir.setSize(Zip64EndCentralDirectory.SIZE + ArrayUtils.getLength(dir.getExtensibleDataSector()));
        dir.setVersionMadeBy(isEmpty() ? CentralDirectory.FileHeader.DEF_VERSION : getFileHeaders().get(0).getVersionMadeBy());
        dir.setVersionNeededToExtract(isEmpty() ? CentralDirectory.FileHeader.DEF_VERSION : getFileHeaders().get(0).getVersionToExtract());
        dir.setDiskNumber(endCentralDirectory.getDiskNumber());
        dir.setStartDiskNumber(endCentralDirectory.getStartDiskNumber());
        dir.setDiskEntries(countNumberOfFileHeaderEntriesOnDisk());
        dir.setTotalEntries(getFileHeaders().size());
        dir.setSize(endCentralDirectory.getSize());
        dir.setOffs(endCentralDirectory.getOffs());

        Zip64EndCentralDirectoryLocator locator = zip64.getEndCentralDirectoryLocator();
        locator.setOffsetZip64EndOfCentralDirRec(endCentralDirectory.getOffs() + endCentralDirectory.getSize());
    }

    private int countNumberOfFileHeaderEntriesOnDisk() {
        if (isSplitArchive())
            return getFileHeaders().size();

        return (int)getFileHeaders().stream()
                                    .filter(fileHeader -> fileHeader.getDiskNumber() == endCentralDirectory.getDiskNumber())
                                    .count();
    }

    public boolean isEmpty() {
        return getFileHeaders().isEmpty();
    }

    public List<String> getEntryNames() {
        return getFileHeaders().stream()
                               .map(CentralDirectory.FileHeader::getFileName)
                               .collect(Collectors.toList());
    }

    public long getTotalEntries() {
        return isZip64() ? zip64.getEndCentralDirectory().getTotalEntries() : endCentralDirectory.getTotalEntries();
    }

    public long getOffsCentralDirectory() {
        if (isZip64())
            return zip64.getEndCentralDirectory().getOffs();
        if (endCentralDirectory != null)
            return endCentralDirectory.getOffs();
        return 0;
    }

    public static Path getSplitFilePath(Path zipFile, int count) {
        return zipFile.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zipFile.toString()), count));
    }

    public Path getPartFile(int diskNumber) {
        return diskNumber == endCentralDirectory.getDiskNumber() ? zipFile : getSplitFilePath(zipFile, diskNumber + 1);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    public void convertToSolid(long[] fileSizeList) {
        long totalBytesWritten = Arrays.stream(fileSizeList).sum();

        setNoSplitArchive();
        updateFileHeaders(fileSizeList);
        updateEndCentralDirectory(totalBytesWritten);
        updateZip64EndCentralDirLocator(totalBytesWritten);
        updateZip64EndCentralDirRec(totalBytesWritten);
    }

    @SuppressWarnings("MethodCanBeVariableArityMethod")
    private void updateFileHeaders(long[] fileSizeList) {
        getFileHeaders().forEach(fileHeader -> {
            fileHeader.updateOffLocalHeaderRelative(Arrays.stream(fileSizeList, 0, fileHeader.getDiskNumber()).sum());
            fileHeader.setDiskNumber(0);
        });
    }

    private void updateEndCentralDirectory(long totalBytesWritten) throws ZipException {
        endCentralDirectory.setDiskNumber(0);
        endCentralDirectory.setStartDiskNumber(0);
        endCentralDirectory.setTotalEntries(getFileHeaders().size());
        endCentralDirectory.setDiskEntries(getFileHeaders().size());
        endCentralDirectory.setOffs(totalBytesWritten);
    }

    private void updateZip64EndCentralDirLocator(long totalBytesWritten) throws ZipException {
        if (isZip64()) {
            zip64.getEndCentralDirectoryLocator().setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
            zip64.getEndCentralDirectoryLocator().updateOffsetZip64EndOfCentralDirRec(totalBytesWritten);
            zip64.getEndCentralDirectoryLocator().setTotNumberOfDiscs(1);
        }
    }

    private void updateZip64EndCentralDirRec(long totalBytesWritten) throws ZipException {
        if (isZip64()) {
            zip64.getEndCentralDirectory().setDiskNumber(0);
            zip64.getEndCentralDirectory().setStartDiskNumber(0);
            zip64.getEndCentralDirectory().setDiskEntries(endCentralDirectory.getTotalEntries());
            zip64.getEndCentralDirectory().updateOffsetStartCenDirWRTStartDiskNo(totalBytesWritten);
        }
    }

    public List<CentralDirectory.FileHeader> getFileHeaders() {
        return centralDirectory.getFileHeaders();
    }

    public void addFileHeader(CentralDirectory.FileHeader fileHeader) {
        centralDirectory.addFileHeader(fileHeader);
        endCentralDirectory.incTotalEntries();
        endCentralDirectory.incDiskEntries();
    }

    public void setFileHeaders(List<CentralDirectory.FileHeader> fileHeaders) {
        centralDirectory.setFileHeaders(fileHeaders);
        endCentralDirectory.setTotalEntries(fileHeaders.size());
        endCentralDirectory.setDiskEntries(fileHeaders.size());
    }

    public long getEndCentralDirectoryOffs() {
        return isZip64() ? zip64.getEndCentralDirectory().getOffs() : endCentralDirectory.getOffs();
    }

    @NonNull
    public ZipModel noSplitOnly() {
        if (Files.exists(zipFile) && isSplitArchive())
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");

        return this;
    }

    @Getter
    @RequiredArgsConstructor
    public static final class Zip64 {

        private final Zip64EndCentralDirectoryLocator endCentralDirectoryLocator;
        private final Zip64EndCentralDirectory endCentralDirectory;

    }

}
