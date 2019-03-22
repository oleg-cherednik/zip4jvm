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
import lombok.Setter;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FilenameUtils;

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
    private Zip64EndCentralDirectoryLocator zip64EndCentralDirectoryLocator;
    private Zip64EndCentralDirectory zip64EndCentralDirectory;
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

    public boolean isSplitArchive() {
        return splitLength > 0;
    }

    public void setNoSplitArchive() {
        splitLength = NO_SPLIT;
    }

    public boolean isZip64Format() {
        return zip64EndCentralDirectoryLocator != null;
    }

    public boolean isEmpty() {
        return getFileHeaders().isEmpty();
    }

    public List<String> getEntryNames() {
        return getFileHeaders().stream()
                               .map(CentralDirectory.FileHeader::getFileName)
                               .collect(Collectors.toList());
    }

    public long getOffsCentralDirectory() {
        if (isZip64Format())
            return zip64EndCentralDirectory.getOffsetStartCenDirWRTStartDiskNo();
        if (endCentralDirectory != null)
            return endCentralDirectory.getOffsCentralDirectory();
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
        endCentralDirectory.setOffsCentralDirectory(totalBytesWritten);
    }

    private void updateZip64EndCentralDirLocator(long totalBytesWritten) throws ZipException {
        if (isZip64Format()) {
            zip64EndCentralDirectoryLocator.setNoOfDiskStartOfZip64EndOfCentralDirRec(0);
            zip64EndCentralDirectoryLocator.updateOffsetZip64EndOfCentralDirRec(totalBytesWritten);
            zip64EndCentralDirectoryLocator.setTotNumberOfDiscs(1);
        }
    }

    private void updateZip64EndCentralDirRec(long totalBytesWritten) throws ZipException {
        if (isZip64Format()) {
            zip64EndCentralDirectory.setNoOfThisDisk(0);
            zip64EndCentralDirectory.setNoOfThisDiskStartOfCentralDir(0);
            zip64EndCentralDirectory.setTotNoOfEntriesInCentralDirOnThisDisk(endCentralDirectory.getTotalEntries());
            zip64EndCentralDirectory.updateOffsetStartCenDirWRTStartDiskNo(totalBytesWritten);
        }
    }

    public List<CentralDirectory.FileHeader> getFileHeaders() {
        return centralDirectory.getFileHeaders();
    }

    public void addFileHeader(CentralDirectory.FileHeader fileHeader) {
        centralDirectory.addFileHeader(fileHeader);
    }

    public void setFileHeaders(List<CentralDirectory.FileHeader> fileHeaders) {
        centralDirectory.setFileHeaders(fileHeaders);
        endCentralDirectory.setTotalEntries(fileHeaders.size());
        endCentralDirectory.setDiskEntries(fileHeaders.size());
    }

    @NonNull
    public ZipModel noSplitOnly() {
        if (Files.exists(zipFile) && isSplitArchive())
            throw new ZipException("Zip file already exists. Zip file format does not allow updating split/spanned files");

        return this;
    }

}
