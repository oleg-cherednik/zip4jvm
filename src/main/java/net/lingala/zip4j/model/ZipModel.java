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

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class ZipModel implements Cloneable {

    public static final int NO_SPLIT = -1;

    private List localFileHeaderList;

    private List dataDescriptorList;

    private ArchiveExtraDataRecord archiveExtraDataRecord;

    @NonNull
    private CentralDirectory centralDirectory = new CentralDirectory();

    private EndCentralDirectory endCentralDirectory;

    private Zip64EndCentralDirectoryLocator zip64EndCentralDirectoryLocator;

    private Zip64EndCentralDirectory zip64EndCentralDirectory;

    private long splitLength = NO_SPLIT;

    private Path zipFile;

    private boolean isZip64Format;

    private boolean isNestedZipFile;

    private long start;

    private long end;

    @NonNull
    private Charset charset = Charset.defaultCharset();

    public void addLocalFileHeader(LocalFileHeader localFileHeader) {
        localFileHeaderList = localFileHeaderList.isEmpty() ? new ArrayList<>() : localFileHeaderList;
        localFileHeaderList.add(localFileHeader);
    }

    public void createEndCentralDirectoryIfNotExist() {
        if (endCentralDirectory == null)
            endCentralDirectory = new EndCentralDirectory();
    }

    public void setEndCentralDirectory(EndCentralDirectory endCentralDirectory) {
        this.endCentralDirectory = endCentralDirectory;
        // TODO check is it used, because 1 split length is invalid value, but it marks an archive as split
        splitLength = endCentralDirectory != null && endCentralDirectory.getNoOfDisk() > 0 ? 1 : NO_SPLIT;
    }


    public boolean isSplitArchive() {
        return splitLength > 0;
    }

    public void setNoSplitArchive() {
        splitLength = NO_SPLIT;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public byte[] convertFileNameToByteArr(String fileName) throws UnsupportedEncodingException {
        return fileName.getBytes(charset);
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

    public CentralDirectory.FileHeader getFileHeader(@NonNull String fileName) throws ZipException {
        if (isEmpty())
            return null;

        return getFileHeaders().stream()
                               .filter(fileHeader -> FilenameUtils.equalsNormalized(fileName, fileHeader.getFileName()))
                               .findFirst().orElse(null);
    }

    public long getOffOfStartOfCentralDir() {
        if (isZip64Format())
            return zip64EndCentralDirectory.getOffsetStartCenDirWRTStartDiskNo();
        if (endCentralDirectory != null)
            return endCentralDirectory.getOffOfStartOfCentralDir();
        return 0;
    }

    public static Path getSplitFilePath(Path zipFile, int count) {
        return zipFile.getParent().resolve(String.format("%s.z%02d", FilenameUtils.getBaseName(zipFile.toString()), count));
    }

    public Path getPartFile(int diskNumberStartOfFile) {
        if (diskNumberStartOfFile == endCentralDirectory.getNoOfDisk())
            return zipFile;
        return getSplitFilePath(zipFile, diskNumberStartOfFile + 1);
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
            fileHeader.updateOffLocalHeaderRelative(Arrays.stream(fileSizeList, 0, fileHeader.getDiskNumberStart()).sum());
            fileHeader.setDiskNumberStart(0);
        });
    }

    private void updateEndCentralDirectory(long totalBytesWritten) throws ZipException {
        endCentralDirectory.setNoOfDisk(0);
        endCentralDirectory.setNoOfDiskStartCentralDir(0);
        endCentralDirectory.setTotNoOfEntriesInCentralDir(getFileHeaders().size());
        endCentralDirectory.setTotalNumberOfEntriesInCentralDirOnThisDisk(getFileHeaders().size());
        endCentralDirectory.setOffOfStartOfCentralDir(totalBytesWritten);
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
            zip64EndCentralDirectory.setTotNoOfEntriesInCentralDirOnThisDisk(endCentralDirectory.getTotNoOfEntriesInCentralDir());
            zip64EndCentralDirectory.updateOffsetStartCenDirWRTStartDiskNo(totalBytesWritten);
        }
    }

    public List<CentralDirectory.FileHeader> getFileHeaders() {
        return centralDirectory.getFileHeaders();
    }

    public void addFileHeader(CentralDirectory.FileHeader fileHeader) {
        centralDirectory.addFileHeader(fileHeader);
    }

}
